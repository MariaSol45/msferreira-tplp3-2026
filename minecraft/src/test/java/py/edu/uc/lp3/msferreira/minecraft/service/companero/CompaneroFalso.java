package py.edu.uc.lp3.msferreira.minecraft.service.companero;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.HttpServer;

/** Servidor HTTP real que hace de A2 o A3 en las pruebas: responde el estado y cuerpo configurados. */
public class CompaneroFalso implements AutoCloseable {
    private final HttpServer servidor;
    private final List<String> pedidos = new ArrayList<>();
    private volatile int estado = 200;
    private volatile String cuerpo = "{\"aceptada\":true}";

    public CompaneroFalso(String ruta) throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext(ruta, intercambio -> {
            synchronized (pedidos) {
                pedidos.add(new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            }
            byte[] respuesta = cuerpo.getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().add("Content-Type", "application/json");
            intercambio.sendResponseHeaders(estado, respuesta.length);
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
    }

    public void aceptar() {
        responder(200, "{\"aceptada\":true}");
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
    }

    @Override
    public void close() {
        servidor.stop(0);
    }
}
