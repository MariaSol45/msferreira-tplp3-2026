package py.edu.uc.lp3.msferreira.minecraft.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import py.edu.uc.lp3.msferreira.minecraft.domain.Entidad;
import py.edu.uc.lp3.msferreira.minecraft.domain.Aldeano;
import py.edu.uc.lp3.msferreira.minecraft.domain.Animal;
import py.edu.uc.lp3.msferreira.minecraft.domain.Creeper;
import py.edu.uc.lp3.msferreira.minecraft.domain.EntidadGenerica;
import py.edu.uc.lp3.msferreira.minecraft.domain.Esqueleto;
import py.edu.uc.lp3.msferreira.minecraft.domain.Jugador;
import py.edu.uc.lp3.msferreira.minecraft.domain.Zombie;
import py.edu.uc.lp3.msferreira.minecraft.repository.EntidadRepository;

class EntidadServiceTests {
    @Mock
    private EntidadRepository entidadRepository;

    private EntidadService entidadService;

    @BeforeEach
    void prepararServicio() {
        MockitoAnnotations.openMocks(this);
        entidadService = new EntidadService(entidadRepository);
        when(entidadRepository.save(any(Entidad.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    @Test
    void rechazaJugadorConSaludFueraDelLimiteSinPersistir() {
        assertThrows(IllegalArgumentException.class,
                () -> entidadService.crear(new Jugador("Steve", 0, 0, 0, 0, 0, 1)));
        assertThrows(IllegalArgumentException.class,
                () -> entidadService.crear(new Jugador("Steve", 0, 5000, 0, 0, 0, 1)));

        verify(entidadRepository, never()).save(any());
    }

    @Test
    void aceptaLosExtremosValidosDeSaludInicialDelJugador() {
        assertEquals(1, entidadService.crear(new Jugador("Uno", 0, 1, 0, 0, 0, 1)).getSalud());
        assertEquals(20, entidadService.crear(new Jugador("Veinte", 0, 20, 0, 0, 0, 1)).getSalud());
    }

    @Test
    void aplicaElMaximoVanillaACreeperYAldeano() {
        assertThrows(IllegalArgumentException.class,
                () -> entidadService.crear(new Creeper(21, 0, 0, 0, 1)));
        assertThrows(IllegalArgumentException.class,
                () -> entidadService.crear(new Aldeano(21, "Granjero")));

        verify(entidadRepository, never()).save(any());
    }

    @Test
    void aplicaLimitesDeSaludPorTipoSinInventarMaximoGenerico() {
        assertEquals(1, entidadService.crear(new Creeper(1, 0, 0, 0, 1)).getSalud());
        assertEquals(20, entidadService.crear(new Creeper(20, 0, 0, 0, 1)).getSalud());
        assertEquals(1, entidadService.crear(new Aldeano(1, "Granjero")).getSalud());
        assertEquals(20, entidadService.crear(new Aldeano(20, "Granjero")).getSalud());
        assertEquals(5000, entidadService.crear(new EntidadGenerica(5000, 0, 0, 0, 1)).getSalud());

        assertThrows(IllegalArgumentException.class,
                () -> entidadService.crear(new Creeper(21, 0, 0, 0, 1)));
        assertThrows(IllegalArgumentException.class,
                () -> entidadService.crear(new Aldeano(21, "Granjero")));
    }

    @Test
    void rechazaClasesTransitoriasEnLaOperacionDePersistencia() {
        assertThrows(IllegalArgumentException.class, () -> entidadService.crear(new Zombie(20)));
        assertThrows(IllegalArgumentException.class, () -> entidadService.crear(new Esqueleto(20)));
        assertThrows(IllegalArgumentException.class, () -> entidadService.crear(new Animal(20)));

        verify(entidadRepository, never()).save(any());
    }

    @Test
    void danoLetalSeLimitaACero() {
        Jugador jugador = new Jugador("Steve", 0, 5, 0, 0, 0, 1);

        entidadService.aplicarDano(jugador, 8);

        assertEquals(0, jugador.getSalud());
    }

    @Test
    void conservaLaSobrecargaDeAtaqueEnElServicio() {
        assertEquals(14, entidadService.atacar(20, 10, 6).saludFinal());
        assertEquals(14, entidadService.atacar(20, 10, 6, 4).saludFinal());
        assertThrows(IllegalArgumentException.class, () -> entidadService.atacar(20, 10, 6, 11));
    }

    @Test
    void entidadNoExponeSetterPublicoDeSalud() {
        boolean tieneSetterPublico = Arrays.stream(Entidad.class.getDeclaredMethods())
                .filter(metodo -> Modifier.isPublic(metodo.getModifiers()))
                .map(Method::getName)
                .anyMatch("setSalud"::equals);

        assertFalse(tieneSetterPublico);
    }
}
