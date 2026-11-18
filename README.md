# Minecraft - POO-08

Servicio HTTP de LP3 desarrollado con Java 21, Spring Boot, JPA y H2. Este proyecto continúa POO-06, POO-07 y POO-08: conserva la jerarquía, el polimorfismo, los constructores sobrecargados, el CRUD y la simulación aprobados, y corrige la separación de responsabilidades e invariantes detectadas en la revisión docente.

## Estado de la corrección

El commit evaluado se conserva como referencia histórica:

<https://github.com/MariaSol45/msferreira-tplp3-2026/commit/8cd5f741f377c8d22e323d818658807c440e889c>

Ese enlace no representa esta corrección posterior. El enlace de la nueva revisión debe obtenerse después de crear y publicar manualmente el futuro commit; este documento no inventa su SHA.

## Objetivo

El servicio persiste una familia de entidades Minecraft, ofrece CRUD polimórfico, expone `UICapable` y ejecuta una explosión entre un `Creeper`, un `Jugador` y un `Aldeano`.

Las dos reglas de la simulación son:

1. El tiempo transcurrido debe alcanzar `tiempoExplosion`; de lo contrario se lanza `ExplosionNoPreparadaException` y se responde 409.
2. Los dos objetivos deben estar dentro del radio antes de aplicar daño; si alguno queda fuera se lanza `ObjetivoFueraDeRangoException` y se responde 422 sin daño parcial.

## Tecnologías y ejecución

- JDK 21.
- Spring Boot 4.1.1.
- Spring Web MVC.
- Spring Data JPA.
- H2 persistente en desarrollo y H2 en memoria para pruebas.
- Maven Wrapper incluido.

Desde la raíz:

```bash
cd minecraft
./mvnw spring-boot:run
```

La API queda en `http://localhost:8080` y la consola H2 en `http://localhost:8080/h2-console`.

## Configuración H2

| Propiedad | Función |
|---|---|
| `spring.datasource.url=jdbc:h2:file:./data/entidades` | Usa el archivo `minecraft/data/entidades.mv.db` cuando se inicia desde `minecraft`. |
| `spring.datasource.driver-class-name=org.h2.Driver` | Selecciona el driver H2. |
| `spring.datasource.username=sa` | Usuario local de desarrollo. |
| `spring.datasource.password=` | Contraseña vacía solo para este ejercicio local. |
| `spring.jpa.hibernate.ddl-auto=update` | Conserva datos y adapta el esquema sin reinicializar la base. |
| `spring.h2.console.enabled=true` | Habilita la consola de desarrollo. |

Las pruebas usan `jdbc:h2:mem:minecraft-test`, `create-drop` y no modifican el archivo de desarrollo.

## Responsabilidades

### Dominio

`Entidad`, `EntidadHostil`, `EntidadPasiva`, `Creeper`, `Jugador`, `Aldeano` y `EntidadGenerica` representan estado y comportamiento polimórfico descriptivo. Los constructores conservan sus firmas, pero no validan ni lanzan excepciones de negocio. Tampoco existen `validarEstado()`, callbacks `@PrePersist/@PreUpdate`, `actualizarDesde()`, `recibirDano()` ni reglas de detonación o radio dentro de las entidades.

La ausencia de `setSalud` público impide modificar directamente la salud. `EntidadStateWriter` es una clase interna del paquete `service`, no pública, que solo escribe estado después de que el servicio validó la operación. JPA usa acceso por campo.

### Servicios

`EntidadService` valida POST, PUT, demostraciones transitorias y ataques. Distingue:

- creación: salud inicial mayor que cero;
- estado posterior: salud entre cero y el máximo aplicable;
- daño: `max(0, salud - dano)`, sin valores negativos.

`ExplosionService` valida IDs, tipos, estados, tiempo, radio y daño; pre valida todos los participantes y modifica la salud dentro de una transacción.

### Controllers

Los controllers traducen HTTP, delegan los casos de uso y construyen la representación de respuesta. No deciden límites de salud, daño, tiempo ni alcance.

## Invariantes

| Tipo o campo | Creación | Estado posterior o PUT |
|---|---|---|
| `Jugador.salud` | 1 a 20 | 0 a 20 |
| `Creeper.salud` | 1 a 20 | 0 a 20 |
| `Aldeano.salud` | 1 a 20 | 0 a 20 |
| `Zombie` y `Esqueleto` transitorios | 1 a 20 | No se persisten |
| `EntidadGenerica` | Mayor que 0 | Mayor o igual que 0 |
| `Animal` transitorio | Mayor que 0 | No se persiste |

Jugador, Creeper y Aldeano tienen 20 puntos máximos en Minecraft vanilla. `EntidadGenerica` y `Animal` no identifican una especie concreta, por lo que no se inventó un máximo para ellos.

Otras reglas:

- coordenadas finitas;
- velocidad, experiencia y rango no negativos;
- daño y tiempo de explosión mayores que cero;
- nombre y profesión no nulos ni blancos;
- IDs positivos y tipos compatibles;
- PUT conserva el subtipo;
- participantes de una explosión deben estar vivos;
- toda validación ocurre antes de copiar estado o aplicar daño.

## API REST

| Método | Ruta | Resultado |
|---|---|---|
| `GET` | `/` | Estado de la aplicación. |
| `GET` | `/entidad` | Construcción transitoria de seis tipos. |
| `GET` | `/entidad/atacar` | Demostración de las sobrecargas del servicio. |
| `GET` | `/ui` | Lectura polimórfica transitoria o persistida. |
| `GET` | `/entidades` | Lista persistida. |
| `GET` | `/entidades/{id}` | Obtiene una entidad. |
| `POST` | `/entidades` | Crea y devuelve 201 con `Location`. |
| `PUT` | `/entidades/{id}` | Reemplaza el estado del mismo subtipo. |
| `DELETE` | `/entidades/{id}` | Borra físicamente y devuelve 204. |
| `POST` | `/explosiones/simular` | Ejecuta y persiste daño. |

### JSON polimórfico

`tipo` es obligatorio en POST y PUT:

| `tipo` | Clase | Campos propios |
|---|---|---|
| `entidad` | `EntidadGenerica` | Campos comunes. |
| `creeper` | `Creeper` | `rangoDeteccion`, `danoAtaque`, `tiempoExplosion`. |
| `jugador` | `Jugador` | `nombre`, `nivelEXP`; acepta alias `experiencia`. |
| `aldeano` | `Aldeano` | `domesticable`, `profesion`. |

Ejemplo válido de Jugador:

```bash
curl -i -X POST http://localhost:8080/entidades \
  -H "Content-Type: application/json" \
  -d '{"tipo":"jugador","nombre":"Alex","nivelEXP":12,"salud":20,"posicionX":2,"posicionY":0,"posicionZ":0,"velocidad":1}'
```

Una creación de Jugador con salud 0, negativa o superior a 20 devuelve 400 y no inserta filas. PUT admite salud 0 porque representa muerte después de daño, pero rechaza valores negativos o superiores a 20 sin modificar el registro anterior.

### UICapable

El contrato transitorio anterior se conserva:

```bash
curl -i "http://localhost:8080/ui?tipo=jugador&nombre=Alex&salud=20"
```

También se puede consultar una implementación persistida:

```bash
curl -i "http://localhost:8080/ui?id=2"
```

El ID debe pertenecer a un `Jugador` o `Aldeano`, las dos implementaciones actuales de `UICapable`.

### Explosión

```json
{
  "creeperId": 1,
  "jugadorId": 2,
  "aldeanoId": 3,
  "tiempoTranscurrido": 3
}
```

`POST /explosiones/simular` carga los tres IDs persistidos, valida sus tipos y estados, valida el tiempo y las dos distancias, y recién entonces aplica daño. La transacción persiste ambas saludes o ninguna.

## Simplificaciones del juego

Este ejercicio no intenta reproducir todo Minecraft vanilla:

- `rangoDeteccion` también se usa como `radioExplosion` por compatibilidad con el modelo aprobado;
- `danoAtaque` también se usa como daño base de explosión;
- el daño es plano y no disminuye con la distancia;
- no se modelan dificultad, armadura, bloques, línea de visión ni Creeper cargado;
- la operación exige exactamente un Jugador y un Aldeano, ambos dentro del radio;
- el Creeper no se elimina ni cambia de estado después de simular.

Estas decisiones son deliberadas y están aisladas en `ExplosionService`, por lo que pueden evolucionar sin cambiar el contrato actual.

## Códigos HTTP

| Código | Caso |
|---:|---|
| 200 | GET, PUT y simulación válida. |
| 201 | Creación válida. |
| 204 | Eliminación válida. |
| 400 | JSON, ID, tipo, estado o invariante inválida. |
| 404 | ID positivo inexistente. |
| 409 | Tiempo de detonación insuficiente. |
| 422 | Objetivo fuera del radio. |

Las excepciones 409 y 422 conservan la jerarquía `ExplosionException` y handlers separados. Ahora se lanzan desde `ExplosionService`, no desde `Creeper`.

## Persistencia

Se mantiene `InheritanceType.JOINED`. Los atributos comunes viven en `ENTIDADES`; los subtipos agregan `ENTIDADES_GENERICAS`, `ENTIDADES_HOSTILES`, `CREEPERS`, `JUGADORES`, `ENTIDADES_PASIVAS` y `ALDEANOS`.

`EntidadRepository` extiende `JpaRepository<Entidad, Long>` y agrega la consulta derivada `findBySaludGreaterThan(0)`, probada con entidades vivas y muertas.

La escritura soportada por la aplicación pasa por los servicios. El repositorio permanece público porque Spring Data debe generar e inyectar su implementación; usarlo directamente desde otra capa omitiría las reglas de aplicación y no forma parte del contrato. Una prueba inserta deliberadamente un Creeper histórico inválido por repositorio para comprobar que `ExplosionService` también rechaza datos preexistentes corruptos.

## Qué hay que pensar

### Recurso y verbo

El recurso principal es `entidades`. POST, GET, PUT y DELETE expresan la operación; el ID va en `/entidades/{id}`. `/explosiones/simular` conserva la acción pedida por la consigna y opera sobre recursos ya persistidos.

### Identidad

H2 genera `Entidad.id` con `IDENTITY`. El cliente omite el ID al crear y recibe el valor en la respuesta 201 y en `Location`.

### Herencia y tablas

`JOINED` evita repetir campos comunes y permite consultas polimórficas, a cambio de joins. Jackson usa `tipo` para distinguir el subtipo JSON; ese dato no es un discriminador JPA.

### Interfaz y repositorio

`EntidadRepository` y `UICapable` son contratos. Spring Data implementa el primero; Jugador y Aldeano implementan el segundo. El controller depende de servicios y no conoce SQL.

### Ocultamiento

No hay setter público de salud. Los services validan antes de usar el escritor package-private. POST inválido y PUT inválido se prueban consultando H2 después del error.

### PUT y PATCH

Se implementa PUT de reemplazo completo, no PATCH. Deben enviarse todos los campos del subtipo y no se permite cambiar su clase.

### DELETE

El borrado es físico. El servicio valida el ID, recupera la entidad y delega `delete`; un GET posterior devuelve 404.

### Cambio de base

Para reemplazar H2 se cambian el driver y la configuración del datasource, y se revisa la compatibilidad del esquema. Dominio, servicios, controllers y `JpaRepository` no dependen de la URL de H2.

### Sobrecarga y sobreescritura

La sobreescritura mantiene la firma de `comportamiento()` en cada subtipo. La sobrecarga cambia parámetros: los constructores de Creeper y `EntidadService.atacar(...)` tienen variantes con diferente lista de argumentos.

## Pruebas

```bash
cd minecraft
./mvnw test
./mvnw -Dsurefire.runOrder=random test
```

La suite actual contiene 83 ejecuciones:

| Categoría | Ejecuciones |
|---|---:|
| Contexto Spring | 1 |
| Dominio y jerarquía | 13 |
| Servicios | 12 |
| Repositorio/JPA | 2 |
| Integración MockMvc | 55 |
| **Total** | **83** |

La cobertura incluye POST Jugador 0/5000 sin inserción, límites 1/20, PUT inválido sin cambios, daño letal a cero, ausencia de setter público, Creeper con tiempo 0, 409 y 422 sin daño, CRUD, JSON discriminado, tablas `JOINED`, `UICapable` persistido y consulta de entidades vivas.

### Por qué las 71 pruebas anteriores no detectaron el defecto

La suite evaluada era verde porque comprobaba el comportamiento que estaba mal especificado:

- aceptaba Jugador con salud 30, 35 y 40;
- solo rechazaba salud negativa y consideraba cero válido también para creación;
- probaba excepciones en constructores y reglas dentro de entidades, reforzando la responsabilidad equivocada;
- no probaba 5000, límites 1/20 ni PUT inválido con lectura posterior;
- no verificaba la estrategia `JOINED` ni sus tablas;
- `/ui` solo usaba objetos transitorios;
- los casos llamados rollback fallaban antes de mutar; sí demostraban prevalidación y ausencia de daño parcial, pero no un fallo posterior a la escritura.

## Colección Insomnia

Archivo: `insomnia/poo08-minecraft.json`. Usa `base_url` e IDs creados durante el recorrido. Incluye CRUD, UI persistida, explosión válida, salud inválida 0/5000, Creeper con tiempo 0, tiempo ausente/negativo, 404, 409, 422 y GET de comprobación.

Orden de uso:

1. Iniciar la aplicación desde `minecraft`.
2. En Insomnia, importar `insomnia/poo08-minecraft.json` como archivo.
3. Crear Creeper, Jugador y Aldeano y copiar los IDs al ambiente.
4. Ejecutar consultas iniciales y `/ui?id=...`.
5. Ejecutar una vez la explosión válida y consultar ambas saludes.
6. Ejecutar los casos 400, 404 y 409 y revisar el GET sin cambios.
7. Mover al Aldeano fuera del radio, ejecutar 422 y comprobar que ninguna salud cambió.
8. Ejecutar DELETE y confirmar 404.

La colección usa el formato de exportación 4. En este entorno se valida su JSON, pero no se afirma una importación gráfica porque Insomnia no está instalado.

## Diagrama

```mermaid
classDiagram
    class Entidad {
        <<abstract Entity>>
        -Long id
        -int salud
        -double posicionX
        -double posicionY
        -double posicionZ
        -int velocidad
        +comportamiento()* String
    }
    class EntidadHostil {
        <<abstract Entity>>
        -double rangoDeteccion
        -int danoAtaque
    }
    class EntidadPasiva {
        <<abstract Entity>>
        -boolean domesticable
    }
    class EntidadGenerica
    class Creeper {
        -int tiempoExplosion
    }
    class Jugador {
        -String nombre
        -int nivelEXP
    }
    class Aldeano {
        -String profesion
    }
    class UICapable {
        <<interface>>
        +getImagen() String
        +getPosicion() String
        +getSonido() String
        +getVelocidad() int
        +getMensajeUI() String
    }

    Entidad <|-- EntidadGenerica
    Entidad <|-- EntidadHostil
    Entidad <|-- EntidadPasiva
    Entidad <|-- Jugador
    EntidadHostil <|-- Creeper
    EntidadPasiva <|-- Aldeano
    UICapable <|.. Jugador
    UICapable <|.. Aldeano

    class EntidadRepository {
        <<interface>>
        +findBySaludGreaterThan(int salud) List~Entidad~
    }
    class EntidadService {
        +crear(Entidad entidad) Entidad
        +actualizar(Long id, Entidad datos) Entidad
        +atacar(int salud, double rango, int dano) ResultadoAtaque
        +atacar(int salud, double rango, int dano, double distancia) ResultadoAtaque
        +obtenerElementoUI(...) UICapable
    }
    class EntidadStateWriter {
        <<package-private>>
        +reemplazar(Entidad destino, Entidad origen)
        +cambiarSalud(Entidad entidad, int salud)
    }
    class ExplosionService {
        +simular(Long creeperId, Long jugadorId, Long aldeanoId, Integer tiempoTranscurrido)
    }
    class EntidadController
    class ExplosionController
    class ExplosionExceptionHandler
    class ExplosionException
    class ExplosionNoPreparadaException
    class ObjetivoFueraDeRangoException

    EntidadController --> EntidadService
    ExplosionController --> ExplosionService
    EntidadService --> EntidadRepository
    EntidadService ..> EntidadStateWriter
    ExplosionService --> EntidadRepository
    ExplosionService --> EntidadService
    ExplosionException <|-- ExplosionNoPreparadaException
    ExplosionException <|-- ObjetivoFueraDeRangoException
    ExplosionService ..> ExplosionNoPreparadaException : lanza
    ExplosionService ..> ObjetivoFueraDeRangoException : lanza
    ExplosionExceptionHandler ..> ExplosionNoPreparadaException
    ExplosionExceptionHandler ..> ObjetivoFueraDeRangoException
```

## Entregables

- [ESPECIFICACION-POO08.md](ESPECIFICACION-POO08.md)
- [BITACORA.md](BITACORA.md)
- [insomnia/poo08-minecraft.json](insomnia/poo08-minecraft.json)
- [LICENSE](LICENSE)

## Licencia

Apache License 2.0.
