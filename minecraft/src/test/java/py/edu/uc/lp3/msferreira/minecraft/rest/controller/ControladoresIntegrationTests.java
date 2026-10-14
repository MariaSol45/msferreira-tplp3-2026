package py.edu.uc.lp3.msferreira.minecraft.rest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ControladoresIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void indexDevuelveJson() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.estado").value("API funcionando"))
                .andExpect(jsonPath("$.dominio").value("Minecraft"));
    }

    @ParameterizedTest
    @CsvSource({
            "creeper, Creeper, La entidad hostil ataca.",
            "aldeano, Aldeano, La entidad pasiva huye."
    })
    void entidadValidaDevuelveJson(String tipo, String clase, String comportamiento) throws Exception {
        mockMvc.perform(get("/entidad")
                        .param("tipo", tipo)
                        .param("salud", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tipo").value(clase))
                .andExpect(jsonPath("$.comportamiento").value(comportamiento));
    }

    @Test
    void tipoDesconocidoDevuelveBadRequest() throws Exception {
        mockMvc.perform(get("/entidad")
                        .param("tipo", "zombie")
                        .param("salud", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Entrada inválida"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/entidad?tipo=creeper&salud=0",
            "/entidad?tipo=creeper&salud=20&x=NaN",
            "/entidad?tipo=creeper&salud=20&x=Infinity",
            "/entidad?tipo=creeper&salud=20&velocidad=-1",
            "/entidad?tipo=creeper&salud=20&rango=-1",
            "/entidad?tipo=creeper&salud=20&dano=0",
            "/entidad?tipo=creeper&salud=20&tiempoExplosion=0",
            "/entidad?tipo=creeper",
            "/entidad?tipo=creeper&salud=no-es-numero"
    })
    void entradaInvalidaDevuelveBadRequest(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isBadRequest());
    }

    @Test
    void profesionInvalidaDevuelveBadRequest() throws Exception {
        mockMvc.perform(get("/entidad")
                        .param("tipo", "aldeano")
                        .param("salud", "20")
                        .param("profesion", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Entrada inválida"));
    }

    @Test
    void uiDeJugadorDevuelveJson() throws Exception {
        mockMvc.perform(get("/ui")
                        .param("tipo", "jugador")
                        .param("nombre", "Alex")
                        .param("x", "1")
                        .param("y", "2")
                        .param("z", "3")
                        .param("velocidad", "4"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tipo").value("Jugador"))
                .andExpect(jsonPath("$.imagen").value("jugador.png"))
                .andExpect(jsonPath("$.posicion").value("(1.0, 2.0, 3.0)"))
                .andExpect(jsonPath("$.sonido").value("Sonido del jugador"))
                .andExpect(jsonPath("$.velocidad").value(4))
                .andExpect(jsonPath("$.mensaje")
                        .value("Entidad visible en (1.0, 2.0, 3.0) con velocidad 4."));
    }

    @Test
    void uiDeAldeanoDevuelveJson() throws Exception {
        mockMvc.perform(get("/ui")
                        .param("tipo", "aldeano")
                        .param("profesion", "Herrero")
                        .param("x", "4")
                        .param("y", "5")
                        .param("z", "6")
                        .param("velocidad", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tipo").value("Aldeano"))
                .andExpect(jsonPath("$.imagen").value("aldeano.png"))
                .andExpect(jsonPath("$.posicion").value("(4.0, 5.0, 6.0)"))
                .andExpect(jsonPath("$.sonido").value("Sonido del aldeano"))
                .andExpect(jsonPath("$.velocidad").value(2))
                .andExpect(jsonPath("$.mensaje")
                        .value("El aldeano de profesión Herrero está en (4.0, 5.0, 6.0)."));
    }

    @Test
    void tipoUIDesconocidoDevuelveBadRequest() throws Exception {
        mockMvc.perform(get("/ui")
                        .param("tipo", "zombie"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Entrada inválida"))
                .andExpect(jsonPath("$.mensaje").value("Tipo válido: jugador o aldeano"));
    }
}
