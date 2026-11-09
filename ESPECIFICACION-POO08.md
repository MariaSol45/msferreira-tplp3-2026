# Especificación POO-08 - Minecraft

## Identificación

- Ejercicio: POO-08 - CRUD REST, H2, reglas de juego y excepciones.
- Dominio: Minecraft.
- Proyecto: `msferreira-tplp3-2026/minecraft`.
- Tecnologías: Java 21, Spring Boot 4.1.1, Spring Web MVC, Spring Data JPA y H2.
- Enunciado oficial: [ejercicio POO-08](https://github.com/alefq/afq-taller-git-2024/blob/main/docs/ejercicio-poo-08-crud-h2-insomnia-2026-10-28.md).
- Rúbrica oficial: [RUBRICA-ejercicios-lp3-2026.md](https://github.com/alefq/afq-taller-git-2024/blob/main/docs/RUBRICA-ejercicios-lp3-2026.md).

Esta especificación corresponde a POO-08. [ESPECIFICACION-POO06.md](ESPECIFICACION-POO06.md) se conserva como documento histórico del ejercicio anterior.

## Objetivo aplicado

Publicar un servicio que persista una familia de entidades de Minecraft, permita CRUD polimórfico, exponga un protocolo de dominio, simule una explosión entre tres clases persistidas y traduzca dos violaciones de reglas a respuestas HTTP diferenciadas.

## Modelo seleccionado

Las tres clases necesarias para la simulación son:

- `Creeper`: origen de la explosión y dueño de las reglas de tiempo, radio y daño.
- `Jugador`: primer objetivo y entidad persistida afectada.
- `Aldeano`: segundo objetivo y entidad persistida afectada.

`Entidad` es la raíz abstracta y persistente. `EntidadGenerica` es la hija neutral usada para representar registros históricos que antes no tenían subtipo concreto. `EntidadHostil` y `EntidadPasiva` son categorías abstractas. Zombie, Esqueleto y Animal permanecen en el modelo Java y en el endpoint transitorio, pero no forman parte del CRUD JPA.

## Persistencia

Se utiliza `InheritanceType.JOINED`:

- `ENTIDADES` contiene ID, salud, posición y velocidad.
- `ENTIDADES_GENERICAS` identifica el subtipo neutral.
- `ENTIDADES_HOSTILES` contiene rango y daño.
- `CREEPERS` contiene tiempo de explosión.
- `JUGADORES` contiene nombre y experiencia.
- `ENTIDADES_PASIVAS` contiene `domesticable`.
- `ALDEANOS` contiene profesión.

Cada clase JPA tiene constructor sin argumentos `protected`. Los constructores públicos o protegidos de negocio validan el estado. Los callbacks `@PrePersist` y `@PreUpdate` vuelven a validar antes de escribir una fila.

`EntidadRepository` extiende `JpaRepository<Entidad, Long>`. Spring Data genera su implementación; el código no implementa manualmente consultas CRUD.

## Identidad y JSON

`Entidad.id` es un `Long` generado por H2 con estrategia `IDENTITY`. El cliente omite el ID al crear y recibe el valor en la respuesta 201.

Jackson exige `tipo` para crear o actualizar:

| Valor | Clase |
|---|---|
| `entidad` | `EntidadGenerica` |
| `creeper` | `Creeper` |
| `jugador` | `Jugador` |
| `aldeano` | `Aldeano` |

Un discriminador ausente o desconocido produce 400. Un PUT debe conservar el subtipo del registro.

## Protocolo de dominio

`UICapable` declara mensajes de imagen, posición, sonido, velocidad y mensaje UI. Jugador y Aldeano lo implementan. `GET /ui` utiliza una referencia del tipo interfaz y responde con cualquiera de las dos implementaciones.

## CRUD REST

| Método | URL | Éxito | Error principal |
|---|---|---:|---:|
| `POST` | `/entidades` | 201 | 400 |
| `GET` | `/entidades` | 200 | - |
| `GET` | `/entidades/{id}` | 200 | 404 |
| `PUT` | `/entidades/{id}` | 200 | 400 o 404 |
| `DELETE` | `/entidades/{id}` | 204 | 404 |

El GET de colección usa `EntidadRepository.findAll()`. No arma una lista fija dentro del controller.

El PUT reemplaza los valores con el cuerpo enviado y no es un PATCH. Los campos omitidos reciben valores predeterminados de Java durante la deserialización; pueden provocar 400 o reemplazar el valor anterior. Por eso el cliente envía el cuerpo completo.

El DELETE es físico. El servicio obtiene primero la entidad para producir 404 si no existe y luego delega el borrado al repositorio.

## Endpoints complementarios

| Método | URL | Propósito |
|---|---|---|
| `GET` | `/` | Confirmar que la aplicación está activa. |
| `GET` | `/entidad` | Construir transitoriamente seis tipos y observar sobreescritura. |
| `GET` | `/entidad/atacar` | Observar los dos métodos `atacar` sobrecargados. |
| `GET` | `/ui` | Usar el protocolo `UICapable`. |
| `POST` | `/explosiones/simular` | Ejecutar la simulación persistente. |

## Reglas de explosión

### Regla 1: tiempo de detonación

`tiempoTranscurrido` es obligatorio y no puede ser negativo; esos errores de entrada producen 400. `Creeper.explotar(int tiempoTranscurrido)` exige que un valor no negativo sea mayor o igual que `tiempoExplosion`. Si no se cumple, lanza `ExplosionNoPreparadaException` y la API responde 409.

### Regla 2: radio de explosión

`Creeper.validarObjetivoDentroDelRadio(Entidad)` calcula la distancia euclidiana mediante `Entidad.distanciaA`. Si supera el radio, lanza `ObjetivoFueraDeRangoException` y la API responde 422.

`ExplosionService` carga un Creeper, un Jugador y un Aldeano por ID. Prevalida ambos objetivos, aplica daño y persiste la salud de Jugador y Aldeano dentro de una transacción. Un GET posterior muestra el resultado.

Solicitud:

```json
{
  "creeperId": 1,
  "jugadorId": 2,
  "aldeanoId": 3,
  "tiempoTranscurrido": 3
}
```

## Jerarquía de excepciones

```text
RuntimeException
└── ExplosionException
    ├── ExplosionNoPreparadaException
    └── ObjetivoFueraDeRangoException
```

Las dos hijas son no comprobadas. Esta elección mantiene las firmas del dominio enfocadas en el comportamiento y permite rollback transaccional por defecto.

| Excepción o caso | Estado |
|---|---:|
| `ExplosionNoPreparadaException` | 409 |
| `ObjetivoFueraDeRangoException` | 422 |
| Entidad inexistente | 404 |
| ID nulo, tipo incorrecto o estado inválido | 400 |

## Invariantes

- La salud es no negativa; el valor 0 representa una entidad sin vida y puede persistirse y actualizarse.
- Las coordenadas son finitas.
- La velocidad y la experiencia no son negativas.
- El rango es finito y no negativo.
- Daño y tiempo de explosión son positivos.
- Nombre y profesión no son nulos ni blancos.
- Un objetivo y una entidad usada para calcular distancia no son nulos.
- Una actualización no cambia el subtipo persistido.
- La simulación no aplica daño parcial cuando una regla falla.

## H2

Desarrollo usa `jdbc:h2:file:./data/entidades`, usuario `sa`, contraseña vacía, `ddl-auto=update` y consola habilitada. Pruebas usa `jdbc:h2:mem:minecraft-test`, `ddl-auto=create-drop` y consola deshabilitada.

La consola de desarrollo está disponible en `http://localhost:8080/h2-console` cuando la aplicación está activa.

## Colección Insomnia

Archivo: `insomnia/poo08-minecraft.json`.

La colección incluye:

- entorno con URL base y variables para tres IDs;
- POST de Creeper, Jugador y Aldeano;
- GET de colección e ítems;
- PUT y GET de verificación;
- DELETE y GET 404 posterior;
- POST inválido y GET que comprueba que no agregó una fila;
- GET de ID inexistente;
- endpoint `UICapable`;
- explosión válida y GET del estado persistido;
- caso 409 y comprobación sin cambios;
- caso 422 y comprobación de que no hubo daño parcial.

La estructura se validó estáticamente. No se afirma importación o ejecución gráfica desde Insomnia.

## Forma de prueba

1. Ejecutar `cd minecraft` y `./mvnw spring-boot:run`.
2. Verificar `GET /`.
3. Importar la colección.
4. Crear los tres participantes y copiar sus IDs al ambiente.
5. Ejecutar los GET iniciales y el endpoint `UICapable`.
6. Ejecutar una sola vez la explosión válida y consultar la salud persistida.
7. Ejecutar el POST inválido, el ID inexistente y el caso 409; usar el GET siguiente para comprobar que no hubo cambios.
8. Ejecutar PUT para mover al Aldeano fuera del radio y confirmar la actualización con GET.
9. Ejecutar el caso 422 y los dos GET que comprueban que no hubo daño parcial.
10. Ejecutar DELETE, confirmar 404 con el GET posterior y completar la limpieza.
11. Abrir la consola H2 y observar las tablas si se requiere una verificación visual.

## Pruebas automatizadas

```bash
cd minecraft
./mvnw test
```

Resultado documentado:

```text
Tests run: 71, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Las 71 ejecuciones cubren contexto, dominio, jerarquía abstracta, sobrecarga, interfaz, CRUD de subtipos, discriminador obligatorio, invariantes, actualización, eliminación, explosión letal con posterior PUT, validación del tiempo transcurrido, persistencia de explosión, IDs incorrectos, 404, 409, 422 y conservación de la salud ante errores. También se ejecutan con orden aleatorio.

## Entregables

- Código y pruebas en el repositorio.
- [README.md](README.md).
- [BITACORA.md](BITACORA.md).
- [insomnia/poo08-minecraft.json](insomnia/poo08-minecraft.json).
- [LICENSE](LICENSE).
- Esta especificación para Classroom.

El enlace exacto al commit final debe agregarse en Classroom después de confirmar y publicar el estado definitivo. No se usa el SHA anterior porque no incluye cambios todavía no confirmados.
