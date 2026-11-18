package py.edu.uc.lp3.msferreira.minecraft.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Modifier;
import java.util.List;

import org.junit.jupiter.api.Test;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

class JerarquiaDomainTests {

    @Test
    void conservaRaizPersistenteYJerarquiaAbstracta() throws NoSuchMethodException {
        assertAll(
                () -> assertTrue(Modifier.isAbstract(Entidad.class.getModifiers())),
                () -> assertTrue(Modifier.isAbstract(
                        Entidad.class.getDeclaredMethod("comportamiento").getModifiers()
                )),
                () -> assertTrue(Entidad.class.isAnnotationPresent(Entity.class)),
                () -> assertEquals(InheritanceType.JOINED,
                        Entidad.class.getAnnotation(Inheritance.class).strategy()),
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

}
