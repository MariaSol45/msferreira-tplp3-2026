# Bitácora de uso de inteligencia artificial

## Proyecto

- Proyecto actual: Minecraft - POO-09 (APIs cruzadas), sobre POO-08.
- Repositorio: `msferreira-tplp3-2026`.
- Dominio: Minecraft.
- Finalidad: registrar de forma transparente la asistencia usada durante análisis, implementación, verificación y documentación.

## Herramientas registradas

| Herramienta | Modelo mostrado por la herramienta | Alcance documentado |
|---|---|---|
| OpenCode | `openai/gpt-5.6-sol` | Auditoría del repositorio, análisis de requisitos, cambios controlados, pruebas, migración de H2 y revisión documental. |
| ChatGPT | `GPT-5.6 Sol` | Herramienta consignada en la bitácora de la etapa POO-06 como apoyo conversacional. No se atribuyen acciones posteriores sin evidencia adicional. |
| OpenCode | `openai/gpt-6.2-sol` | Cierre de POO-08: verificación de la base Git, documentación, colección, pruebas y arranque aislado. |
| Claude Code (app de escritorio) | `claude-opus-5-5` (Claude Opus 5.5) | POO-09: cierre de la última observación de POO-08, OpenAPI, clientes a A2 y A3, escenario, Insomnia, pruebas y documentación. |
| OpenCode | `opencode/big-pickle` | Integración de POO-09 con los OpenAPI reales de A2 y A3, adaptación de clientes y cuerpo, `yaDetonado` desde H2, externalización de URLs y puerto, pruebas, documentación y verificación. |

## Cronología verificable

La cronología combina el historial Git con las acciones observadas en las sesiones de OpenCode. Cuando el historial demuestra un resultado pero no quién operó la herramienta, se describe el cambio sin atribuir su autoría.

### 24 de septiembre de 2026 - base del dominio

Los commits `6e0f986` y `6005b23` incorporaron el modelo Minecraft, un controller polimórfico y la respuesta del endpoint raíz.

### 7 de octubre de 2026 - POO-06

Los commits `a18536b`, `1db952e`, `4609b7b` y `d36ecf2` registran:

- separación entre dominio y controllers;
- constructores y métodos sobrecargados;
- invariantes del dominio;
- pruebas JUnit y MockMvc;
- README, bitácora y especificación POO-06.

La bitácora de esa etapa registró asistencia para revisar paquetes, sobrecarga, sobreescritura, validaciones, respuestas HTTP 400, pruebas y documentación.

### 14 de octubre de 2026 - protocolo de dominio

El commit `aa3a05c` agregó `UICapable`, sus implementaciones en Jugador y Aldeano, el endpoint `/ui` y pruebas de dominio e integración.

### 28 de octubre de 2026 - H2 y CRUD

Los commits `2b8855a` y `51864a0` registran:

- configuración de Spring Data JPA y H2;
- `EntidadRepository` y `EntidadService`;
- CRUD REST de `/entidades`;
- ampliación del endpoint transitorio `/entidad`;
- endpoint `/entidad/atacar` para observar sobrecarga;
- pruebas de persistencia y comportamiento HTTP.

### 29 de octubre de 2026 - explosiones y excepciones

El commit `8ab1de1` incorporó:

- distancia euclidiana y reglas de explosión en el dominio;
- `ExplosionException` con dos clases hijas;
- handlers 409 y 422;
- servicio y controller de explosión;
- pruebas de dominio, servicio e integración;
- configuración H2 en memoria para pruebas.

### 5 de noviembre de 2026 - persistencia polimórfica y colección POO-08

El commit `120fdc8` registró:

- persistencia JPA de Creeper, Jugador y Aldeano;
- discriminación JSON de subtipos;
- simulación por IDs de participantes persistidos;
- persistencia de la salud resultante;
- validación de IDs ausentes, inexistentes o de tipo incorrecto;
- ampliación de pruebas CRUD y de explosión;
- colección `insomnia/poo08-minecraft.json`.

### 5 de noviembre de 2026 - auditoría final

OpenCode realizó una auditoría contra el enunciado POO-08 y la rúbrica oficial. Se revisaron código, JPA, Jackson, CRUD, reglas, excepciones, Insomnia, documentación e historial. Se ejecutaron 65 pruebas en el estado auditado, primero en orden normal y luego con orden aleatorio. La auditoría detectó que `Entidad` seguía concreta, que el discriminador podía omitirse y que la documentación permanecía centrada en POO-06.

### 5 de noviembre de 2026 - corrección de la raíz abstracta

Después de aprobar un plan explícito, OpenCode realizó estas acciones:

- verificó que no hubiera aplicación Java, puerto 8080 ni descriptor abierto sobre H2;
- creó el respaldo `/tmp/opencode/entidades-before-abstract-20261105.mv.db` fuera del repositorio;
- verificó la copia con SHA-256 `18342222d95737dad9a8a70212ac5b0176a18b3d435cdb29fb5f1dd6887abb85`;
- convirtió `Entidad` y `comportamiento()` en abstractos;
- agregó `EntidadGenerica` para conservar dos filas históricas sin subtipo;
- hizo obligatorio `tipo` en POST y PUT;
- agregó pruebas para tipo ausente y desconocido;
- migró únicamente los IDs 8 y 9 a `ENTIDADES_GENERICAS`, sin eliminarlos ni renumerarlos;
- arrancó la aplicación contra H2 persistente y consultó por GET los IDs 8, 9, 11, 12 y 13;
- ejecutó 68 pruebas en orden normal y aleatorio, todas aprobadas.

### 5 de noviembre de 2026 - auditoría documental POO-08

La solicitud pidió contrastar nuevamente el proyecto con las fuentes oficiales y corregir solo afirmaciones comprobables. OpenCode:

- volvió a consultar el enunciado POO-08 y la rúbrica;
- auditó endpoints, configuración, jerarquías, servicios, handlers y pruebas;
- reescribió el README para el estado POO-08;
- creó una especificación POO-08 separada de la histórica POO-06;
- completó las respuestas de “Qué hay que pensar”;
- agregó verificaciones faltantes a la colección Insomnia;
- excluyó `minecraft/bin/` en `.gitignore` sin borrar la carpeta local;
- validó estáticamente la colección, sin afirmar una importación gráfica.

### 9 de noviembre de 2026 - estado evaluado

El commit [`8cd5f741f377c8d22e323d818658807c440e889c`](https://github.com/MariaSol45/msferreira-tplp3-2026/commit/8cd5f741f377c8d22e323d818658807c440e889c) permitió salud cero como estado posterior al daño, distinguió `tiempoTranscurrido` ausente o negativo y elevó la suite a 71 ejecuciones. Ese es el commit revisado por el profesor y se conserva como referencia histórica.

### 18 de noviembre de 2026 - auditoría de la devolución docente

OpenCode con `openai/gpt-5.6-sol` auditó el commit evaluado, el código actual, README, especificación, bitácora, pruebas, reportes Maven y colección Insomnia. También contrastó los límites de salud de Jugador, Creeper y Aldeano con fuentes de Minecraft vanilla.

La auditoría confirmó que las 71 pruebas eran verdes, pero reforzaban supuestos incorrectos:

- aceptaban Jugador con salud 30, 35 y 40;
- no probaban POST con salud 0 o 5000 ni los límites 1 y 20;
- exigían que constructores y entidades lanzaran excepciones;
- invocaban daño, tiempo y radio directamente en el dominio;
- no verificaban la ausencia de setters públicos;
- no comprobaban físicamente las tablas `JOINED`;
- `/ui` solo usaba instancias transitorias.

### 18 de noviembre de 2026 - corrección integral

Se aplicaron los siguientes cambios sin crear otro proyecto y sin modificar la H2 de desarrollo:

- las entidades pasaron a representar estado y conservar comportamiento polimórfico descriptivo;
- las validaciones, copia de estado, ataque y daño se trasladaron a `EntidadService`;
- tiempo, radio, estado de participantes y excepciones de explosión se trasladaron a `ExplosionService`;
- se eliminó `setSalud` y se agregó un escritor package-private para cambios ya validados;
- POST distingue salud inicial positiva de salud posterior al daño, donde cero es válido;
- Jugador, Creeper y Aldeano usan máximo vanilla de 20;
- se agregó `findBySaludGreaterThan(0)` con pruebas;
- `/ui?id=...` usa Jugador o Aldeano persistido y conserva el contrato transitorio anterior;
- se ampliaron pruebas de servicio, MockMvc y persistencia `JOINED`;
- se actualizaron README, especificación e Insomnia.

La devolución específica pedía reglas en `service`, mientras el enunciado general original hablaba de reglas en métodos del dominio. Se dio prioridad a la devolución docente más reciente. La sobrecarga de ataque se preservó en `EntidadService`, y las firmas de constructores, herencia, abstracción, sobreescritura e interfaz se conservaron.

### 18 de noviembre de 2026 - respaldo adversarial y cierre documental

El [respaldo `4e4c38cdeab75b5edfa164055d206d972171754c`](https://github.com/MariaSol45/msferreira-tplp3-2026/commit/4e4c38cdeab75b5edfa164055d206d972171754c) incorpora las correcciones posteriores a la suite de 83 pruebas: ID no nulo rechazado en POST/PUT, campos obligatorios de JSON, coerción numérica deshabilitada, exclusión de tipos transitorios del CRUD, distancia estable y rollback probado con `flush` y consultas JDBC antes y después del fallo.

El cierre retomó ese respaldo en `main`, contrastó los archivos de entrega con el código y añadió a Insomnia regresiones de ID en POST, salud decimal y PUT incompleto. El 404 se obtiene después de eliminar un registro creado por el propio recorrido.

- `./mvnw test` y `./mvnw -Dsurefire.runOrder=random -Dsurefire.runOrder.random.seed=620824 test`: 96 pruebas por ejecución, 0 fallos, 0 errores y 0 omitidas.
- Spring Boot arrancó con `jdbc:h2:mem:poo08-smoke`, en un puerto local asignado dinámicamente. Se ejecutaron por HTTP las 31 solicitudes del export y un GET final verificó que no quedaran registros. Importación gráfica: no verificada.
- El SHA-256 del archivo H2 de desarrollo se comparó antes y después del cierre para comprobar que su contenido no cambió.

### 21 de noviembre de 2026 - POO-09, APIs cruzadas

Sesión con Claude Code (Claude Opus 5.5). Prompt, en palabras resumidas: "Siguiendo con el repositorio y el servicio de POO-08, publicar el OpenAPI, consumir a dos compañeras del grupo (A2 Irina, A3 Cecilia), exponer un escenario que guarde la jugada en H2 solo si las dos aceptan e indique de quién fue el rechazo, y cerrar las observaciones de la corrección de POO-08". Se adjuntó el PDF de la corrección de POO-08.

Lo que se hizo, en commits separados sobre la rama `feature/poo-09-apis-cruzadas` (creada desde `main` en `d14f1b7`):

1. `fix(poo-08)`: se revisó cada observación de la corrección contra el código. La mayoría ya estaba cerrada en `d14f1b7`; faltaba que las excepciones de las reglas salieran de `domain.exception`, y se movieron a `service.exception`. Se quitaron frases de estado previas a la entrega en README y especificación.
2. `feat(openapi)`: se tomó el trabajo de springdoc que estaba sin terminar en `feature/RESTClient` y se documentó el cuerpo de error de 409 y 422.
3. `feat(openapi)`: `POST /explosiones/validar`, para que el grupo consuma mis dos reglas sin modificar mi H2.
4. `feat(clientes)`: `ClienteCompanero` (RestClient) con `ClienteA2` y `ClienteA3`, URL y ruta configurables y traducción de errores a mi jerarquía.
5. `feat(escenario)`: `POST /jugadas/explosion`, la entidad `Jugada` y `CompaneroExceptionHandler`.
6. `test(insomnia)`: colección POO-09 y respuestas de ejemplo.
7. `docs`: README y esta bitácora.

Decisiones:

- Irina y Cecilia todavía no habían publicado su OpenAPI. Por eso el contrato que se les envía es, por ahora, el mismo cuerpo de mi `/explosiones/validar`, y el único lugar a adaptar cuando publiquen es `armarCuerpo` de cada cliente más la configuración.
- Orden: mis reglas, A2 y después A3; las llamadas HTTP van fuera de la transacción y la escritura va al final.
- Un 4xx del compañero es un rechazo (409 con `origen`); sin respuesta o con 5xx es "no disponible" (502 con `origen`).

Verificación:

- `./mvnw test` y `./mvnw -Dsurefire.runOrder=random -Dsurefire.runOrder.random.seed=620824 test`: 120 pruebas, 0 fallos, 0 errores, 0 omitidas.
- Se arrancó el jar tres veces (8080 como A1, y 8082 y 8083 como copias de mi servicio haciendo de A2 y A3), con H2 en memoria. Por HTTP: el camino feliz respondió 201 y GET mostró la jugada y la salud 14; con 8083 detenido respondió 502 con origen A3, y con 8082 detenido 502 con origen A2, sin cambios en H2; `/v3/api-docs`, `/v3/api-docs.yaml` y `/swagger-ui.html` respondieron 200.
- No se probó contra los servicios reales de Irina y Cecilia porque todavía no existían. La colección de Insomnia se validó como JSON; no se importó en la aplicación gráfica.
- No se tocó la base H2 de desarrollo (`minecraft/data/`).

### 21 de noviembre de 2026 - integración con los OpenAPI reales de A2 y A3

Sesión con OpenCode (`opencode/big-pickle`). Se pidió completar y verificar la integración en la rama `feature/poo-09-apis-cruzadas`, usando las IP de Tailscale ya informadas para A1 (100.87.131.50:8080), A2 (100.87.95.24:8082) y A3 (100.122.114.103:8083).

Antes de tocar código se revisaron el estado Git (rama y commits), el código existente y los dos OpenAPI reales con `curl` a `/v3/api-docs`. De ellos salieron los esquemas efectivos:

- A2: `POST /api/minecraft/reglas/mecha-corta`, `MechaCortaRequest` con `saludCreeper`, `creeper` y `objetivos`; respuestas 200 `ReglaResponse` (`aceptada`, `regla`, `objetivo`, `distancia`, `mensaje`) y errores 400/409/422 con `error`, `codigo` y `mensaje`.
- A3: `POST /explosiones/validar`, `ValidacionExplosionRequest` con `saludCreeper` y `yaDetonado`; errores con `tipo` (`CreeperMuertoException`, `CreeperYaDetonadoException`).

Cambios aplicados:

1. `application.properties`: `server.port=${SERVER_PORT:8080}` y URLs/rutas de A2 y A3 con las IP de Tailscale como valores predeterminados; las @Value de los clientes calzan con esos valores.
2. `ExplosionService.describir` devuelve `EstadoExplosion`: cuerpo de mis reglas más `saludCreeper` y `yaDetonado`. `yaDetonado` se obtiene con `JugadaRepository.existsByCreeperId`, sin agregar columnas al modelo persistente.
3. `ClienteA2` arma `MechaCortaRequest`; `ClienteA3` arma `ValidacionExplosionRequest`. `ClienteCompanero` interpreta `aceptada`, `codigo`/`tipo` y `mensaje`, exige `aceptada=true` en 2xx, conserva el cuerpo original en el rechazo y registra pasos en DEBUG sin incluir secretos ni cuerpos de pedido.
4. `ErrorCompanero` incorpora `motivoCompanero`; `JugadaService` mantiene el orden mis reglas → A2 → A3 → escritura transaccional.
5. Pruebas: 10 en `ClienteCompaneroTests` y 12 en `JugadaEscenarioIntegrationTests`, con servidores HTTP locales. `POST /explosiones/validar` quedó intacto.
6. README (mapa real, cuatro diagramas de secuencia, variables de entorno y comandos curl), Insomnia (IP Tailscale y cuerpos reales de A2/A3) y esta bitácora.

Verificación:

- `./mvnw test` y `./mvnw -Dsurefire.runOrder=random -Dsurefire.runOrder.random.seed=620824 test`: 132 pruebas, 0 fallos, 0 errores, 0 omitidas en cada ejecución.
- Arranque del jar smoke con H2 en memoria y puerto efímero: `/`, `/jugadas/companeros` y `/v3/api-docs` respondieron 200; las URL efectivas son las de Tailscale.
- Llamadas directas reales: A2 aceptó un jugador a 2 bloques (200) y rechazó uno a 6 (422 `fuera_de_alcance`); A3 aceptó `saludCreeper=20, yaDetonado=false` (200) y rechazó `saludCreeper=0` (422 `CreeperMuertoException`).
- Escenario real contra A2: con el jugador a 4,5 bloques A2 devolvió 422 y A1 respondió 409 con `origen=A2`, `estadoCompanero=422`, `motivoCompanero=fuera_de_alcance` y el cuerpo original, sin guardar jugada ni aplicar daño.
- Incidente externo: poco después, A3 dejó de responder (tiempo de espera agotado en `curl` y en Java); el escenario respondió correctamente 502 con `origen=A3` y sin cambios en H2. Para el camino feliz con A3 real queda repetir la prueba cuando Cecilia vuelva a publicar el servicio.
- Diagramas UML de secuencia en Mermaid: se completaron los cinco que pide la rúbrica (camino feliz, rechazo de A2 con 422, rechazo de A3 con 409, reglas propias 400/409/422 y fallo de comunicación 502) con índice enlazado desde README. Cada diagrama muestra solo A1, A2, A3 y H2 cuando intervienen, y una tabla distingue lo verificado de lo pendiente. El camino feliz real A1 → A2 → A3 queda registrado como **pendiente porque Cecilia no responde**; no se presenta como probado con los tres servicios reales.
- No se ejecutaron `git add`, commit, push, merge ni cambios de rama. No se tocó la H2 de desarrollo.

### 23 de noviembre de 2026 - continuación de la verificación POO-09

Sesión con OpenCode (`opencode/big-pickle`). Prompt resumido: retomar solo los pendientes de POO-09 en `feature/poo-09-apis-cruzadas`, verificar A1/A2/A3 por HTTP, comprobar persistencia y rechazos, revisar Insomnia y los diagramas Mermaid, y estimar los requisitos de la rúbrica sin repetir la auditoría anterior ni tocar reglas que funcionan.

Estado previo: A2 respondía en `http://100.87.95.24:8082`; A3 no respondía en `http://100.122.114.103:8083`; A1 no estaba levantada. Se revisaron rama, commits, código del escenario y documentación antes de ejecutar.

Acciones y verificación:

- Pruebas específicas: `ClienteCompaneroTests` (10) y `JugadaEscenarioIntegrationTests` (12), 22/22 sin fallos.
- Se arrancó A1 desde el jar con H2 en memoria, sin tocar `minecraft/data/`, y se confirmó por Tailscale (`100.87.131.50:8080`). `GET /jugadas/companeros` mostró las URLs reales de A2 y A3.
- A2 real aceptó a 2 bloques (200) y rechazó a 6 (422 `fuera_de_alcance`). El escenario con Jugador a 4.5 bloques pasó mis reglas, recibió el 422 de A2 y devolvió 409 con `origen=A2`, `estadoCompanero=422` y `motivoCompanero=fuera_de_alcance`; `GET /jugadas` quedó vacío y las saludes en 20.
- A3 real sigue caída: el escenario devolvió 502 con `origen=A3` y sin cambios en H2. Con A2 apuntado a `localhost:9` se reprodujo el 502 con `origen=A2`, también sin cambios.
- Como A3 no responde, se levantó un servidor HTTP local en 8083 con el contrato leído de su OpenAPI. Con A2 real: camino feliz 201 con `Location`, jugada leíble por GET y salud 14; repetir con el mismo Creeper envió `yaDetonado=true`, A3 local respondió 409 `CreeperYaDetonadoException` y A1 devolvió 409 con `origen=A3`, sin segunda jugada ni daño. Sigue pendiente repetir estos dos casos con Cecilia real.
- Insomnia: se agregó `jugador_lejano_id` y el POST que lo crea; el rechazo de A2 usa ese Jugador en vez del que A2 acepta. Las cinco respuestas de ejemplo se reemplazaron por cuerpos copiados de ejecuciones reales del 23/11 y se validó el JSON.
- README: se ajustaron los cinco diagramas (A3 fuera del diagrama de rechazo de A2, H2 con/sin INSERT y UPDATE, códigos 200/422/409/201/502) y la tabla de estado; se documentó la evidencia y lo pendiente.

No se ejecutaron `git add`, commit, push, merge ni cambios de rama. Tampoco se modificaron reglas de negocio.

### 23 de noviembre de 2026 - migración a Log4j2 y trazas DEBUG

Sesión con OpenCode (`opencode/big-pickle`). Prompt resumido: el profesor exige Log4j2 y logs DEBUG en todos los pasos de la integración; si el proyecto usa Logback, migrar con el starter oficial; cubrir inicio/validación local, preparación y envío a A2/A3, respuestas 200/400/409/422, conexión/timeout/502, compañera que rechaza, decisión de persistencia y confirmación en H2; sin cambiar reglas de negocio ni ejecutar operaciones Git.

Diagnóstico: `dependency:tree` mostró `spring-boot-starter-logging`, `logback-classic` y `log4j-to-slf4j`. Es decir, el código usaba SLF4J con backend Logback; el puente no era Log4j2 implementation. El enunciado y la rúbrica públicos no mencionan logging, pero se sigue la instrucción directa del profesor comunicada por el usuario.

Cambios:

- `pom.xml`: exclusión de `spring-boot-starter-logging` en los starters de Spring Boot y alta de `spring-boot-starter-log4j2`.
- `ClienteCompanero.java`: logger nativo Log4j2, logs de preparación por esquema, URL/envío, estado HTTP recibido, 4xx interpretado, 5xx, respuesta sin `aceptada=true`, conexión/timeout y traducción a 502. Se conserva el cuerpo original del compañero para la respuesta de error, pero no se loguea.
- `ClienteA2.java` y `ClienteA3.java`: descripción del esquema del cuerpo solo para el log, sin valores.
- `JugadaService.java`: inicio de jugada, estado leído, validación local, consulta a cada compañera, decisión de no persistir al fallar, decisión de persistir y confirmación del `jugadaId` guardado en H2.
- `CompaneroExceptionHandler.java`: logs del 409 y 502 finales con rol y compañero.
- `application.properties`: `INFO` general y paquete de la aplicación en `INFO`, activable con `LOGGING_LEVEL_PY_EDU_UC_LP3_MSFERREIRA_MINECRAFT=DEBUG`.
- `README.md`: documentación de la migración, la variable DEBUG y un ejemplo de traza.

Verificación:

- `dependency:tree` posterior: sin `spring-boot-starter-logging`, sin Logback y sin `log4j-to-slf4j`; presentes `spring-boot-starter-log4j2`, `log4j-core` y `log4j-slf4j2-impl`.
- Tests específicos: `ClienteCompaneroTests` (10) y `JugadaEscenarioIntegrationTests` (12), 22/22 sin fallos.
- `JugadaEscenarioIntegrationTests` con `DEBUG` mostró 200 aceptados, 400/409/422 rechazados con motivo, 503 traducido a 502, rechazo local, decisión de no persistir, decisión de persistir y guardado confirmado en H2.
- Suite completa: `./mvnw -q test`, 132/132 sin fallos, errores ni omitidas.

Sigue pendiente repetir el camino feliz y el rechazo con A3 real cuando Cecilia vuelva a publicar el servicio. No se ejecutaron `git add`, commit, push, merge ni cambios de rama; no se modificaron reglas de negocio ni la H2 de desarrollo.

### 23 de noviembre de 2026 - prueba con los tres servicios reales y cierre documental

Sesión con Claude Code (Claude Opus 5.5). Prompt resumido: verificar hasta dónde está POO-09 contra el enunciado, actualizar el Markdown de entrega y el README, y explicar cómo levantar y probar los servicios de Irina y Cecilia.

- `./mvnw test`: 132/132 sin fallos, errores ni omitidas.
- Irina y Cecilia estaban en línea por Tailscale. Se leyeron sus OpenAPI y `GET /api/minecraft/jugadas/companeras` de Irina, que confirma el mismo round robin con las mismas tres URLs.
- A1 se arrancó con H2 en memoria en el puerto 18080, sin tocar la base de desarrollo, y se probó contra los servicios reales: feliz 201 (`Location: /jugadas/1`, salud 14); rechazo de A3 409 `CreeperYaDetonadoException` con `origen=A3` y sin segunda jugada; rechazo de A2 422 `fuera_de_alcance` traducido a 409 con `origen=A2` y salud 20.
- Se reemplazaron `01-feliz-201.json` y `03-rechazo-a3-409.json` por estas respuestas reales.
- README: se citan las dos reglas de Irina y de Cecilia, se documenta el round robin comprobado, se quitan los pendientes con A3 real y las frases de estado sobre el commit y Classroom, y se explica qué se hizo cuando A3 no respondía.
- Se actualizó el Markdown de entrega de Classroom.

- Pruebas cruzadas desde la máquina de A1: escenario de Irina (201, jugada 1 aceptada por Cecilia y A1) y escenario de Cecilia (503 inicial por su URL `localhost:8082`, corregido por ella; 422 por un Creeper dañado por otra prueba cercana; feliz 201, emboscada 2 aceptada por Irina y A1). Se agregó la carpeta 07 a la colección de Insomnia con esas pruebas y se documentó en el README.

No se modificó código Java ni reglas de negocio.

## Resumen de prompts

Los prompts de las sesiones POO-08 solicitaron, en palabras resumidas:

1. Auditar el proyecto completo sin modificar archivos y clasificar hallazgos por severidad.
2. Analizar cómo restaurar la abstracción de `Entidad`, exigir subtipos JSON y preservar H2 antes de implementar.
3. Implementar el plan aprobado con respaldo consistente, migración sin pérdida de IDs, pruebas normales y aleatorias, y sin operaciones Git.
4. Completar la auditoría documental contra las consignas oficiales, actualizar README, Mermaid, bitácora, especificación, Insomnia y `.gitignore`, y volver a ejecutar pruebas.
5. Corregir integralmente la devolución del profesor sin modificar historial Git ni la base H2 de desarrollo, con auditoría previa, reglas vanilla, pruebas de persistencia y verificación final.
6. Retomar solo el cierre POO-08 de `main` en `4e4c38c`: actualizar documentación e Insomnia, probar en orden normal y aleatorio, verificar un arranque en memoria y estimar los ocho criterios con evidencia.

No se reconstruyen textualmente prompts de sesiones anteriores que no quedaron registrados. Los commits se usan para describir resultados verificables, no para inferir quién ejecutó cada comando.

## Decisiones técnicas asistidas

- Mantener `Entidad` como raíz JPA abstracta con estrategia `JOINED`.
- Introducir `EntidadGenerica` como compatibilidad para filas históricas que antes representaban directamente a `Entidad`.
- Usar `tipo` como discriminador Jackson obligatorio y no como columna JPA.
- Mantener el CRUD polimórfico sobre `EntidadRepository`.
- Implementar PUT como reemplazo del mismo subtipo y rechazar cambios de clase.
- Prevalidar ambos objetivos antes de aplicar daño y ejecutar la simulación dentro de una transacción.
- Usar excepciones no comprobadas para reglas del dominio y handlers separados para 409 y 422.
- Separar la H2 persistente de la H2 en memoria usada por pruebas.
- Distinguir salud inicial de salud posterior al daño.
- Mantener máximo 20 para Jugador, Creeper y Aldeano, sin inventar máximo para tipos genéricos.
- Centralizar reglas en servicios según la devolución docente y dejar entidades sin callbacks ni validaciones.
- Conservar el contrato REST y agregar `id` opcional a `/ui` para usar entidades persistidas.

## Evidencia y límites

- Las pruebas automatizadas se ejecutaron desde Maven; los conteos finales se registran en README y especificación.
- La aplicación se arrancó contra la base persistente durante la corrección de abstracción y se consultaron los cinco registros existentes.
- La colección Insomnia se validó como JSON y contra los endpoints actuales.
- No se realizó importación gráfica porque `insomnia` e `inso` no están instalados en el entorno.
- No se afirma una ejecución gráfica de la colección.
- No se ejecutaron `git add`, commit ni push durante las auditorías y correcciones descritas en las últimas dos etapas.

## Responsabilidad

La asistencia propuso y aplicó cambios bajo instrucciones explícitas. La revisión final, la decisión de aceptar los cambios, la creación del commit de entrega y la presentación en Classroom corresponden a la autora.
