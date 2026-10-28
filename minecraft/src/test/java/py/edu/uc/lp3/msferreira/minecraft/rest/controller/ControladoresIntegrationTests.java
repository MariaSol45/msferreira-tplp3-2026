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
            "creeper, Creeper, El creeper se acerca en silencio y explota en 3 segundos.",
            "aldeano, Aldeano, El aldeano de profesión Granjero comercia con los jugadores."
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
                        .param("tipo", "dragon")
                        .param("salud", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Entrada inválida"));
    }

    @Test
    void creeperSinTiempoExplosionUsaConstructorSimplificadoYMuestraSuEstado() throws Exception {
        mockMvc.perform(get("/entidad")
                        .param("tipo", "creeper")
                        .param("salud", "20")
                        .param("x", "1")
                        .param("y", "2")
                        .param("z", "3")
                        .param("velocidad", "4")
                        .param("rango", "12")
                        .param("dano", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Creeper"))
                .andExpect(jsonPath("$.salud").value(20))
                .andExpect(jsonPath("$.posicion").value("(1.0, 2.0, 3.0)"))
                .andExpect(jsonPath("$.velocidad").value(4))
                .andExpect(jsonPath("$.rangoDeteccion").value(12.0))
                .andExpect(jsonPath("$.danoAtaque").value(6))
                .andExpect(jsonPath("$.tiempoExplosion").value(3))
                .andExpect(jsonPath("$.constructor").value("simplificado"));
    }

    @Test
    void creeperConTiempoExplosionUsaConstructorCompleto() throws Exception {
        mockMvc.perform(get("/entidad")
                        .param("tipo", "creeper")
                        .param("salud", "20")
                        .param("tiempoExplosion", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tiempoExplosion").value(8))
                .andExpect(jsonPath("$.constructor").value("completo"))
                .andExpect(jsonPath("$.comportamiento")
                        .value("El creeper se acerca en silencio y explota en 8 segundos."));
    }

    @Test
    void aldeanoMuestraSuProfesionYEstadoPropio() throws Exception {
        mockMvc.perform(get("/entidad")
                        .param("tipo", "aldeano")
                        .param("salud", "20")
                        .param("profesion", "Bibliotecario")
                        .param("domesticable", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profesion").value("Bibliotecario"))
                .andExpect(jsonPath("$.domesticable").value(false))
                .andExpect(jsonPath("$.comportamiento")
                        .value("El aldeano de profesión Bibliotecario comercia con los jugadores."));
    }

    @ParameterizedTest
    @CsvSource({
            "zombie, Zombie, El zombie persigue aldeanos hasta 10.0 bloques para infectarlos.",
            "esqueleto, Esqueleto, El esqueleto dispara flechas que causan 5 puntos de daño.",
            "animal, Animal, El animal salvaje recorre el entorno y huye de las amenazas.",
            "jugador, Jugador, El jugador Steve interactúa con el mundo con nivel de experiencia 0."
    })
    void tiposConcretosMuestranSuComportamiento(String tipo, String clase, String comportamiento) throws Exception {
        mockMvc.perform(get("/entidad")
                        .param("tipo", tipo)
                        .param("salud", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value(clase))
                .andExpect(jsonPath("$.comportamiento").value(comportamiento));
    }

    @Test
    void ataqueDirectoEsObservable() throws Exception {
        mockMvc.perform(get("/entidad/atacar")
                        .param("saludObjetivo", "20")
                        .param("dano", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.variante").value("atacar(Entidad)"))
                .andExpect(jsonPath("$.saludInicial").value(20))
                .andExpect(jsonPath("$.saludFinal").value(14))
                .andExpect(jsonPath("$.danoAplicado").value(6));
    }

    @Test
    void ataqueConDistanciaEsObservable() throws Exception {
        mockMvc.perform(get("/entidad/atacar")
                        .param("saludObjetivo", "20")
                        .param("dano", "6")
                        .param("rango", "10")
                        .param("distancia", "4.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.variante").value("atacar(Entidad, double)"))
                .andExpect(jsonPath("$.distancia").value(4.5))
                .andExpect(jsonPath("$.saludFinal").value(14));
    }

    @Test
    void ataqueFueraDeRangoDevuelveBadRequest() throws Exception {
        mockMvc.perform(get("/entidad/atacar")
                        .param("rango", "10")
                        .param("distancia", "11"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Entrada inválida"))
                .andExpect(jsonPath("$.mensaje").value("El objetivo está fuera del rango de detección."));
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
