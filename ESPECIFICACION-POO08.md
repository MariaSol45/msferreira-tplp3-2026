# Especificación POO-08 - Minecraft

## Identificación

- Proyecto: `msferreira-tplp3-2026/minecraft`.
- Tecnologías: Java 21, Spring Boot 4.1.1, Spring Web MVC, Spring Data JPA y H2.
- Commit evaluado: [8cd5f741f377c8d22e323d818658807c440e889c](https://github.com/MariaSol45/msferreira-tplp3-2026/commit/8cd5f741f377c8d22e323d818658807c440e889c).
- Cierre de la corrección (código, pruebas, documentación e Insomnia): [d14f1b7633bf71b9a0efda935b18c2c654857580](https://github.com/MariaSol45/msferreira-tplp3-2026/commit/d14f1b7633bf71b9a0efda935b18c2c654857580). El traslado de las excepciones a `service.exception` se hizo después, en la rama de POO-09; su commit queda enlazado en el README.
- Referencias: [enunciado POO-08](https://github.com/alefq/afq-taller-git-2024/blob/main/docs/ejercicio-poo-08-crud-h2-insomnia-2026-10-28.md), [rúbrica](https://github.com/alefq/afq-taller-git-2024/blob/main/docs/RUBRICA-ejercicios-lp3-2026.md) y [especificación histórica POO-06](ESPECIFICACION-POO06.md).

Para la entrega conjunta, usar el enlace del commit que incluya también el cierre documental cuando se publique.

## Decisión ante la contradicción

El enunciado público original ubica las reglas del juego en métodos del dominio. La devolución específica transmitida para esta corrección pide entidades de estado y validaciones, reglas y lanzamiento de excepciones de negocio en `service`. Se sigue esa indicación sin atribuirla al texto del enunciado público.

Para conservar lo aprobado:

- se mantienen herencia `JOINED`, abstracción, `comportamiento()`, sobreescritura, `UICapable` y las firmas de constructores;
- la sobrecarga de ataque se conserva como `EntidadService.atacar(...)` con y sin distancia;
- se eliminan de las entidades los métodos que ejecutaban reglas: `validarEstado`, callbacks JPA, `actualizarDesde`, `recibirDano`, `Creeper.explotar(int)`, validación de radio y aplicación de daño;
- `Creeper.explotar()` sin argumentos permanece como comportamiento descriptivo histórico y no decide una operación persistente.

## Modelo y persistencia

`Entidad` es abstracta y persistente. `EntidadGenerica` conserva registros históricos neutrales. Creeper, Jugador y Aldeano son los participantes persistidos de la simulación. Zombie, Esqueleto y Animal permanecen como tipos transitorios.

Se usa `InheritanceType.JOINED`:

| Tabla | Estado |
|---|---|
| `ENTIDADES` | ID, salud, posición y velocidad. |
| `ENTIDADES_GENERICAS` | Subtipo neutral. |
| `ENTIDADES_HOSTILES` | Rango y daño. |
| `CREEPERS` | Tiempo de explosión. |
| `JUGADORES` | Nombre y experiencia. |
| `ENTIDADES_PASIVAS` | Domesticable. |
| `ALDEANOS` | Profesión. |

Los constructores JPA sin argumentos siguen siendo `protected`. Los campos son privados y JPA usa acceso por campo. No hay setters públicos de salud ni callbacks de validación.

`EntidadStateWriter` es package-private dentro de `service`. Copia campos o cambia salud por reflexión después de validar; no contiene decisiones ni expone mutadores públicos. Esto protege las rutas de aplicación, pero no impide que un acceso directo a JPA o SQL eluda los servicios. No se agrega una migración ni se normalizan filas históricas automáticamente.

## JSON e identidad

El ID es `Long`, generado con `IDENTITY`. POST y PUT rechazan un ID no nulo en el cuerpo. El ID de URL de PUT (y el de los demás accesos por ID) debe ser positivo; un ID positivo inexistente devuelve 404. El servicio de escritura solo acepta `EntidadGenerica`, `Creeper`, `Jugador` y `Aldeano`.

Jackson exige `tipo`:

| Valor | Clase |
|---|---|
| `entidad` | `EntidadGenerica` |
| `creeper` | `Creeper` |
| `jugador` | `Jugador` |
| `aldeano` | `Aldeano` |

POST y PUT exigen los campos de construcción comunes y propios del subtipo. Por ejemplo, además de `tipo`, `Jugador` lleva `nombre`, `nivelEXP` (alias `experiencia`), `salud`, `posicionX`, `posicionY`, `posicionZ` y `velocidad`. La configuración de Jackson rechaza los campos obligatorios omitidos o nulos y evita convertir decimales o texto en enteros. PUT es reemplazo completo, conserva el subtipo y valida antes de escribir: `{"tipo":"jugador","nombre":"Alex"}` da 400 y conserva el registro.

## Responsabilidades

### Entidades

Representan estado, relaciones de herencia y comportamiento polimórfico descriptivo. Un constructor puede representar temporalmente datos inválidos recibidos por Jackson; ese objeto no se persiste hasta que el servicio lo acepta.

### EntidadService

- CRUD y transacciones.
- Reglas de salud de creación y estado posterior.
- Validación de coordenadas, velocidad, rango, daño, tiempo, nombre, experiencia y profesión.
- Compatibilidad de subtipos en PUT.
- Construcción y validación de demostraciones transitorias.
- Sobrecargas de ataque y transición de salud.
- Resolución de `UICapable` transitorio o persistido.

### ExplosionService

- Valida solicitud, IDs y tipos.
- Valida el estado actual de Creeper, Jugador y Aldeano.
- Exige participantes vivos.
- Valida tiempo de detonación.
- Calcula distancia euclidiana 3D con `Math.hypot` y valida el radio inclusivo.
- Valida ambos objetivos antes de aplicar daño.
- Aplica y persiste daño dentro de `@Transactional`.
- Lanza `ExplosionNoPreparadaException` y `ObjetivoFueraDeRangoException`.

### Controllers

Traducen HTTP y delegan. No contienen límites de salud, reglas de daño, tiempo o radio.

## Invariantes

### Salud

| Tipo | Creación (POST o demostración transitoria) | PUT o estado posterior al daño |
|---|---|---|
| Jugador | 1 a 20 | 0 a 20 |
| Creeper | 1 a 20 | 0 a 20 |
| Aldeano | 1 a 20 | 0 a 20 |
| Zombie/Esqueleto transitorios | 1 a 20 | No aplica |
| EntidadGenerica | Mayor que 0 | Mayor o igual que 0 |
| Animal transitorio | Mayor que 0 | No aplica |

Se toma como referencia la salud base habitual de 20 de Jugador, Creeper, Aldeano, Zombie y Esqueleto en Minecraft vanilla, sin representar modificadores. No se asigna un máximo adicional a tipos genéricos que no identifican una especie.

El daño debe ser positivo y la transición es:

```text
saludFinal = max(0, saludInicial - dano)
```

Cero es inválido como salud inicial, pero válido después del daño y en PUT para representar una entidad muerta. PUT completo también puede volver a fijar una salud positiva válida. No hay un endpoint de curación o reaparición; es una capacidad del reemplazo administrativo del proyecto.

### Otras invariantes

- Coordenadas finitas; se aceptan valores negativos.
- Velocidad no negativa.
- Rango finito y no negativo.
- Daño de ataque positivo.
- Tiempo de explosión positivo.
- Experiencia no negativa.
- Nombre y profesión no nulos ni blancos.
- IDs positivos.
- Tipos de participantes compatibles.
- Participantes de explosión vivos.

## CRUD

| Método | URL | Éxito | Errores |
|---|---|---:|---|
| POST | `/entidades` | 201 | 400 |
| GET | `/entidades` | 200 | - |
| GET | `/entidades/{id}` | 200 | 400, 404 |
| PUT | `/entidades/{id}` | 200 | 400, 404 |
| DELETE | `/entidades/{id}` | 204 | 400, 404 |

La persistencia inválida no se intenta: POST valida antes de `save`; PUT valida los datos y el subtipo antes de copiar estado.

## Consulta de dominio

`EntidadRepository.findBySaludGreaterThan(0)` devuelve entidades vivas. Es una consulta derivada de Spring Data, no una implementación SQL manual. Las pruebas persisten una entidad viva y otra muerta y verifican el filtro.

## UICapable

Jugador y Aldeano implementan `UICapable` desde ramas distintas. `GET /ui` conserva el modo transitorio histórico y agrega el modo persistido:

```text
GET /ui?tipo=jugador&salud=20&nombre=Alex
GET /ui?id={idPersistido}
```

Con `id`, el servicio recupera la entidad real y comprueba que implemente la interfaz.

## Simulación

Solicitud:

```json
{
  "creeperId": 1,
  "jugadorId": 2,
  "aldeanoId": 3,
  "tiempoTranscurrido": 3
}
```

Los IDs del ejemplo deben sustituirse por los IDs reales. Orden de ejecución:

1. Validar tiempo de entrada e IDs.
2. Cargar y comprobar tipos.
3. Validar los tres estados y que estén vivos.
4. Comparar tiempo transcurrido con tiempo de explosión.
5. Calcular y validar las dos distancias; una distancia igual al radio es válida.
6. Aplicar daño con límite inferior cero y reportar la salud efectivamente perdida.
7. Persistir ambas entidades dentro de la misma transacción.

La validación completa ocurre antes del primer cambio de salud. Un 409 o 422 conserva ambas saludes.

## Excepciones

```text
RuntimeException
└── ExplosionException
    ├── ExplosionNoPreparadaException -> 409
    └── ObjetivoFueraDeRangoException -> 422
```

Las clases personalizadas están en `service.exception` y se lanzan desde `ExplosionService`; el dominio no declara ni lanza excepciones. `@RestControllerAdvice` mantiene un handler por hija y traduce argumentos inválidos, recurso inexistente y JSON ilegible. Los errores de conversión de parámetros de URL que resuelve Spring pueden tener otro cuerpo.

## Simplificaciones documentadas

- Radio de explosión = `rangoDeteccion`.
- Daño base de explosión = `danoAtaque`.
- Daño plano sin atenuación por distancia.
- Sin dificultad, armadura, bloques, línea de visión ni Creeper cargado.
- Exactamente dos objetivos obligatorios.
- El Creeper no se consume ni cambia de estado después de la simulación. Una repetición válida aplica daño otra vez; con un participante muerto se rechaza.

Estas simplificaciones preservan el contrato aprobado. No se presentan como reproducción exacta de Minecraft vanilla.

## Atomicidad

`ExplosionService.simular` usa `@Transactional`. Las excepciones personalizadas son no comprobadas y provocan rollback si salen de la operación. Los 409 y 422 ocurren antes de mutar. La prueba de rollback, en cambio, hace `flush`, confirma por JDBC que la transacción ve el cambio `20 → 12` para ambos objetivos, fuerza un fallo y comprueba por JDBC que las dos filas conservan 20 después de salir de la transacción.

La atomicidad de una invocación no impide que dos explosiones simultáneas, o un PUT y una explosión sobre los mismos registros, se sobrescriban. No hay bloqueo pesimista ni control optimista de versión.

## Pruebas

Comando:

```bash
cd minecraft
./mvnw test
```

La suite del cierre registró **96 ejecuciones, 0 fallos, 0 errores y 0 omitidas** tanto en orden normal como aleatorio. Cubre:

- Jugador POST con salud 0, -1, 30 y 5000: 400 y cero inserciones;
- Jugador POST con salud 1 y 20: 201;
- POST con ID existente y PUT incompleto, nulo o fuera del límite: 400 y estado anterior intacto;
- salud decimal o entre comillas: 400;
- daño letal: salud final cero;
- ausencia de `setSalud` público y rechazo por servicio;
- Creeper con tiempo cero: 400 sin daño;
- tiempo insuficiente: 409 sin daño;
- objetivo fuera de radio: 422 sin daño parcial;
- CRUD, JSON polimórfico y borrado;
- anotación y tablas físicas `JOINED` después de `flush/clear`;
- `UICapable` transitorio y persistido;
- consulta de entidades vivas;
- persistencia de salud después de explosión, participantes muertos o IDs repetidos, distancias extremas y sobredaño;
- rollback comprobado después de enviar las actualizaciones a H2 con `flush`.

También se ejecuta con orden aleatorio:

```bash
./mvnw -Dsurefire.runOrder=random -Dsurefire.runOrder.random.seed=620824 test
```

## Defectos de la entrega evaluada

1. Jugador no tenía máximo de salud y aceptaba 30, 35, 40 o 5000.
2. POST no distinguía salud inicial de salud posterior al daño.
3. Entidades validaban y lanzaban excepciones desde constructores, callbacks y métodos de negocio.
4. `setSalud` público permitía cambios directos.
5. Creeper validaba tiempo/radio y aplicaba daño, en vez del servicio.
6. Controllers construían y ejecutaban reglas de demostración.
7. `/ui` solo operaba con objetos transitorios.
8. El repositorio no tenía consulta de dominio.
9. Documentación e Insomnia usaban Jugador 30 y Aldeano 25.

## Por qué las 71 pruebas eran verdes

Los tests reflejaban la implementación defectuosa: esperaban validación en constructores, invocaban daño desde entidades y creaban Jugadores por encima de 20. No había casos 0/5000 para POST, límites 1/20, PUT inválido con consulta posterior, ausencia de setter, inspección física de `JOINED` ni UI sobre persistencia. Por eso 71 pruebas exitosas no demostraban las reglas corregidas.

La corrección intermedia de 83 pruebas todavía dejaba pasar un `id` en POST, valores por defecto en PUT incompleto, coerción de tipos y errores de distancia por elevar diferencias extremas al cuadrado. Su prueba de rollback fallaba antes de enviar los `UPDATE` a H2. La base `4e4c38c` corrige estos puntos y añade regresiones específicas.

## Insomnia

`insomnia/poo08-minecraft.json` incluye CRUD y simulación, salud 0 y 5000, tiempo de Creeper cero, tiempo transcurrido ausente/negativo, ID no nulo en POST, salud decimal, PUT incompleto, 409 y 422. Los GET posteriores comprueban que los errores no insertaron filas ni alteraron los objetivos. El 404 se verifica con el ID del Creeper eliminado por el propio recorrido.

Ejecutar en el orden del README, contra la instancia aislada, y copiar los IDs de los POST al entorno antes de seguir. Se verificaron por HTTP las 31 solicitudes del export y se comprobó que la colección quedara vacía al terminar la limpieza. No se verificó la importación gráfica en Insomnia.

## Verificación

```bash
cd minecraft
./mvnw test
./mvnw -Dsurefire.runOrder=random -Dsurefire.runOrder.random.seed=620824 test
./mvnw spring-boot:run \
  -Dspring-boot.run.arguments="--spring.datasource.url=jdbc:h2:mem:poo08-smoke --spring.jpa.hibernate.ddl-auto=create-drop --spring.h2.console.enabled=false --server.address=127.0.0.1 --server.port=0"
```

Desde la raíz del repositorio:

```bash
jq empty insomnia/poo08-minecraft.json
git diff --check
sha256sum minecraft/data/entidades.mv.db
```
