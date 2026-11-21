package py.edu.uc.lp3.msferreira.minecraft.service.companero;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.sun.net.httpserver.HttpServer;

/** Servidor HTTP real que hace de A2 o A3 en las pruebas: responde el estado y cuerpo configurados. */
public class CompaneroFalso implements AutoCloseable {
    private final HttpServer servidor;
    private final List<String> pedidos = new ArrayList<>();
    private volatile int estado = 200;
    private volatile String cuerpo = "{\"aceptada\":true,\"mensaje\":\"OK\"}";
    private volatile Predicate<String> condicion;
    private volatile int estadoCondicional;
    private volatile String cuerpoCondicional;

    public CompaneroFalso(String ruta) throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext(ruta, intercambio -> {
            String pedido = new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            synchronized (pedidos) {
                pedidos.add(pedido);
            }
            int estadoRespuesta = estado;
            String cuerpoRespuesta = cuerpo;
            Predicate<String> regla = condicion;
            if (regla != null && regla.test(pedido)) {
                estadoRespuesta = estadoCondicional;
                cuerpoRespuesta = cuerpoCondicional;
            }
            byte[] respuesta = cuerpoRespuesta.getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().add("Content-Type", "application/json");
            intercambio.sendResponseHeaders(estadoRespuesta, respuesta.length);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(respuesta);
            }
        });
        servidor.start();
    }

    public String baseUrl() {
        return "http://127.0.0.1:" + servidor.getAddress().getPort();
    }

    public void responder(int estado, String cuerpo) {
        this.estado = estado;
        this.cuerpo = cuerpo;
        this.condicion = null;
    }

    public void responderCuando(Predicate<String> condicion, int estado, String cuerpo) {
        this.condicion = condicion;
        this.estadoCondicional = estado;
        this.cuerpoCondicional = cuerpo;
    }

    public void aceptar() {
        responder(200, "{\"aceptada\":true,\"mensaje\":\"OK\"}");
    }

    public List<String> pedidos() {
        synchronized (pedidos) {
            return List.copyOf(pedidos);
        }
    }

    public void limpiar() {
        synchronized (pedidos) {
            pedidos.clear();
        }
        condicion = null;
    }

    @Override
    public void close() {
        servidor.stop(0);
    }
}
