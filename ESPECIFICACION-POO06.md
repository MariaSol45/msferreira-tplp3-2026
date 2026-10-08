# Especificación POO-06

## Objetivo

Construir una aplicación Spring Boot con Java 21 que demuestre conceptos fundamentales de Programación Orientada a Objetos y los exponga mediante una API REST verificable.

## Dominio elegido

Minecraft. El modelo representa entidades generales, entidades hostiles, entidades pasivas, jugadores y especializaciones como Creeper, Zombie, Esqueleto, Aldeano y Animal.

## Consignas aplicadas

- Proyecto Spring Boot con Java 21, Maven Wrapper y Spring Web MVC.
- Clase base abstracta con un método abstracto.
- Herencia con múltiples clases hijas.
- Sobreescritura del mismo método con implementaciones diferentes.
- Polimorfismo mediante referencias del tipo padre.
- Constructor simple y constructor sobrecargado.
- Método de dominio sobrecargado.
- Encapsulación de atributos e invariantes de dominio.
- Separación entre dominio y controladores REST.
- Endpoint raíz y endpoint que construye objetos desde la URL.
- Respuestas JSON y errores conocidos de entrada con HTTP 400.
- Pruebas JUnit y MockMvc.
- README, diagrama Mermaid, licencia Apache 2.0 y bitácora de IA.

## Estructura del proyecto

```text
py.edu.uc.lp3.msferreira.minecraft
├── MinecraftApplication.java
├── domain
│   ├── Entidad.java
│   ├── EntidadHostil.java
│   ├── EntidadPasiva.java
│   ├── Jugador.java
│   ├── Zombie.java
│   ├── Creeper.java
│   ├── Esqueleto.java
│   ├── Aldeano.java
│   └── Animal.java
└── rest
    └── controller
        ├── IndexController.java
        └── EntidadController.java
```

## Encapsulación

Los atributos son privados. La salud puede consultarse con `getSalud()`, pero no modificarse directamente. Los cambios de salud pasan por `recibirDano`, que mantiene el valor mínimo en cero.

Los constructores protegen salud, velocidad, coordenadas, rango, daño, tiempo de explosión, experiencia, nombre y profesión. Los métodos de ataque rechazan objetivos nulos y distancias inválidas o fuera de rango.

## Herencia y sobreescritura

`Entidad` es abstracta y declara `comportamiento()`. `EntidadHostil`, `EntidadPasiva` y `Jugador` sobrescriben ese método con la misma firma y resultados diferentes.

Las jerarquías concretas son:

- `Creeper`, `Zombie` y `Esqueleto` extienden `EntidadHostil`.
- `Aldeano` y `Animal` extienden `EntidadPasiva`.
- `Jugador` extiende directamente `Entidad`.

## Polimorfismo

`EntidadController` utiliza una variable `Entidad` para almacenar un `Creeper` o un `Aldeano`. La respuesta se obtiene invocando `comportamiento()` mediante el tipo padre, con selección dinámica de la implementación.

## Sobrecarga

`Creeper` ofrece dos constructores:

```java
Creeper(int salud, double posicionX, double posicionY, double posicionZ,
        int velocidad, double rangoDeteccion, int danoAtaque)

Creeper(int salud, double posicionX, double posicionY, double posicionZ,
        int velocidad, double rangoDeteccion, int danoAtaque,
        int tiempoExplosion)
```

El constructor reducido delega con `this(...)` al constructor completo y utiliza 3 segundos como tiempo de explosión válido.

`EntidadHostil` ofrece dos métodos con el mismo nombre y distintos argumentos:

```java
atacar(Entidad objetivo)
atacar(Entidad objetivo, double distancia)
```

## Endpoints

| Método | Ruta | Resultado |
|---|---|---|
| GET | `/` | Estado general de la API en JSON. |
| GET | `/entidad` | Construye un Creeper o Aldeano desde parámetros y devuelve su comportamiento en JSON. |

Parámetros de `/entidad`:

| Parámetro | Requerido | Regla o valor predeterminado |
|---|---|---|
| `tipo` | Sí | `creeper` o `aldeano`. |
| `salud` | Sí | Entero mayor que 0. |
| `x`, `y`, `z` | No | Coordenadas finitas; valor predeterminado `0`. |
| `velocidad` | No | Entero no negativo; valor predeterminado `1`. |
| `rango` | No | Finito y no negativo; valor predeterminado `10`. |
| `dano` | No | Entero mayor que 0; valor predeterminado `5`. |
| `tiempoExplosion` | No | Entero mayor que 0; valor predeterminado `3`. |
| `profesion` | No | Texto no vacío; valor predeterminado `Granjero`. |

## Ejemplos de prueba

API disponible:

```bash
curl -i "http://localhost:8080/"
```

Creeper válido, resultado esperado `200 OK`:

```bash
curl -i "http://localhost:8080/entidad?tipo=creeper&salud=20"
```

Aldeano válido, resultado esperado `200 OK`:

```bash
curl -i "http://localhost:8080/entidad?tipo=aldeano&salud=20&profesion=Herrero"
```

Tipo inválido, resultado esperado `400 Bad Request`:

```bash
curl -i "http://localhost:8080/entidad?tipo=zombie&salud=20"
```

Invariante inválida, resultado esperado `400 Bad Request`:

```bash
curl -i "http://localhost:8080/entidad?tipo=creeper&salud=0"
curl -i "http://localhost:8080/entidad?tipo=creeper&salud=20&x=NaN"
```

## Ejecución

Desde la raíz del repositorio:

```bash
cd minecraft
./mvnw spring-boot:run
```

## Pruebas automatizadas

```bash
cd minecraft
./mvnw clean test
```

Resultado esperado para el estado documentado:

```text
Tests run: 21, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Las pruebas cubren carga de contexto, constructores válidos e inválidos, daño, salud mínima, sobrecarga, sobreescritura, polimorfismo, JSON, endpoints válidos y respuestas HTTP 400.

## Archivos de entrega

- Descripción completa y diagrama: [README.md](README.md).
- Bitácora de IA: [BITACORA.md](BITACORA.md).
- Licencia Apache 2.0: [LICENSE](LICENSE).

## Enlace al commit de entrega

El enlace exacto al commit de entrega se proporcionará en Classroom después de realizar el último commit y push. No se incluye en este archivo un SHA autorreferencial porque el commit cambiaría al modificar la propia especificación.
