package py.edu.uc.lp3.msferreira.minecraft.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import py.edu.uc.lp3.msferreira.minecraft.domain.Entidad;
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
                .andExpect(jsonPath("$.salud").value(20))
                .andExpect(jsonPath("$.posicionX").value(1.5))
                .andExpect(jsonPath("$.velocidad").value(4));

        assertEquals(1, entidadRepository.count());
    }

    @Test
    void listaYObtieneUnaEntidad() throws Exception {
        Entidad guardada = entidadRepository.save(new Entidad(15, 1, 2, 3, 2));

        mockMvc.perform(get("/entidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(guardada.getId()))
                .andExpect(jsonPath("$[0].salud").value(15));

        mockMvc.perform(get("/entidades/{id}", guardada.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guardada.getId()))
                .andExpect(jsonPath("$.posicionZ").value(3.0));
    }

    @Test
    void actualizaUnaEntidad() throws Exception {
        Entidad guardada = entidadRepository.save(new Entidad(15, 1, 2, 3, 2));

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
        Entidad guardada = entidadRepository.save(new Entidad(15, 1, 2, 3, 2));

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
    void rechazaUnaEntidadConSaludInvalida() throws Exception {
        mockMvc.perform(post("/entidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "salud": 0,
                                  "posicionX": 1,
                                  "posicionY": 2,
                                  "posicionZ": 3,
                                  "velocidad": 4
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Entrada inválida"))
                .andExpect(jsonPath("$.mensaje").value("La salud debe ser mayor que 0."));

        assertEquals(0, entidadRepository.count());
    }
}
