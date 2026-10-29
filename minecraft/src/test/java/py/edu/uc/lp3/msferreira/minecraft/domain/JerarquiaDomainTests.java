package py.edu.uc.lp3.msferreira.minecraft.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Modifier;
import java.util.List;

import org.junit.jupiter.api.Test;

import jakarta.persistence.Entity;
import py.edu.uc.lp3.msferreira.minecraft.domain.exception.ObjetivoFueraDeRangoException;

class JerarquiaDomainTests {

    @Test
    void conservaEntidadConcretaYPersistenteConCategoriasAbstractas() {
        assertAll(
                () -> assertFalse(Modifier.isAbstract(Entidad.class.getModifiers())),
                () -> assertTrue(Entidad.class.isAnnotationPresent(Entity.class)),
                () -> assertTrue(Modifier.isAbstract(EntidadHostil.class.getModifiers())),
                () -> assertTrue(Modifier.isAbstract(EntidadPasiva.class.getModifiers()))
        );
    }

    @Test
    void mantieneRelacionesCoherentesYCompatibilidadDomesticable() {
        EntidadHostil creeper = new Creeper(20, 0, 0, 0, 1, 5, 8);
        EntidadHostil zombie = new Zombie(20);
        EntidadHostil esqueleto = new Esqueleto(20);
        EntidadPasiva aldeano = new Aldeano(20, 0, 0, 0, 1, true, "Granjero");
        EntidadPasiva animal = new Animal(20, 0, 0, 0, 1, false, false);

        assertAll(
                () -> assertTrue(List.of(creeper, zombie, esqueleto).stream()
                        .allMatch(EntidadHostil.class::isInstance)),
                () -> assertTrue(List.of(aldeano, animal).stream()
                        .allMatch(EntidadPasiva.class::isInstance)),
                () -> assertTrue(aldeano.isDomesticable()),
                () -> assertFalse(animal.isDomesticable())
        );
    }

    @Test
    void resuelveComportamientosSobrescritosConReferenciasEntidad() {
        List<Entidad> entidades = List.of(
                new Creeper(20, 0, 0, 0, 1, 5, 8),
                new Zombie(20),
                new Esqueleto(20),
                new Aldeano(20, "Granjero"),
                new Animal(20),
                new Jugador("Steve")
        );

        List<String> comportamientos = entidades.stream()
                .map(Entidad::comportamiento)
                .toList();

        assertAll(
                () -> assertTrue(comportamientos.get(0).contains("creeper")),
                () -> assertTrue(comportamientos.get(1).contains("zombie")),
                () -> assertTrue(comportamientos.get(2).contains("esqueleto")),
                () -> assertTrue(comportamientos.get(3).contains("aldeano")),
                () -> assertTrue(comportamientos.get(4).contains("animal")),
                () -> assertTrue(comportamientos.get(5).contains("jugador"))
        );
    }

    @Test
    void creeperCalculaAlcanceYAplicaDanoSobreReferenciasEntidad() {
        Creeper creeper = new Creeper(20, 0, 0, 0, 0, 5, 8);
        Entidad jugador = new Jugador("Steve", 0, 20, 3, 4, 0, 0);
        Entidad aldeano = new Aldeano(20, 6, 0, 0, 0, true, "Granjero");

        int danoAplicado = creeper.aplicarDanoExplosion(jugador);

        assertAll(
                () -> assertEquals(5, creeper.distanciaA(jugador)),
                () -> assertEquals(8, danoAplicado),
                () -> assertEquals(12, jugador.getSalud()),
                () -> assertThrows(ObjetivoFueraDeRangoException.class,
                        () -> creeper.aplicarDanoExplosion(aldeano)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> creeper.distanciaA(null))
        );
    }
}
