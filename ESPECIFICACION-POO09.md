# Especificación POO-09 — APIs cruzadas (Minecraft)

## Identificación

- Proyecto: `msferreira-tplp3-2026/minecraft`.
- Autora y rol: María Sol Ferreira, A1.
- Rama de trabajo: `feature/poo-09-apis-cruzadas`.
- Dominio: Minecraft.
- Enunciado: [ejercicio POO-09](https://github.com/alefq/afq-taller-git-2024/blob/main/docs/ejercicio-poo-09-apis-cruzadas-2026-11-16.md).
- Rúbrica: [RUBRICA-ejercicios-lp3-2026](https://github.com/alefq/afq-taller-git-2024/blob/main/docs/RUBRICA-ejercicios-lp3-2026.md).
- Documentación detallada: [README](README.md) y [BITACORA](BITACORA.md).

## Grupo y round robin

| Rol | Integrante | URL base | OpenAPI | Regla que aporta |
|---|---|---|---|---|
| A1 | María Sol Ferreira (este servicio) | `http://100.87.131.50:8080` (local: `http://localhost:8080`) | `http://100.87.131.50:8080/v3/api-docs` (JSON), `/v3/api-docs.yaml`, `/swagger-ui.html` | (1) Un Creeper solo explota si `tiempoTranscurrido >= tiempoExplosion`; si no, 409. (2) Los objetivos deben estar a una distancia euclídea menor o igual al radio; si no, 422 y nadie recibe daño. |
| A2 | Irina | `http://100.87.95.24:8082` | `http://100.87.95.24:8082/v3/api-docs` | `POST /api/minecraft/reglas/mecha-corta`: mecha corta, con un jugador vivo a 3 bloques o menos. A1 consume esta regla. Onda expansiva se aplica dentro del escenario de Irina y no expone un endpoint de validación aparte. |
| A3 | Cecilia | `http://100.122.114.103:8083` | `http://100.122.114.103:8083/v3/api-docs` | `POST /explosiones/validar`: Creeper vivo (422 `CreeperMuertoException`) y detonación única (409 `CreeperYaDetonadoException`). |

Round robin documentado en el README: A1 consulta a Irina y Cecilia; Irina consulta a Cecilia y a María Sol; Cecilia consulta a Irina y a María Sol. Las tres usan las mismas tres URLs.

Queda pendiente documentar los repositorios y hashes de A2 y A3 para reproducir desde un checkout las versiones usadas el 23/11. Las respuestas guardadas corresponden a esa ejecución histórica.

## Escenario

`POST /jugadas/explosion` registra una explosión de Creeper sobre un Jugador y un Aldeano persistentes solo si A1, A2 y A3 la aceptan. El éxito guarda la `Jugada` en H2 y aplica el daño en la misma transacción; si A2 o A3 rechazan, H2 queda igual y la respuesta identifica el origen del rechazo.

Orden: validaciones propias de A1 → A2 → A3 → escritura. Las llamadas HTTP quedan fuera de la transacción. Tras ambas aprobaciones, A1 bloquea la fila del Creeper en H2 y comprueba de nuevo si ya hay una jugada antes de aplicar daño. Un fallo de escritura revierte el daño y la jugada.

## Contrato del escenario

Solicitud de ejemplo. Usar los IDs devueltos al crear las entidades:

```json
{
  "creeperId": 1,
  "jugadorId": 2,
  "aldeanoId": 3,
  "tiempoTranscurrido": 3
}
```

Respuestas:

| Caso | Código | Señal principal |
|---|---:|---|
| A2 y A3 aceptan y se confirma la escritura | 201 | `Jugada` guardada, `Location: /jugadas/{id}` |
| Rechazo de A2 o A3 | 409 | `origen` = `A2` o `A3`, `motivoCompanero`, cuerpo original del compañero |
| A2/A3 no responde, 5xx o 200 sin `aceptada=true` | 502 | `origen` = `A2` o `A3` |
| Regla propia de tiempo | 409 | `ErrorExplosion` sin `origen` |
| A1 detecta una jugada existente al escribir | 409 | `ErrorExplosion` «Creeper ya registrado», sin `origen` remoto |
| Regla propia de radio | 422 | `ErrorExplosion` |
| ID inexistente o dato inválido | 404 / 400 | No se consulta a los compañeros |

`GET /jugadas` y `GET /jugadas/{id}` permiten volver a leer lo persistido. `GET /jugadas/companeros` muestra las URL efectivas de A2 y A3. `GET /entidades/{id}` permite comprobar la salud después del daño.

## Configuración de los clientes

| Variable de entorno | Propiedad | Valor predeterminado |
|---|---|---|
| `SERVER_PORT` | `server.port` | `8080` |
| `A2_BASE_URL` | `companeros.a2.base-url` | `http://100.87.95.24:8082` |
| `A2_RUTA_VALIDACION` | `companeros.a2.ruta-validacion` | `/api/minecraft/reglas/mecha-corta` |
| `A3_BASE_URL` | `companeros.a3.base-url` | `http://100.122.114.103:8083` |
| `A3_RUTA_VALIDACION` | `companeros.a3.ruta-validacion` | `/explosiones/validar` |

## Cómo probarlo

1. Conectar las tres máquinas por Tailscale y arrancar primero A2 y A3, después A1:

   ```bash
   # A2 (Irina), en su máquina
   ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8082

   # A3 (Cecilia), en su máquina
   ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8083

   # A1 (este repositorio)
   cd minecraft
   ./mvnw spring-boot:run
   ```

2. Abrir o importar los OpenAPI de A1, A2 y A3. En A1 también se puede consultar `GET /jugadas/companeros`.

3. Repetir el recorrido de la colección [`insomnia/poo09-apis-cruzadas.json`](insomnia/poo09-apis-cruzadas.json): OpenAPI, preparación, escenario feliz, rechazo de A2, rechazo de A3, reglas propias y, si están disponibles, escenarios de Irina y Cecilia que consumen a A1.

4. Camino feliz por HTTP:

   ```bash
   A1=http://100.87.131.50:8080

   curl -s -X POST "$A1/entidades" -H 'Content-Type: application/json' \
     -d '{"tipo":"creeper","salud":20,"posicionX":0,"posicionY":0,"posicionZ":0,"velocidad":1,"rangoDeteccion":5,"danoAtaque":6,"tiempoExplosion":3}'
   curl -s -X POST "$A1/entidades" -H 'Content-Type: application/json' \
     -d '{"tipo":"jugador","nombre":"Steve","nivelEXP":0,"salud":20,"posicionX":2,"posicionY":0,"posicionZ":0,"velocidad":1}'
   curl -s -X POST "$A1/entidades" -H 'Content-Type: application/json' \
     -d '{"tipo":"aldeano","salud":20,"posicionX":4,"posicionY":0,"posicionZ":0,"velocidad":1,"domesticable":true,"profesion":"Granjero"}'

   read -r -p 'IDs de Creeper, Jugador y Aldeano (separados por espacios): ' creeper_id jugador_id aldeano_id
   curl -i -X POST "$A1/jugadas/explosion" -H 'Content-Type: application/json' \
     -d "$(jq -n --argjson creeper "$creeper_id" --argjson jugador "$jugador_id" --argjson aldeano "$aldeano_id" \
       '{creeperId:$creeper,jugadorId:$jugador,aldeanoId:$aldeano,tiempoTranscurrido:3}')"
   curl -s "$A1/jugadas" | jq .
   ```

   Resultado esperado: 201 con `Location`, jugada leíble por `GET` y salud reducida de los objetivos.

5. Rechazo de A2: crear un Jugador a 4.5 bloques, dentro del radio 5 de A1 pero fuera del radio 3 de Irina, y usarlo en `/jugadas/explosion`. Resultado esperado: 409 con `origen=A2`. Comparar la lista y las saludes con el estado inmediatamente anterior al rechazo. Si ya se ejecutó el feliz, la jugada anterior debe seguir en la lista.

6. Rechazo de A3: ejecutar de nuevo el escenario feliz con el mismo Creeper. A1 envía `yaDetonado=true` porque ya existe una `Jugada`; resultado esperado: 409 con `origen=A3`, sin segunda jugada ni daño.

7. Si una compañera no está en línea, el escenario debe responder 502 con su `origen` y no guardar nada. Para corregir sin depender de que estén encendidas, usar las respuestas guardadas en [`insomnia/respuestas-ejemplo/`](insomnia/respuestas-ejemplo/).

8. Pruebas automatizadas del repositorio:

   ```bash
   cd minecraft
   ./mvnw test
   ./mvnw -Dsurefire.runOrder=random -Dsurefire.runOrder.random.seed=620824 test
   ```

La suite incluye dos pedidos concurrentes con ambas aprobaciones remotas simuladas y un fallo de guardado inyectado después de enviar los cambios a H2. Los resultados de Maven y la evidencia histórica de servicios reales se describen en el README y la bitácora, respectivamente.

## Entregable en Classroom

Adjuntar este archivo `.md` y la URL de GitHub del commit de entrega con su hash completo en el mismo ítem de Classroom.
