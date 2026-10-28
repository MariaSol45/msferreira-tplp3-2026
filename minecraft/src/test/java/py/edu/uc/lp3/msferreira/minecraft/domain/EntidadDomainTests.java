package py.edu.uc.lp3.msferreira.minecraft.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EntidadDomainTests {

    @Test
    void constructorCompletoCreaUnCreeperValido() {
        Creeper creeper = new Creeper(20, 0, 0, 0, 1, 10, 5, 3);

        assertEquals(20, creeper.getSalud());
    }

    @Test
    void constructorSobrecargadoCreaUnCreeperValido() {
        Creeper creeper = new Creeper(20, 0, 0, 0, 1, 10, 5);

        assertNotNull(creeper);
        assertEquals(20, creeper.getSalud());
        assertEquals(3, creeper.getTiempoExplosion());
    }

    @Test
    void constructoresSimplificadosUsanValoresPredeterminados() {
        Zombie zombie = new Zombie(20);
        Esqueleto esqueleto = new Esqueleto(20);
        Aldeano aldeano = new Aldeano(20, "Granjero");
        Animal animal = new Animal(20);
        Jugador jugador = new Jugador("Steve");

        assertAll(
                () -> assertEquals("(0.0, 0.0, 0.0)", zombie.getPosicion()),
                () -> assertEquals(1, zombie.getVelocidad()),
                () -> assertEquals(10, zombie.getRangoDeteccion()),
                () -> assertEquals(3, zombie.getDanoAtaque()),
                () -> assertEquals(15, esqueleto.getRangoDeteccion()),
                () -> assertEquals(4, esqueleto.getDanoAtaque()),
                () -> assertEquals("Granjero", aldeano.getProfesion()),
                () -> assertEquals(true, aldeano.isDomesticable()),
                () -> assertEquals(false, animal.isDomesticable()),
                () -> assertEquals(false, animal.isMontar()),
                () -> assertEquals(20, jugador.getSalud()),
                () -> assertEquals(0, jugador.getNivelEXP()),
                () -> assertEquals(1, jugador.getVelocidad())
        );
    }

    @Test
    void constructoresCompletosConservanLosValoresRecibidos() {
        Zombie zombie = new Zombie(30, 1, 2, 3, 4, 12, 6);
        Esqueleto esqueleto = new Esqueleto(25, 4, 5, 6, 3, 18, 7);
        Aldeano aldeano = new Aldeano(22, 7, 8, 9, 2, false, "Herrero");
        Animal animal = new Animal(18, 2, 3, 4, 5, true, true);
        Jugador jugador = new Jugador("Alex", 9, 40, 5, 6, 7, 8);

        assertAll(
                () -> assertEquals(12, zombie.getRangoDeteccion()),
                () -> assertEquals(6, zombie.getDanoAtaque()),
                () -> assertEquals("(1.0, 2.0, 3.0)", zombie.getPosicion()),
                () -> assertEquals(18, esqueleto.getRangoDeteccion()),
                () -> assertEquals(7, esqueleto.getDanoAtaque()),
                () -> assertEquals("Herrero", aldeano.getProfesion()),
                () -> assertEquals(false, aldeano.isDomesticable()),
                () -> assertEquals(true, animal.isDomesticable()),
                () -> assertEquals(true, animal.isMontar()),
                () -> assertEquals("Alex", jugador.getNombre()),
                () -> assertEquals(9, jugador.getNivelEXP()),
                () -> assertEquals(40, jugador.getSalud())
        );
    }

    @Test
    void constructoresRechazanEstadosInvalidos() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Creeper(0, 0, 0, 0, 1, 10, 5, 3)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Creeper(20, Double.NaN, 0, 0, 1, 10, 5, 3)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Creeper(20, 0, 0, 0, -1, 10, 5, 3)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Creeper(20, 0, 0, 0, 1, Double.NaN, 5, 3)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Creeper(20, 0, 0, 0, 1, 10, 0, 3)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Creeper(20, 0, 0, 0, 1, 10, 5, 0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Jugador(" ", 0, 20, 0, 0, 0, 1)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Jugador("Steve", -1, 20, 0, 0, 0, 1)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Aldeano(20, 0, 0, 0, 1, true, " "))
        );
    }

    @Test
    void recibirDanoReduceLaSaludSinSuperarElMinimoCero() {
        Creeper creeper = new Creeper(20, 0, 0, 0, 1, 10, 5);

        creeper.recibirDano(7);
        assertEquals(13, creeper.getSalud());

        creeper.recibirDano(50);
        assertEquals(0, creeper.getSalud());
    }

    @Test
    void metodosAtacarSobrecargadosAplicanDano() {
        Creeper atacante = new Creeper(20, 0, 0, 0, 1, 10, 5);
        Aldeano objetivoDirecto = new Aldeano(20, 0, 0, 0, 1, true, "Granjero");
        Aldeano objetivoConDistancia = new Aldeano(20, 0, 0, 0, 1, true, "Herrero");

        atacante.atacar(objetivoDirecto);
        atacante.atacar(objetivoConDistancia, 5);

        assertAll(
                () -> assertEquals(15, objetivoDirecto.getSalud()),
                () -> assertEquals(15, objetivoConDistancia.getSalud()),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> atacante.atacar(objetivoConDistancia, 11)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> atacante.atacar(null))
        );
    }

    @Test
    void comportamientoSobrescritoFuncionaConPolimorfismo() {
        Entidad creeper = new Creeper(20, 0, 0, 0, 1, 10, 5);
        Entidad zombie = new Zombie(20);
        Entidad esqueleto = new Esqueleto(20);
        Entidad aldeano = new Aldeano(20, "Granjero");
        Entidad animal = new Animal(20);
        Entidad jugador = new Jugador("Steve");

        assertAll(
                () -> assertEquals("El creeper se acerca en silencio y explota en 3 segundos.",
                        creeper.comportamiento()),
                () -> assertEquals("El zombie persigue aldeanos hasta 10.0 bloques para infectarlos.",
                        zombie.comportamiento()),
                () -> assertEquals("El esqueleto dispara flechas que causan 4 puntos de daño.",
                        esqueleto.comportamiento()),
                () -> assertEquals("El aldeano de profesión Granjero comercia con los jugadores.",
                        aldeano.comportamiento()),
                () -> assertEquals("El animal salvaje recorre el entorno y huye de las amenazas.",
                        animal.comportamiento()),
                () -> assertEquals("El jugador Steve interactúa con el mundo con nivel de experiencia 0.",
                        jugador.comportamiento())
        );
    }

    @Test
    void jugadorYAldeanoImplementanUICapable() {
        Jugador jugador = new Jugador("Steve", 0, 20, 0, 0, 0, 1);
        Aldeano aldeano = new Aldeano(20, 0, 0, 0, 1, true, "Granjero");

        assertAll(
                () -> assertInstanceOf(UICapable.class, jugador),
                () -> assertInstanceOf(UICapable.class, aldeano)
        );
    }

    @Test
    void jugadorConservaElMensajeUIPredeterminado() {
        Jugador jugador = new Jugador("Alex", 0, 20, 1, 2, 3, 4);

        assertEquals(
                "Entidad visible en (1.0, 2.0, 3.0) con velocidad 4.",
                jugador.getMensajeUI()
        );
    }

    @Test
    void aldeanoSobrescribeElMensajeUIEIncluyeSuProfesion() {
        Aldeano aldeano = new Aldeano(20, 1, 2, 3, 1, true, "Herrero");

        assertEquals(
                "El aldeano de profesión Herrero está en (1.0, 2.0, 3.0).",
                aldeano.getMensajeUI()
        );
    }

    @Test
    void uiCapablePermiteUsoPolimorfico() {
        UICapable jugador = new Jugador("Steve", 0, 20, 1, 2, 3, 4);
        UICapable aldeano = new Aldeano(20, 4, 5, 6, 2, true, "Bibliotecario");

        assertAll(
                () -> assertEquals("jugador.png", jugador.getImagen()),
                () -> assertEquals("(1.0, 2.0, 3.0)", jugador.getPosicion()),
                () -> assertEquals("Sonido del jugador", jugador.getSonido()),
                () -> assertEquals(4, jugador.getVelocidad()),
                () -> assertEquals("aldeano.png", aldeano.getImagen()),
                () -> assertEquals("El aldeano de profesión Bibliotecario está en (4.0, 5.0, 6.0).",
                        aldeano.getMensajeUI())
        );
    }
}
