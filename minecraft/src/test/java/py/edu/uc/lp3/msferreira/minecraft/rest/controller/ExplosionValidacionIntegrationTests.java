package py.edu.uc.lp3.msferreira.minecraft.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import py.edu.uc.lp3.msferreira.minecraft.repository.EntidadRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ExplosionValidacionIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntidadRepository entidadRepository;

    @BeforeEach
    void limpiar() {
        entidadRepository.deleteAll();
    }

    @Test
    void aceptaCuandoSeCumplenLasDosReglasSinTocarLaBase() throws Exception {
        mockMvc.perform(post("/explosiones/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud(3, 3, 4)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aceptada").value(true));

        assertEquals(0, entidadRepository.count());
    }

    @Test
    void rechazaCon409SiElCreeperNoDetono() throws Exception {
        mockMvc.perform(post("/explosiones/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud(3, 2, 4)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Explosión no preparada"))
                .andExpect(jsonPath("$.estado").value(409));
    }

    @Test
    void rechazaCon422SiUnObjetivoQuedaFueraDelRadio() throws Exception {
        mockMvc.perform(post("/explosiones/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud(3, 3, 6)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.error").value("Objetivo fuera del radio de explosión"))
                .andExpect(jsonPath("$.estado").value(422));
    }

    @Test
    void rechazaCon400SiFaltanObjetivos() throws Exception {
        mockMvc.perform(post("/explosiones/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tiempoExplosion":3,"tiempoTranscurrido":3,"radioExplosion":5,
                                 "creeper":{"x":0,"y":0,"z":0},"objetivos":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400));
    }

    private String solicitud(int tiempoExplosion, int tiempoTranscurrido, double xAldeano) {
        return """
                {"tiempoExplosion":%d,"tiempoTranscurrido":%d,"radioExplosion":5,
                 "creeper":{"x":0,"y":0,"z":0},
                 "objetivos":[
                   {"tipo":"jugador","salud":20,"posicion":{"x":2,"y":0,"z":0}},
                   {"tipo":"aldeano","salud":20,"posicion":{"x":%s,"y":0,"z":0}}]}
                """.formatted(tiempoExplosion, tiempoTranscurrido, xAldeano);
    }
}
