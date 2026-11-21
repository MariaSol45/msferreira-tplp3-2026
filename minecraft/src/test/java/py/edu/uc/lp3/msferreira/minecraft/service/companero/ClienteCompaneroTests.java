package py.edu.uc.lp3.msferreira.minecraft.service.companero;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService.EstadoExplosion;
import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService.ObjetivoValidacion;
import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService.Posicion;
import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService.SolicitudValidacion;
import py.edu.uc.lp3.msferreira.minecraft.service.exception.CompaneroNoDisponibleException;
import py.edu.uc.lp3.msferreira.minecraft.service.exception.RechazoCompaneroException;

class ClienteCompaneroTests {
    private static final String RUTA_A2 = "/api/minecraft/reglas/mecha-corta";
    private static final EstadoExplosion ESTADO = new EstadoExplosion(
            new SolicitudValidacion(
                    3, 3, 5.0, new Posicion(0, 64, 0),
                    List.of(
                            new ObjetivoValidacion("jugador", 20, new Posicion(2, 64, 0)),
                            new ObjetivoValidacion("aldeano", 20, new Posicion(4, 64, 0))
                    )
            ),
            20,
            false
    );

    private CompaneroFalso irina;
    private ClienteA2 cliente;

    @BeforeEach
    void levantar() throws Exception {
        irina = new CompaneroFalso(RUTA_A2);
        cliente = new ClienteA2("Irina", irina.baseUrl(), RUTA_A2, 2);
    }

    @AfterEach
    void bajar() {
        irina.close();
    }

    @Test
    void aceptaElEsquemaDeMechaCortaDeIrina() {
        irina.responder(200, """
                {"aceptada":true,"regla":"mecha-corta","objetivo":0,"distancia":2.0,
                 "mensaje":"El creeper enciende la mecha."}
                """);

        assertDoesNotThrow(() -> cliente.validar(ESTADO));

        String pedido = irina.pedidos().getFirst();
        assertTrue(pedido.contains("\"saludCreeper\":20"), pedido);
        assertTrue(pedido.contains("\"creeper\":{\"x\":0.0,\"y\":64.0,\"z\":0.0}"), pedido);
        assertTrue(pedido.contains("\"tipo\":\"jugador\""), pedido);
        assertTrue(pedido.contains("\"salud\":20"), pedido);
        assertTrue(pedido.contains("\"posicion\":{\"x\":2.0,\"y\":64.0,\"z\":0.0}"), pedido);
        assertFalse(pedido.contains("tiempoExplosion"), pedido);
        assertFalse(pedido.contains("yaDetonado"), pedido);
    }

    @Test
    void enviaSaludCreeperYaDetonadoACecilia() throws Exception {
        try (CompaneroFalso cecilia = new CompaneroFalso("/explosiones/validar")) {
            ClienteA3 clienteA3 = new ClienteA3("Cecilia", cecilia.baseUrl(), "/explosiones/validar", 2);

            assertDoesNotThrow(() -> clienteA3.validar(ESTADO));

            String pedido = cecilia.pedidos().getFirst();
            assertTrue(pedido.contains("\"saludCreeper\":20"), pedido);
            assertTrue(pedido.contains("\"yaDetonado\":false"), pedido);
            assertFalse(pedido.contains("objetivos"), pedido);
        }
    }

    @Test
    void interpretaElFueraDeAlcanceDeIrina() {
        assertarRechazo(422, """
                {"error":"validacion","codigo":"fuera_de_alcance",
                 "mensaje":"Ningún jugador vivo a 3 bloques o menos."}
                """, "fuera_de_alcance");
    }

    @Test
    void interpretaLaEntidadMuertaDeIrina() {
        assertarRechazo(409, """
                {"error":"conflicto","codigo":"entidad_muerta","mensaje":"El Creeper está muerto."}
                """, "entidad_muerta");
    }

    @Test
    void interpretaElDatoInvalidoDeIrina() {
        assertarRechazo(400, """
                {"error":"peticion","codigo":"dato_invalido","mensaje":"Falta la posición del creeper."}
                """, "dato_invalido");
    }

    @Test
    void interpretaElTipoDeErrorDeCecilia() throws Exception {
        try (CompaneroFalso cecilia = new CompaneroFalso("/explosiones/validar")) {
            ClienteA3 clienteA3 = new ClienteA3("Cecilia", cecilia.baseUrl(), "/explosiones/validar", 2);
            String cuerpo = """
                    {"status":409,"tipo":"CreeperYaDetonadoException","mensaje":"Ya detonó."}
                    """;
            cecilia.responder(409, cuerpo);

            RechazoCompaneroException rechazo =
                    assertThrows(RechazoCompaneroException.class, () -> clienteA3.validar(ESTADO));

            assertEquals("A3", rechazo.getRol());
            assertEquals("Cecilia", rechazo.getCompanero());
            assertEquals(409, rechazo.getEstadoCompanero());
            assertEquals("CreeperYaDetonadoException", rechazo.getMotivo());
            assertEquals(cuerpo, rechazo.getRespuestaCompanero());
        }
    }

    @Test
    void traduceUn5xxEnCompaneroNoDisponible() {
        irina.responder(500, "{}");

        CompaneroNoDisponibleException error =
                assertThrows(CompaneroNoDisponibleException.class, () -> cliente.validar(ESTADO));
        assertEquals("A2", error.getRol());
    }

    @Test
    void traduceUnaConexionRechazadaEnCompaneroNoDisponible() {
        ClienteA3 cecilia = new ClienteA3("Cecilia", "http://127.0.0.1:9", "/explosiones/validar", 1);

        CompaneroNoDisponibleException error =
                assertThrows(CompaneroNoDisponibleException.class, () -> cecilia.validar(ESTADO));
        assertEquals("A3", error.getRol());
        assertEquals("Cecilia", error.getCompanero());
        assertTrue(error.getMessage().contains("conexión rechazada"), error.getMessage());
    }

    @Test
    void noAceptaUn200SinElCampoAceptada() {
        irina.responder(200, "{\"regla\":\"mecha-corta\"}");

        CompaneroNoDisponibleException error =
                assertThrows(CompaneroNoDisponibleException.class, () -> cliente.validar(ESTADO));
        assertEquals("A2", error.getRol());
        assertTrue(error.getMessage().contains("respuesta inesperada"), error.getMessage());
    }

    @Test
    void noAceptaUn200ConAceptadaFalse() {
        irina.responder(200, "{\"aceptada\":false}");

        assertThrows(CompaneroNoDisponibleException.class, () -> cliente.validar(ESTADO));
    }

    private void assertarRechazo(int estado, String cuerpo, String motivoEsperado) {
        irina.responder(estado, cuerpo);

        RechazoCompaneroException rechazo =
                assertThrows(RechazoCompaneroException.class, () -> cliente.validar(ESTADO));

        assertEquals("A2", rechazo.getRol());
        assertEquals("Irina", rechazo.getCompanero());
        assertEquals(estado, rechazo.getEstadoCompanero());
        assertEquals(motivoEsperado, rechazo.getMotivo());
        assertEquals(cuerpo, rechazo.getRespuestaCompanero());
    }
}
