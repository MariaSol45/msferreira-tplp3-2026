package py.edu.uc.lp3.msferreira.minecraft.rest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ExplosionControllerIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void devuelveElResultadoDeLaExplosion() throws Exception {
        mockMvc.perform(post("/explosiones/simular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud(3, 2, 4)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evento").value("Explosión de Creeper"))
                .andExpect(jsonPath("$.clasesParticipantes[0]").value("Creeper"))
                .andExpect(jsonPath("$.clasesParticipantes[1]").value("Jugador"))
                .andExpect(jsonPath("$.clasesParticipantes[2]").value("Aldeano"))
                .andExpect(jsonPath("$.objetivos[0].saludInicial").value(20))
                .andExpect(jsonPath("$.objetivos[0].saludFinal").value(12))
                .andExpect(jsonPath("$.objetivos[1].saludFinal").value(12));
    }

    @Test
    void devuelveConflictCuandoLaExplosionNoEstaPreparada() throws Exception {
        mockMvc.perform(post("/explosiones/simular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud(2, 2, 4)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Explosión no preparada"))
                .andExpect(jsonPath("$.estado").value(409));
    }

    @Test
    void devuelveUnprocessableEntityCuandoUnObjetivoEstaFueraDelRadio() throws Exception {
        mockMvc.perform(post("/explosiones/simular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud(3, 2, 6)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.error").value("Objetivo fuera del radio de explosión"))
                .andExpect(jsonPath("$.estado").value(422));
    }

    private String solicitud(int tiempoTranscurrido,
                             double posicionXJugador, double posicionXAldeano) {
        return """
                {
                  "tiempoTranscurrido": %d,
                  "posicionXJugador": %s,
                  "posicionXAldeano": %s
                }
                """.formatted(
                tiempoTranscurrido,
                posicionXJugador,
                posicionXAldeano
        );
    }
}
