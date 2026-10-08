# Bitácora de uso de inteligencia artificial

## Proyecto

- Proyecto: Minecraft - POO-06.
- Repositorio: `msferreira-tplp3-2026`.
- Finalidad: documentar de forma transparente la asistencia de herramientas de IA durante el análisis, la implementación, la verificación y la documentación.

## Herramientas utilizadas

| Herramienta | Modelo | Uso registrado |
|---|---|---|
| OpenCode | `openai/gpt-5.6-sol` | Análisis del repositorio, propuesta y aplicación controlada de cambios, ejecución de pruebas y revisión de resultados. El identificador exacto pudo determinarse desde la sesión de OpenCode. |
| ChatGPT | `GPT-5.6 Sol` | Apoyo conversacional durante el análisis, la implementación, las pruebas y la documentación. |

## Resumen de los prompts

Los prompts se redactaron por etapas y solicitaron, en palabras propias, las siguientes tareas:

1. Auditar el proyecto desde cero contra la rúbrica y el enunciado POO-06, sin modificar archivos, verificando código, Git, documentación, arranque, endpoints y pruebas.
2. Reorganizar las clases en un paquete raíz, un paquete `domain` y un paquete `rest.controller`, conservando exactamente la lógica existente.
3. Incorporar sobrecarga real de constructores y métodos, además de reforzar invariantes para salud, coordenadas, velocidad, rango, daño, explosión, experiencia, nombres, profesiones y objetivos.
4. Traducir errores conocidos de entrada a HTTP 400 sin capturar indiscriminadamente errores internos, y agregar pruebas JUnit y MockMvc defendibles.
5. Completar la documentación final, actualizar el diagrama Mermaid y preparar una especificación resumida para Classroom.

Los prompts también establecieron restricciones explícitas: no modificar áreas fuera de cada etapa, no ejecutar `git add`, no crear commits y no hacer push desde la asistencia automatizada.

## Tareas asistidas por IA

- Revisión de la estructura Maven y Spring Boot.
- Comparación del proyecto con los criterios de la rúbrica POO-06.
- Identificación de problemas de paquetes, sobrecarga, invariantes, errores HTTP y cobertura de pruebas.
- Reorganización de paquetes y actualización de imports.
- Implementación de validaciones y sobrecargas en el dominio.
- Implementación del manejo HTTP 400 limitado a `EntidadController`.
- Diseño de pruebas unitarias del dominio y pruebas de integración con MockMvc.
- Ejecución de Maven, arranque de Spring Boot y comprobación de endpoints con `curl`.
- Redacción y actualización de README, Mermaid y especificación de entrega.

## Revisión y responsabilidad

El código y la documentación asistidos por IA fueron revisados por la autora. Las pruebas fueron ejecutadas y sus resultados fueron comprobados. La selección final de cambios, el versionado, los commits y la publicación en el repositorio fueron realizados manualmente por la autora.

La IA se utilizó como herramienta de apoyo y no reemplazó la revisión del funcionamiento ni la responsabilidad sobre la entrega.
