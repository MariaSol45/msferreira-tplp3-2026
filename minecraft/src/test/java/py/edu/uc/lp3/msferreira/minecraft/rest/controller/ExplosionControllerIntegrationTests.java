package py.edu.uc.lp3.msferreira.minecraft.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import py.edu.uc.lp3.msferreira.minecraft.domain.Jugador;
import py.edu.uc.lp3.msferreira.minecraft.repository.EntidadRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ExplosionControllerIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntidadRepository entidadRepository;

    private Creeper creeper;
    private Jugador jugador;
    private Aldeano aldeano;

    @BeforeEach
    void prepararParticipantes() {
        entidadRepository.deleteAll();
        creeper = entidadRepository.save(new Creeper(20, 0, 0, 0, 0));
        jugador = entidadRepository.save(new Jugador("Steve", 0, 20, 2, 0, 0, 0));
        aldeano = entidadRepository.save(new Aldeano(20, 4, 0, 0, 0, true, "Granjero"));
    }

    @Test
    void persisteElResultadoYPermiteConsultarloConGet() throws Exception {
        mockMvc.perform(post("/explosiones/simular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud(3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evento").value("Explosión de Creeper"))
                .andExpect(jsonPath("$.creeperId").value(creeper.getId()))
                .andExpect(jsonPath("$.objetivos[0].id").value(jugador.getId()))
                .andExpect(jsonPath("$.objetivos[0].saludInicial").value(20))
                .andExpect(jsonPath("$.objetivos[0].saludFinal").value(12))
                .andExpect(jsonPath("$.objetivos[1].id").value(aldeano.getId()))
                .andExpect(jsonPath("$.objetivos[1].saludFinal").value(12));

        mockMvc.perform(get("/entidades/{id}", jugador.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("jugador"))
                .andExpect(jsonPath("$.salud").value(12));
        mockMvc.perform(get("/entidades/{id}", aldeano.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("aldeano"))
                .andExpect(jsonPath("$.salud").value(12));
    }

    @Test
    void devuelveConflictYConservaLaSaludCuandoLaExplosionNoEstaPreparada() throws Exception {
        mockMvc.perform(post("/explosiones/simular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud(2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Explosión no preparada"))
                .andExpect(jsonPath("$.estado").value(409));

        assertEquals(20, entidadRepository.findById(jugador.getId()).orElseThrow().getSalud());
        assertEquals(20, entidadRepository.findById(aldeano.getId()).orElseThrow().getSalud());
    }

    @Test
    void devuelveUnprocessableEntityYRevierteCuandoUnObjetivoEstaFueraDelRadio() throws Exception {
        aldeano = entidadRepository.save(new Aldeano(20, 6, 0, 0, 0, true, "Granjero"));

        mockMvc.perform(post("/explosiones/simular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud(3)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.error").value("Objetivo fuera del radio de explosión"))
                .andExpect(jsonPath("$.estado").value(422));

        assertEquals(20, entidadRepository.findById(jugador.getId()).orElseThrow().getSalud());
        assertEquals(20, entidadRepository.findById(aldeano.getId()).orElseThrow().getSalud());
    }

    @Test
    void rechazaIdsQueNoCorrespondenAlTipoEsperado() throws Exception {
        String solicitud = """
                {
                  "creeperId": %d,
                  "jugadorId": %d,
                  "aldeanoId": %d,
                  "tiempoTranscurrido": 3
                }
                """.formatted(creeper.getId(), aldeano.getId(), jugador.getId());

        mockMvc.perform(post("/explosiones/simular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Entrada inválida"));
    }

    @Test
    void devuelveNotFoundCuandoUnaEntidadNoExiste() throws Exception {
        String solicitud = """
                {
                  "creeperId": 999999,
                  "jugadorId": %d,
                  "aldeanoId": %d,
                  "tiempoTranscurrido": 3
                }
                """.formatted(jugador.getId(), aldeano.getId());

        mockMvc.perform(post("/explosiones/simular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Entidad no encontrada"));
    }

    private String solicitud(int tiempoTranscurrido) {
        return """
                {
                  "creeperId": %d,
                  "jugadorId": %d,
                  "aldeanoId": %d,
                  "tiempoTranscurrido": %d
                }
                """.formatted(
                creeper.getId(),
                jugador.getId(),
                aldeano.getId(),
                tiempoTranscurrido
        );
    }
}
