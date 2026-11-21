package py.edu.uc.lp3.msferreira.minecraft.service.companero;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService.ObjetivoValidacion;
import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService.Posicion;
import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService.SolicitudValidacion;
import py.edu.uc.lp3.msferreira.minecraft.service.exception.CompaneroNoDisponibleException;
import py.edu.uc.lp3.msferreira.minecraft.service.exception.RechazoCompaneroException;

class ClienteCompaneroTests {
    private static final SolicitudValidacion JUGADA = new SolicitudValidacion(
            3, 3, 5.0, new Posicion(0, 0, 0),
            List.of(new ObjetivoValidacion("jugador", 20, new Posicion(2, 0, 0))));

    private CompaneroFalso irina;
    private ClienteA2 cliente;

    @BeforeEach
    void levantar() throws Exception {
        irina = new CompaneroFalso("/validar");
        cliente = new ClienteA2("Irina", irina.baseUrl(), "/validar", 2);
    }

    @AfterEach
    void bajar() {
        irina.close();
    }

    @Test
    void terminaSinErrorCuandoElCompaneroAcepta() {
        assertDoesNotThrow(() -> cliente.validar(JUGADA));
        assertEquals(1, irina.pedidos().size());
        assertTrue(irina.pedidos().getFirst().contains("\"tiempoExplosion\":3"));
    }

    @Test
    void traduceUn4xxEnRechazoConElRolYLaRespuestaDelCompanero() {
        irina.responder(422, "{\"mensaje\":\"objetivo fuera de rango\"}");

        RechazoCompaneroException rechazo = assertThrows(RechazoCompaneroException.class, () -> cliente.validar(JUGADA));
        assertEquals("A2", rechazo.getRol());
        assertEquals("Irina", rechazo.getCompanero());
        assertEquals(422, rechazo.getEstadoCompanero());
        assertTrue(rechazo.getRespuestaCompanero().contains("objetivo fuera de rango"));
    }

    @Test
    void traduceUn5xxEnCompaneroNoDisponible() {
        irina.responder(500, "{}");

        CompaneroNoDisponibleException error =
                assertThrows(CompaneroNoDisponibleException.class, () -> cliente.validar(JUGADA));
        assertEquals("A2", error.getRol());
    }

    @Test
    void traduceUnaConexionRechazadaEnCompaneroNoDisponible() {
        ClienteA3 cecilia = new ClienteA3("Cecilia", "http://127.0.0.1:9", "/validar", 1);

        CompaneroNoDisponibleException error =
                assertThrows(CompaneroNoDisponibleException.class, () -> cecilia.validar(JUGADA));
        assertEquals("A3", error.getRol());
        assertEquals("Cecilia", error.getCompanero());
        assertTrue(error.getMessage().contains("conexión rechazada"), error.getMessage());
    }
}
