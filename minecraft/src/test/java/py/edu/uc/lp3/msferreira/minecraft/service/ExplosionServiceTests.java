package py.edu.uc.lp3.msferreira.minecraft.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import py.edu.uc.lp3.msferreira.minecraft.domain.exception.ExplosionNoPreparadaException;
import py.edu.uc.lp3.msferreira.minecraft.domain.exception.ObjetivoFueraDeRangoException;
import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService.ResultadoExplosion;

class ExplosionServiceTests {
    private final ExplosionService explosionService = new ExplosionService();

    @Test
    void simulaExplosionConCreeperJugadorYAldeano() {
        ResultadoExplosion resultado = explosionService.simular(3, 2, 4);

        assertAll(
                () -> assertEquals("Explosión de Creeper", resultado.evento()),
                () -> assertEquals(3, resultado.clasesParticipantes().size()),
                () -> assertEquals("Creeper", resultado.clasesParticipantes().get(0)),
                () -> assertEquals("Jugador", resultado.objetivos().get(0).tipo()),
                () -> assertEquals(12, resultado.objetivos().get(0).saludFinal()),
                () -> assertEquals("Aldeano", resultado.objetivos().get(1).tipo()),
                () -> assertEquals(12, resultado.objetivos().get(1).saludFinal())
        );
    }

    @Test
    void aceptaCoordenadasNegativasYCalculaLaDistancia() {
        ResultadoExplosion resultado = explosionService.simular(3, -2, -4);

        assertAll(
                () -> assertEquals(2, resultado.objetivos().get(0).distancia()),
                () -> assertEquals(4, resultado.objetivos().get(1).distancia())
        );
    }

    @Test
    void aplicaDanoAUnObjetivoEnElLimiteDelRadio() {
        ResultadoExplosion resultado = explosionService.simular(3, 2, 5);

        assertAll(
                () -> assertEquals(5, resultado.objetivos().get(1).distancia()),
                () -> assertEquals(12, resultado.objetivos().get(1).saludFinal())
        );
    }

    @Test
    void rechazaExplosionAntesDeCompletarElTiempo() {
        ExplosionNoPreparadaException excepcion = assertThrows(
                ExplosionNoPreparadaException.class,
                () -> explosionService.simular(2, 2, 4)
        );

        assertEquals(
                "El Creeper necesita 3 segundos para explotar, pero solo transcurrieron 2.",
                excepcion.getMessage()
        );
    }

    @Test
    void rechazaObjetivoFueraDelRadioDeExplosion() {
        ObjetivoFueraDeRangoException excepcion = assertThrows(
                ObjetivoFueraDeRangoException.class,
                () -> explosionService.simular(3, 2, 6)
        );

        assertEquals(
                "El objetivo Aldeano está a 6.0 bloques y queda fuera del radio de explosión de 5.0 bloques.",
                excepcion.getMessage()
        );
    }
}
