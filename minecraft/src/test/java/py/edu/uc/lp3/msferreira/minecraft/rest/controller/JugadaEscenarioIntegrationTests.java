package py.edu.uc.lp3.msferreira.minecraft.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;

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
import py.edu.uc.lp3.msferreira.minecraft.domain.Jugada;
import py.edu.uc.lp3.msferreira.minecraft.domain.Jugador;
import py.edu.uc.lp3.msferreira.minecraft.repository.EntidadRepository;
import py.edu.uc.lp3.msferreira.minecraft.repository.JugadaRepository;
import py.edu.uc.lp3.msferreira.minecraft.service.companero.CompaneroFalso;

@SpringBootTest
@AutoConfigureMockMvc
class JugadaEscenarioIntegrationTests {
    private static final String RUTA_A2 = "/api/minecraft/reglas/mecha-corta";
    private static final String RUTA_A3 = "/explosiones/validar";
    private static final CompaneroFalso IRINA = levantar(RUTA_A2);
    private static final CompaneroFalso CECILIA = levantar(RUTA_A3);

    @DynamicPropertySource
    static void configurarCompaneros(DynamicPropertyRegistry registro) {
        registro.add("companeros.a2.base-url", IRINA::baseUrl);
        registro.add("companeros.a2.ruta-validacion", () -> RUTA_A2);
        registro.add("companeros.a3.base-url", CECILIA::baseUrl);
        registro.add("companeros.a3.ruta-validacion", () -> RUTA_A3);
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
        String pedidoA2 = IRINA.pedidos().getFirst();
        assertTrue(pedidoA2.contains("\"saludCreeper\":20"), pedidoA2);
        assertTrue(pedidoA2.contains("\"creeper\":{\"x\":0.0,\"y\":0.0,\"z\":0.0}"), pedidoA2);
        assertTrue(pedidoA2.contains("\"tipo\":\"jugador\""), pedidoA2);
        assertFalse(pedidoA2.contains("tiempoExplosion"), pedidoA2);
        String pedidoA3 = CECILIA.pedidos().getFirst();
        assertTrue(pedidoA3.contains("\"saludCreeper\":20"), pedidoA3);
        assertTrue(pedidoA3.contains("\"yaDetonado\":false"), pedidoA3);
    }

    @Test
    void companerosMuestraLasRutasConfiguradas() throws Exception {
        mockMvc.perform(get("/jugadas/companeros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rol").value("A2"))
                .andExpect(jsonPath("$[0].urlValidacion").value(IRINA.baseUrl() + RUTA_A2))
                .andExpect(jsonPath("$[1].rol").value("A3"))
                .andExpect(jsonPath("$[1].urlValidacion").value(CECILIA.baseUrl() + RUTA_A3));
    }

    @Test
    void rechazoDeA2NoGuardaNadaYNoConsultaAA3() throws Exception {
        String cuerpo = """
                {"error":"validacion","codigo":"fuera_de_alcance",
                 "mensaje":"Ningún jugador vivo a 3 bloques o menos."}
                """;
        IRINA.responder(422, cuerpo);

        mockMvc.perform(post("/jugadas/explosion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud(3)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.origen").value("A2"))
                .andExpect(jsonPath("$.companero").value("Irina"))
                .andExpect(jsonPath("$.estadoCompanero").value(422))
                .andExpect(jsonPath("$.motivoCompanero").value("fuera_de_alcance"))
                .andExpect(jsonPath("$.respuestaCompanero").value(cuerpo));

        assertBaseSinCambios();
        assertEquals(0, CECILIA.pedidos().size());
    }

    @Test
    void rechazoDeA2PorCreeperMuertoNoGuardaNada() throws Exception {
        IRINA.responder(409, """
                {"error":"conflicto","codigo":"entidad_muerta","mensaje":"El Creeper está muerto."}
                """);

        mockMvc.perform(post("/jugadas/explosion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud(3)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.origen").value("A2"))
                .andExpect(jsonPath("$.estadoCompanero").value(409))
                .andExpect(jsonPath("$.motivoCompanero").value("entidad_muerta"));

        assertBaseSinCambios();
    }

    @Test
    void rechazoDeA2PorDatoInvalidoNoGuardaNada() throws Exception {
        IRINA.responder(400, """
                {"error":"peticion","codigo":"dato_invalido","mensaje":"Falta la posición del creeper."}
                """);

        mockMvc.perform(post("/jugadas/explosion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud(3)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.origen").value("A2"))
                .andExpect(jsonPath("$.estadoCompanero").value(400))
                .andExpect(jsonPath("$.motivoCompanero").value("dato_invalido"));

        assertBaseSinCambios();
    }

    @Test
    void rechazoDeA3NoGuardaNada() throws Exception {
        CECILIA.responder(409, """
                {"status":409,"tipo":"CreeperYaDetonadoException","mensaje":"Ya detonó."}
                """);

        mockMvc.perform(post("/jugadas/explosion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud(3)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.origen").value("A3"))
                .andExpect(jsonPath("$.companero").value("Cecilia"))
                .andExpect(jsonPath("$.estadoCompanero").value(409))
                .andExpect(jsonPath("$.motivoCompanero").value("CreeperYaDetonadoException"));

        assertBaseSinCambios();
        assertEquals(1, IRINA.pedidos().size());
    }

    @Test
    void rechazoDeA3PorCreeperMuertoNoGuardaNada() throws Exception {
        CECILIA.responder(422, """
                {"status":422,"tipo":"CreeperMuertoException","mensaje":"Está muerto."}
                """);

        mockMvc.perform(post("/jugadas/explosion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud(3)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.origen").value("A3"))
                .andExpect(jsonPath("$.estadoCompanero").value(422))
                .andExpect(jsonPath("$.motivoCompanero").value("CreeperMuertoException"));

        assertBaseSinCambios();
    }

    @Test
    void unCreeperYaRegistradoSeEnvíaComoYaDetonadoACecilia() throws Exception {
        jugadaRepository.save(new Jugada(
                "Explosión de Creeper", creeper.getId(), jugador.getId(), aldeano.getId(),
                3, 12, 12, "Irina", "Cecilia", Instant.now()
        ));
        CECILIA.responderCuando(pedido -> pedido.contains("\"yaDetonado\":true"), 409,
                "{\"status\":409,\"tipo\":\"CreeperYaDetonadoException\"}");

        mockMvc.perform(post("/jugadas/explosion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud(3)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.origen").value("A3"));

        assertEquals(1, jugadaRepository.count());
        assertTrue(CECILIA.pedidos().getFirst().contains("\"yaDetonado\":true"));
        mockMvc.perform(get("/entidades/{id}", jugador.getId())).andExpect(jsonPath("$.salud").value(20));
        mockMvc.perform(get("/entidades/{id}", aldeano.getId())).andExpect(jsonPath("$.salud").value(20));
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
    void unaRespuestaInesperadaDeA2Devuelve502YNoConsultaAA3() throws Exception {
        IRINA.responder(200, "{\"regla\":\"mecha-corta\"}");

        mockMvc.perform(post("/jugadas/explosion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud(3)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.origen").value("A2"));

        assertBaseSinCambios();
        assertEquals(0, CECILIA.pedidos().size());
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
        mockMvc.perform(get("/jugadas")).andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/entidades/{id}", jugador.getId())).andExpect(jsonPath("$.salud").value(20));
        mockMvc.perform(get("/entidades/{id}", aldeano.getId())).andExpect(jsonPath("$.salud").value(20));
    }

    private String solicitud(int tiempoTranscurrido) {
        return """
                {"creeperId":%d,"jugadorId":%d,"aldeanoId":%d,"tiempoTranscurrido":%d}
                """.formatted(creeper.getId(), jugador.getId(), aldeano.getId(), tiempoTranscurrido);
    }

    private static CompaneroFalso levantar(String ruta) {
        try {
            return new CompaneroFalso(ruta);
        } catch (IOException excepcion) {
            throw new UncheckedIOException(excepcion);
        }
    }
}
