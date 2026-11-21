package py.edu.uc.lp3.msferreira.minecraft.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import py.edu.uc.lp3.msferreira.minecraft.domain.Aldeano;
import py.edu.uc.lp3.msferreira.minecraft.domain.Creeper;
import py.edu.uc.lp3.msferreira.minecraft.domain.Jugador;
import py.edu.uc.lp3.msferreira.minecraft.repository.EntidadRepository;
import py.edu.uc.lp3.msferreira.minecraft.repository.JugadaRepository;
import py.edu.uc.lp3.msferreira.minecraft.service.companero.CompaneroFalso;

@SpringBootTest
@AutoConfigureMockMvc
class JugadaEscenarioIntegrationTests {
    private static final CompaneroFalso IRINA = levantar();
    private static final CompaneroFalso CECILIA = levantar();

    @DynamicPropertySource
    static void configurarCompaneros(DynamicPropertyRegistry registro) {
        registro.add("companeros.a2.base-url", IRINA::baseUrl);
        registro.add("companeros.a2.ruta-validacion", () -> "/validar");
        registro.add("companeros.a3.base-url", CECILIA::baseUrl);
        registro.add("companeros.a3.ruta-validacion", () -> "/validar");
    }

    @AfterAll
    static void bajar() {
        IRINA.close();
        CECILIA.close();
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntidadRepository entidadRepository;

    @Autowired
    private JugadaRepository jugadaRepository;

    private Creeper creeper;
    private Jugador jugador;
    private Aldeano aldeano;

    @BeforeEach
    void preparar() {
        jugadaRepository.deleteAll();
        entidadRepository.deleteAll();
        creeper = entidadRepository.save(new Creeper(20, 0, 0, 0, 0));
        jugador = entidadRepository.save(new Jugador("Steve", 0, 20, 2, 0, 0, 0));
        aldeano = entidadRepository.save(new Aldeano(20, 4, 0, 0, 0, true, "Granjero"));
        IRINA.aceptar();
        CECILIA.aceptar();
        IRINA.limpiar();
        CECILIA.limpiar();
    }

    @Test
    void caminoFelizGuardaLaJugadaYAplicaElDano() throws Exception {
        String ubicacion = mockMvc.perform(post("/jugadas/explosion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud(3)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.aceptadaPorA2").value("Irina"))
                .andExpect(jsonPath("$.aceptadaPorA3").value("Cecilia"))
                .andExpect(jsonPath("$.saludFinalJugador").value(12))
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get(ubicacion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creeperId").value(creeper.getId()));
        mockMvc.perform(get("/entidades/{id}", jugador.getId()))
                .andExpect(jsonPath("$.salud").value(12));
        assertEquals(1, IRINA.pedidos().size());
        assertEquals(1, CECILIA.pedidos().size());
        assertTrue(IRINA.pedidos().getFirst().contains("\"radioExplosion\":5.0"));
    }

    @Test
    void rechazoDeA2NoGuardaNadaYNoConsultaAA3() throws Exception {
        IRINA.responder(422, "{\"error\":\"regla de Irina\"}");

        mockMvc.perform(post("/jugadas/explosion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud(3)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.origen").value("A2"))
                .andExpect(jsonPath("$.companero").value("Irina"))
                .andExpect(jsonPath("$.estadoCompanero").value(422))
                .andExpect(jsonPath("$.respuestaCompanero").value("{\"error\":\"regla de Irina\"}"));

        assertBaseSinCambios();
        assertEquals(0, CECILIA.pedidos().size());
    }

    @Test
    void rechazoDeA3NoGuardaNada() throws Exception {
        CECILIA.responder(400, "{\"error\":\"regla de Cecilia\"}");

        mockMvc.perform(post("/jugadas/explosion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud(3)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.origen").value("A3"))
                .andExpect(jsonPath("$.companero").value("Cecilia"))
                .andExpect(jsonPath("$.estadoCompanero").value(400));

        assertBaseSinCambios();
        assertEquals(1, IRINA.pedidos().size());
    }

    @Test
    void siA3NoRespondeDevuelve502ConOrigenYNoGuardaNada() throws Exception {
        CECILIA.responder(503, "{}");

        mockMvc.perform(post("/jugadas/explosion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud(3)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.origen").value("A3"));

        assertBaseSinCambios();
    }

    @Test
    void miReglaDeDetonacionRechazaAntesDeConsultarALosCompaneros() throws Exception {
        mockMvc.perform(post("/jugadas/explosion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud(2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Explosión no preparada"));

        assertBaseSinCambios();
        assertEquals(0, IRINA.pedidos().size());
        assertEquals(0, CECILIA.pedidos().size());
    }

    @Test
    void idInvalidoDevuelve400SinConsultarALosCompaneros() throws Exception {
        mockMvc.perform(post("/jugadas/explosion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"creeperId\":-5,\"jugadorId\":1,\"aldeanoId\":1,\"tiempoTranscurrido\":3}"))
                .andExpect(status().isBadRequest());
        assertEquals(0, IRINA.pedidos().size());
    }

    private void assertBaseSinCambios() throws Exception {
        assertEquals(0, jugadaRepository.count());
        mockMvc.perform(get("/jugadas")).andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/entidades/{id}", jugador.getId())).andExpect(jsonPath("$.salud").value(20));
        mockMvc.perform(get("/entidades/{id}", aldeano.getId())).andExpect(jsonPath("$.salud").value(20));
    }

    private String solicitud(int tiempoTranscurrido) {
        return """
                {"creeperId":%d,"jugadorId":%d,"aldeanoId":%d,"tiempoTranscurrido":%d}
                """.formatted(creeper.getId(), jugador.getId(), aldeano.getId(), tiempoTranscurrido);
    }

    private static CompaneroFalso levantar() {
        try {
            return new CompaneroFalso("/validar");
        } catch (IOException excepcion) {
            throw new UncheckedIOException(excepcion);
        }
    }
}
