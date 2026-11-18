package py.edu.uc.lp3.msferreira.minecraft.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import py.edu.uc.lp3.msferreira.minecraft.domain.Aldeano;
import py.edu.uc.lp3.msferreira.minecraft.domain.Creeper;
import py.edu.uc.lp3.msferreira.minecraft.domain.Jugador;
import py.edu.uc.lp3.msferreira.minecraft.domain.exception.ExplosionNoPreparadaException;
import py.edu.uc.lp3.msferreira.minecraft.domain.exception.ObjetivoFueraDeRangoException;
import py.edu.uc.lp3.msferreira.minecraft.repository.EntidadRepository;
import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService.ResultadoExplosion;
import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService.SolicitudExplosion;

@SpringBootTest
class ExplosionServiceTests {
    @Autowired
    private ExplosionService explosionService;

    @Autowired
    private EntidadRepository entidadRepository;

    private Creeper creeper;
    private Jugador jugador;
    private Aldeano aldeano;

    @BeforeEach
    void limpiarBaseDeDatos() {
        entidadRepository.deleteAll();
    }

    @Test
    void simulaYPersisteExplosionConCreeperJugadorYAldeano() {
        guardarParticipantes(2, 4);

        ResultadoExplosion resultado = explosionService.simular(
                creeper.getId(), jugador.getId(), aldeano.getId(), 3
        );

        Jugador jugadorPersistido = (Jugador) entidadRepository.findById(jugador.getId()).orElseThrow();
        Aldeano aldeanoPersistido = (Aldeano) entidadRepository.findById(aldeano.getId()).orElseThrow();
        assertAll(
                () -> assertEquals("Explosión de Creeper", resultado.evento()),
                () -> assertEquals(creeper.getId(), resultado.creeperId()),
                () -> assertEquals(jugador.getId(), resultado.objetivos().get(0).id()),
                () -> assertEquals(12, resultado.objetivos().get(0).saludFinal()),
                () -> assertEquals(aldeano.getId(), resultado.objetivos().get(1).id()),
                () -> assertEquals(12, resultado.objetivos().get(1).saludFinal()),
                () -> assertEquals(12, jugadorPersistido.getSalud()),
                () -> assertEquals(12, aldeanoPersistido.getSalud()),
                () -> assertEquals(20, entidadRepository.findById(creeper.getId()).orElseThrow().getSalud())
        );
    }

    @Test
    void rechazaSolicitudNulaEnElServicio() {
        assertThrows(IllegalArgumentException.class,
                () -> explosionService.simular((SolicitudExplosion) null));
    }

    @Test
    void aceptaCoordenadasNegativasYCalculaLaDistancia() {
        guardarParticipantes(-2, -4);

        ResultadoExplosion resultado = explosionService.simular(
                creeper.getId(), jugador.getId(), aldeano.getId(), 3
        );

        assertAll(
                () -> assertEquals(2, resultado.objetivos().get(0).distancia()),
                () -> assertEquals(4, resultado.objetivos().get(1).distancia())
        );
    }

    @Test
    void aceptaUnObjetivoEnElLimiteDelRadio() {
        guardarParticipantes(2, 5);

        ResultadoExplosion resultado = explosionService.simular(
                creeper.getId(), jugador.getId(), aldeano.getId(), 3
        );

        assertEquals(5, resultado.objetivos().get(1).distancia());
        assertEquals(12, entidadRepository.findById(aldeano.getId()).orElseThrow().getSalud());
    }

    @Test
    void noModificaSaludCuandoLaExplosionNoEstaPreparada() {
        guardarParticipantes(2, 4);

        ExplosionNoPreparadaException excepcion = assertThrows(
                ExplosionNoPreparadaException.class,
                () -> explosionService.simular(creeper.getId(), jugador.getId(), aldeano.getId(), 2)
        );

        assertAll(
                () -> assertEquals(
                        "El Creeper necesita 3 segundos para explotar, pero solo transcurrieron 2.",
                        excepcion.getMessage()
                ),
                () -> assertEquals(20, entidadRepository.findById(jugador.getId()).orElseThrow().getSalud()),
                () -> assertEquals(20, entidadRepository.findById(aldeano.getId()).orElseThrow().getSalud())
        );
    }

    @Test
    void prevalidacionEvitaDanoParcialCuandoUnObjetivoEstaFueraDelRadio() {
        guardarParticipantes(2, 6);

        ObjetivoFueraDeRangoException excepcion = assertThrows(
                ObjetivoFueraDeRangoException.class,
                () -> explosionService.simular(creeper.getId(), jugador.getId(), aldeano.getId(), 3)
        );

        assertAll(
                () -> assertEquals(
                        "El objetivo Aldeano está a 6.0 bloques y queda fuera del radio de explosión de 5.0 bloques.",
                        excepcion.getMessage()
                ),
                () -> assertEquals(20, entidadRepository.findById(jugador.getId()).orElseThrow().getSalud()),
                () -> assertEquals(20, entidadRepository.findById(aldeano.getId()).orElseThrow().getSalud())
        );
    }

    @Test
    void calculaDistanciasGrandesSinDesbordamiento() {
        creeper = entidadRepository.save(new Creeper(20, 0, 0, 0, 0, 1.1e200, 8, 3));
        jugador = entidadRepository.save(new Jugador("Steve", 0, 20, 1e200, 0, 0, 0));
        aldeano = entidadRepository.save(new Aldeano(20, 0, 0, 0, 0, true, "Granjero"));

        ResultadoExplosion resultado = explosionService.simular(
                creeper.getId(), jugador.getId(), aldeano.getId(), 3
        );

        assertEquals(1e200, resultado.objetivos().getFirst().distancia());
        assertEquals(12, entidadRepository.findById(jugador.getId()).orElseThrow().getSalud());
    }

    @Test
    void noConfundeUnaDistanciaPequenaConCero() {
        creeper = entidadRepository.save(new Creeper(20, 0, 0, 0, 0, 0, 8, 3));
        jugador = entidadRepository.save(new Jugador("Steve", 0, 20, 1e-200, 0, 0, 0));
        aldeano = entidadRepository.save(new Aldeano(20, 0, 0, 0, 0, true, "Granjero"));

        assertThrows(ObjetivoFueraDeRangoException.class, () -> explosionService.simular(
                creeper.getId(), jugador.getId(), aldeano.getId(), 3
        ));
        assertEquals(20, entidadRepository.findById(jugador.getId()).orElseThrow().getSalud());
        assertEquals(20, entidadRepository.findById(aldeano.getId()).orElseThrow().getSalud());
    }

    @Test
    void limitaSobredanoACeroYReportaSoloLaSaludPerdida() {
        creeper = entidadRepository.save(new Creeper(20, 0, 0, 0, 0));
        jugador = entidadRepository.save(new Jugador("Steve", 0, 3, 2, 0, 0, 0));
        aldeano = entidadRepository.save(new Aldeano(5, 4, 0, 0, 0, true, "Granjero"));

        ResultadoExplosion resultado = explosionService.simular(
                creeper.getId(), jugador.getId(), aldeano.getId(), 3
        );

        assertAll(
                () -> assertEquals(0, resultado.objetivos().get(0).saludFinal()),
                () -> assertEquals(3, resultado.objetivos().get(0).danoRecibido()),
                () -> assertEquals(0, resultado.objetivos().get(1).saludFinal()),
                () -> assertEquals(5, resultado.objetivos().get(1).danoRecibido()),
                () -> assertEquals(0, entidadRepository.findById(jugador.getId()).orElseThrow().getSalud()),
                () -> assertEquals(0, entidadRepository.findById(aldeano.getId()).orElseThrow().getSalud())
        );
    }

    private void guardarParticipantes(double posicionXJugador, double posicionXAldeano) {
        creeper = entidadRepository.save(new Creeper(20, 0, 0, 0, 0));
        jugador = entidadRepository.save(new Jugador("Steve", 0, 20, posicionXJugador, 0, 0, 0));
        aldeano = entidadRepository.save(
                new Aldeano(20, posicionXAldeano, 0, 0, 0, true, "Granjero")
        );
    }
}
