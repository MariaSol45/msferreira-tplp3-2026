# Bitácora de uso de inteligencia artificial

## Proyecto

- Proyecto actual: Minecraft - POO-08.
- Repositorio: `msferreira-tplp3-2026`.
- Dominio: Minecraft.
- Finalidad: registrar de forma transparente la asistencia usada durante análisis, implementación, verificación y documentación.

## Herramientas registradas

| Herramienta | Modelo mostrado por la herramienta | Alcance documentado |
|---|---|---|
| OpenCode | `openai/gpt-5.6-sol` | Auditoría del repositorio, análisis de requisitos, cambios controlados, pruebas, migración de H2 y revisión documental. |
| ChatGPT | `GPT-5.6 Sol` | Herramienta consignada en la bitácora de la etapa POO-06 como apoyo conversacional. No se atribuyen acciones posteriores sin evidencia adicional. |

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

## Resumen de prompts

Los prompts de las sesiones POO-08 solicitaron, en palabras resumidas:

1. Auditar el proyecto completo sin modificar archivos y clasificar hallazgos por severidad.
2. Analizar cómo restaurar la abstracción de `Entidad`, exigir subtipos JSON y preservar H2 antes de implementar.
3. Implementar el plan aprobado con respaldo consistente, migración sin pérdida de IDs, pruebas normales y aleatorias, y sin operaciones Git.
4. Completar la auditoría documental contra las consignas oficiales, actualizar README, Mermaid, bitácora, especificación, Insomnia y `.gitignore`, y volver a ejecutar pruebas.

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

## Evidencia y límites

- Las pruebas automatizadas se ejecutaron desde Maven; los conteos finales se registran en README y especificación.
- La aplicación se arrancó contra la base persistente durante la corrección de abstracción y se consultaron los cinco registros existentes.
- La colección Insomnia se validó como JSON y contra los endpoints actuales.
- No se realizó importación gráfica porque `insomnia` e `inso` no están instalados en el entorno.
- No se afirma una ejecución gráfica de la colección.
- No se ejecutaron `git add`, commit ni push durante las auditorías y correcciones descritas en las últimas dos etapas.

## Responsabilidad

La asistencia propuso y aplicó cambios bajo instrucciones explícitas. La revisión final, la decisión de aceptar los cambios, la creación del commit de entrega y la presentación en Classroom corresponden a la autora.
