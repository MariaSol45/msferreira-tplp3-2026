package py.edu.uc.lp3.msferreira.minecraft.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import com.sun.net.httpserver.HttpServer;

import py.edu.uc.lp3.msferreira.minecraft.service.ApiExternaService.ObjetoExterno;

class ApiExternaServiceTests {

    private HttpServer servidor;
    private ApiExternaService apiExternaService;

    @BeforeEach
    void iniciarServidor() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        servidor.start();
        apiExternaService = new ApiExternaService(WebClient.builder(), baseUrlDelServidor(), 2);
    }

    @AfterEach
    void detenerServidor() {
        servidor.stop(0);
    }

    @Test
    void listaLosObjetosConDataNulaYAtributosVariables() {
        responder("/objects", 200, """
                [
                  {"id":"1","name":"Google Pixel 6 Pro","data":{"color":"Cloudy White","capacity":"128 GB"}},
                  {"id":"2","name":"Apple iPhone 12 Mini","data":null},
                  {"id":"3","name":"Dispositivo raro","data":{"aniosGarantia":2,"enStock":true}}
                ]
                """);

        List<ObjetoExterno> objetos = apiExternaService.listarObjetos();

        assertEquals(3, objetos.size());
        assertEquals("1", objetos.get(0).id());
        assertEquals("Google Pixel 6 Pro", objetos.get(0).name());
        assertEquals("Cloudy White", objetos.get(0).data().get("color"));
        assertEquals("128 GB", objetos.get(0).data().get("capacity"));
        assertEquals("2", objetos.get(1).id());
        assertNull(objetos.get(1).data());
        assertEquals(2, objetos.get(2).data().get("aniosGarantia"));
        assertEquals(true, objetos.get(2).data().get("enStock"));
    }

    @Test
    void respuestaVaciaDevuelveListaVacia() {
        responder("/objects", 200, "[]");

        assertTrue(apiExternaService.listarObjetos().isEmpty());
    }

    @Test
    void respuestaFallidaSeTraduceEnUnaExcepcion() {
        responder("/objects", 503, "servicio no disponible");

        ApiExternaException error = assertThrows(ApiExternaException.class,
                () -> apiExternaService.listarObjetos());

        assertTrue(error.getMessage().contains("503"));
        assertTrue(error.getMessage().contains("servicio no disponible"));
    }

    @Test
    void errorDeConexionSeTraduceEnUnaExcepcion() throws IOException {
        int puerto;
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            puerto = socket.getLocalPort();
        }
        ApiExternaService sinServidor = new ApiExternaService(
                WebClient.builder(), "http://" + InetAddress.getLoopbackAddress().getHostAddress() + ":" + puerto, 2);

        ApiExternaException error = assertThrows(ApiExternaException.class, sinServidor::listarObjetos);

        assertTrue(error.getMessage().contains("No se pudo consultar la API externa"));
    }

    private String baseUrlDelServidor() {
        return "http://" + InetAddress.getLoopbackAddress().getHostAddress()
                + ":" + servidor.getAddress().getPort();
    }

    private void responder(String ruta, int estado, String cuerpo) {
        servidor.createContext(ruta, intercambio -> {
            byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().set("Content-Type", "application/json");
            intercambio.sendResponseHeaders(estado, bytes.length);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(bytes);
            }
        });
    }
}
