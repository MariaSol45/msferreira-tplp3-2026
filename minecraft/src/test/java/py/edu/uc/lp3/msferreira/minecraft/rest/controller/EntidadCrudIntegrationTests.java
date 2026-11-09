package py.edu.uc.lp3.msferreira.minecraft.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import py.edu.uc.lp3.msferreira.minecraft.domain.Aldeano;
import py.edu.uc.lp3.msferreira.minecraft.domain.Creeper;
import py.edu.uc.lp3.msferreira.minecraft.domain.Entidad;
import py.edu.uc.lp3.msferreira.minecraft.domain.EntidadGenerica;
import py.edu.uc.lp3.msferreira.minecraft.domain.Jugador;
import py.edu.uc.lp3.msferreira.minecraft.repository.EntidadRepository;

@SpringBootTest
@AutoConfigureMockMvc
class EntidadCrudIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntidadRepository entidadRepository;

    @BeforeEach
    void limpiarBaseDeDatos() {
        entidadRepository.deleteAll();
    }

    @Test
    void creaUnaEntidad() throws Exception {
        mockMvc.perform(post("/entidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "entidad",
                                  "salud": 20,
                                  "posicionX": 1.5,
                                  "posicionY": 2.5,
                                  "posicionZ": 3.5,
                                  "velocidad": 4
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/entidades/\\d+")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.tipo").value("entidad"))
                .andExpect(jsonPath("$.salud").value(20))
                .andExpect(jsonPath("$.posicionX").value(1.5))
                .andExpect(jsonPath("$.velocidad").value(4));

        assertEquals(1, entidadRepository.count());
    }

    @Test
    void listaYObtieneUnaEntidad() throws Exception {
        Entidad guardada = entidadRepository.save(new EntidadGenerica(15, 1, 2, 3, 2));

        mockMvc.perform(get("/entidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(guardada.getId()))
                .andExpect(jsonPath("$[0].tipo").value("entidad"))
                .andExpect(jsonPath("$[0].salud").value(15));

        mockMvc.perform(get("/entidades/{id}", guardada.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guardada.getId()))
                .andExpect(jsonPath("$.posicionZ").value(3.0));
    }

    @Test
    void actualizaUnaEntidad() throws Exception {
        Entidad guardada = entidadRepository.save(new EntidadGenerica(15, 1, 2, 3, 2));

        mockMvc.perform(put("/entidades/{id}", guardada.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "entidad",
                                  "salud": 30,
                                  "posicionX": 4,
                                  "posicionY": 5,
                                  "posicionZ": 6,
                                  "velocidad": 7
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guardada.getId()))
                .andExpect(jsonPath("$.salud").value(30))
                .andExpect(jsonPath("$.velocidad").value(7));

        Entidad actualizada = entidadRepository.findById(guardada.getId()).orElseThrow();
        assertEquals(30, actualizada.getSalud());
        assertEquals(6, actualizada.getPosicionZ());
    }

    @Test
    void eliminaUnaEntidad() throws Exception {
        Entidad guardada = entidadRepository.save(new EntidadGenerica(15, 1, 2, 3, 2));

        mockMvc.perform(delete("/entidades/{id}", guardada.getId()))
                .andExpect(status().isNoContent());

        assertEquals(0, entidadRepository.count());
    }

    @Test
    void devuelveNotFoundCuandoLaEntidadNoExiste() throws Exception {
        mockMvc.perform(get("/entidades/{id}", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Entidad no encontrada"));
    }

    @Test
    void rechazaUnaEntidadConSaludNegativa() throws Exception {
        mockMvc.perform(post("/entidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "entidad",
                                  "salud": -1,
                                  "posicionX": 1,
                                  "posicionY": 2,
                                  "posicionZ": 3,
                                  "velocidad": 4
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Entrada inválida"))
                .andExpect(jsonPath("$.mensaje").value("La salud no puede ser negativa."));

        assertEquals(0, entidadRepository.count());
    }

    @Test
    void rechazaCrearSinDiscriminadorYNoPersisteDatos() throws Exception {
        mockMvc.perform(post("/entidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "salud": 20,
                                  "posicionX": 1,
                                  "posicionY": 2,
                                  "posicionZ": 3,
                                  "velocidad": 4
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Entrada inválida"));

        assertEquals(0, entidadRepository.count());
    }

    @Test
    void rechazaActualizarSinDiscriminadorYConservaElRegistro() throws Exception {
        EntidadGenerica guardada = entidadRepository.save(new EntidadGenerica(15, 1, 2, 3, 2));

        mockMvc.perform(put("/entidades/{id}", guardada.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "salud": 30,
                                  "posicionX": 4,
                                  "posicionY": 5,
                                  "posicionZ": 6,
                                  "velocidad": 7
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Entrada inválida"));

        Entidad persistida = entidadRepository.findById(guardada.getId()).orElseThrow();
        assertInstanceOf(EntidadGenerica.class, persistida);
        assertEquals(15, persistida.getSalud());
        assertEquals(3, persistida.getPosicionZ());
    }

    @Test
    void rechazaActualizarConTipoDesconocidoYConservaElRegistro() throws Exception {
        EntidadGenerica guardada = entidadRepository.save(new EntidadGenerica(15, 1, 2, 3, 2));

        mockMvc.perform(put("/entidades/{id}", guardada.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "dragon",
                                  "salud": 30,
                                  "posicionX": 4,
                                  "posicionY": 5,
                                  "posicionZ": 6,
                                  "velocidad": 7
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Entrada inválida"));

        Entidad persistida = entidadRepository.findById(guardada.getId()).orElseThrow();
        assertInstanceOf(EntidadGenerica.class, persistida);
        assertEquals(15, persistida.getSalud());
        assertEquals(3, persistida.getPosicionZ());
    }

    @Test
    void creaConsultaYListaLosTresSubtipos() throws Exception {
        mockMvc.perform(post("/entidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "creeper",
                                  "salud": 20,
                                  "posicionX": 1,
                                  "posicionY": 2,
                                  "posicionZ": 3,
                                  "velocidad": 2,
                                  "rangoDeteccion": 8,
                                  "danoAtaque": 6,
                                  "tiempoExplosion": 4
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("creeper"))
                .andExpect(jsonPath("$.tiempoExplosion").value(4))
                .andExpect(jsonPath("$.rangoDeteccion").value(8.0));

        mockMvc.perform(post("/entidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "jugador",
                                  "nombre": "Alex",
                                  "nivelEXP": 12,
                                  "salud": 30,
                                  "posicionX": 4,
                                  "posicionY": 5,
                                  "posicionZ": 6,
                                  "velocidad": 3
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("jugador"))
                .andExpect(jsonPath("$.nombre").value("Alex"))
                .andExpect(jsonPath("$.nivelEXP").value(12));

        mockMvc.perform(post("/entidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "aldeano",
                                  "salud": 25,
                                  "posicionX": 7,
                                  "posicionY": 8,
                                  "posicionZ": 9,
                                  "velocidad": 1,
                                  "domesticable": true,
                                  "profesion": "Bibliotecario"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("aldeano"))
                .andExpect(jsonPath("$.profesion").value("Bibliotecario"))
                .andExpect(jsonPath("$.domesticable").value(true));

        assertEquals(3, entidadRepository.count());
        assertInstanceOf(Creeper.class, buscarPorTipo(Creeper.class));
        assertInstanceOf(Jugador.class, buscarPorTipo(Jugador.class));
        assertInstanceOf(Aldeano.class, buscarPorTipo(Aldeano.class));

        Long creeperId = buscarPorTipo(Creeper.class).getId();
        mockMvc.perform(get("/entidades/{id}", creeperId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("creeper"))
                .andExpect(jsonPath("$.danoAtaque").value(6));

        mockMvc.perform(get("/entidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].tipo", org.hamcrest.Matchers.containsInAnyOrder(
                        "creeper", "jugador", "aldeano"
                )));
    }

    @Test
    void actualizaLosAtributosPropiosDeCadaSubtipo() throws Exception {
        Creeper creeper = entidadRepository.save(new Creeper(20, 0, 0, 0, 1, 5, 8, 3));
        Jugador jugador = entidadRepository.save(new Jugador("Steve", 2, 20, 0, 0, 0, 1));
        Aldeano aldeano = entidadRepository.save(new Aldeano(20, 0, 0, 0, 1, true, "Granjero"));

        mockMvc.perform(put("/entidades/{id}", creeper.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "creeper",
                                  "salud": 28,
                                  "posicionX": 1,
                                  "posicionY": 1,
                                  "posicionZ": 1,
                                  "velocidad": 2,
                                  "rangoDeteccion": 9,
                                  "danoAtaque": 7,
                                  "tiempoExplosion": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("creeper"))
                .andExpect(jsonPath("$.tiempoExplosion").value(5));

        mockMvc.perform(put("/entidades/{id}", jugador.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "jugador",
                                  "nombre": "Alex",
                                  "nivelEXP": 20,
                                  "salud": 35,
                                  "posicionX": 2,
                                  "posicionY": 2,
                                  "posicionZ": 2,
                                  "velocidad": 4
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("jugador"))
                .andExpect(jsonPath("$.nombre").value("Alex"))
                .andExpect(jsonPath("$.nivelEXP").value(20));

        mockMvc.perform(put("/entidades/{id}", aldeano.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "aldeano",
                                  "salud": 22,
                                  "posicionX": 3,
                                  "posicionY": 3,
                                  "posicionZ": 3,
                                  "velocidad": 2,
                                  "domesticable": false,
                                  "profesion": "Herrero"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("aldeano"))
                .andExpect(jsonPath("$.profesion").value("Herrero"))
                .andExpect(jsonPath("$.domesticable").value(false));

        Creeper creeperActualizado = (Creeper) entidadRepository.findById(creeper.getId()).orElseThrow();
        Jugador jugadorActualizado = (Jugador) entidadRepository.findById(jugador.getId()).orElseThrow();
        Aldeano aldeanoActualizado = (Aldeano) entidadRepository.findById(aldeano.getId()).orElseThrow();
        assertEquals(5, creeperActualizado.getTiempoExplosion());
        assertEquals("Alex", jugadorActualizado.getNombre());
        assertEquals("Herrero", aldeanoActualizado.getProfesion());
    }

    @Test
    void eliminaLosTresSubtipos() throws Exception {
        Creeper creeper = entidadRepository.save(new Creeper(20, 0, 0, 0, 1));
        Jugador jugador = entidadRepository.save(new Jugador("Steve"));
        Aldeano aldeano = entidadRepository.save(new Aldeano(20, "Granjero"));

        mockMvc.perform(delete("/entidades/{id}", creeper.getId())).andExpect(status().isNoContent());
        mockMvc.perform(delete("/entidades/{id}", jugador.getId())).andExpect(status().isNoContent());
        mockMvc.perform(delete("/entidades/{id}", aldeano.getId())).andExpect(status().isNoContent());

        assertEquals(0, entidadRepository.count());
    }

    @Test
    void rechazaDatosInvalidosDeLosTresSubtipos() throws Exception {
        mockMvc.perform(post("/entidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "creeper",
                                  "salud": 20,
                                  "posicionX": 0,
                                  "posicionY": 0,
                                  "posicionZ": 0,
                                  "velocidad": 1,
                                  "rangoDeteccion": 5,
                                  "danoAtaque": 8,
                                  "tiempoExplosion": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Entrada inválida"));

        mockMvc.perform(post("/entidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "jugador",
                                  "nombre": " ",
                                  "nivelEXP": 0,
                                  "salud": 20,
                                  "posicionX": 0,
                                  "posicionY": 0,
                                  "posicionZ": 0,
                                  "velocidad": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Entrada inválida"));

        mockMvc.perform(post("/entidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "aldeano",
                                  "salud": 20,
                                  "posicionX": 0,
                                  "posicionY": 0,
                                  "posicionZ": 0,
                                  "velocidad": 1,
                                  "domesticable": true,
                                  "profesion": " "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Entrada inválida"));

        mockMvc.perform(post("/entidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "dragon",
                                  "salud": 20,
                                  "posicionX": 0,
                                  "posicionY": 0,
                                  "posicionZ": 0,
                                  "velocidad": 1
                                }
                                """))
                .andExpect(status().isBadRequest());

        assertEquals(0, entidadRepository.count());
    }

    @Test
    void rechazaCambiarElSubtipoDuranteUnaActualizacion() throws Exception {
        Creeper creeper = entidadRepository.save(new Creeper(20, 0, 0, 0, 1));

        mockMvc.perform(put("/entidades/{id}", creeper.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "jugador",
                                  "nombre": "Alex",
                                  "nivelEXP": 5,
                                  "salud": 20,
                                  "posicionX": 0,
                                  "posicionY": 0,
                                  "posicionZ": 0,
                                  "velocidad": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("No se puede cambiar el tipo de una entidad existente."));

        assertInstanceOf(Creeper.class, entidadRepository.findById(creeper.getId()).orElseThrow());
    }

    private Entidad buscarPorTipo(Class<? extends Entidad> tipo) {
        return entidadRepository.findAll().stream()
                .filter(tipo::isInstance)
                .findFirst()
                .orElseThrow();
    }
}
