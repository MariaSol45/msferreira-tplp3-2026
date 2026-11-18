package py.edu.uc.lp3.msferreira.minecraft.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import io.swagger.v3.oas.annotations.media.Schema;

@Service
public class ApiExternaService {

    @Schema(description = "Objeto externo devuelto por la API restful-api.dev.")
    public record ObjetoExterno(
            @Schema(description = "Identificador del objeto.", example = "1") String id,
            @Schema(description = "Nombre del objeto.", example = "Google Pixel 6 Pro") String name,
            @Schema(description = "Atributos variables del objeto; puede ser null.", nullable = true) Map<String, Object> data) {
    }

    private final WebClient webClient;
    private final Duration timeout;

    public ApiExternaService(
            WebClient.Builder builder,
            @Value("${api.externa.base-url:https://api.restful-api.dev}") String baseUrl,
            @Value("${api.externa.timeout-segundos:10}") long timeoutSegundos) {
        this.webClient = builder.clone().baseUrl(baseUrl).build();
        this.timeout = Duration.ofSeconds(timeoutSegundos);
    }

    public List<ObjetoExterno> listarObjetos() {
        try {
            List<ObjetoExterno> objetos = webClient.get()
                    .uri("/objects")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, respuesta ->
                            respuesta.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .map(cuerpo -> new ApiExternaException(
                                            "La API externa respondió "
                                                    + respuesta.statusCode().value()
                                                    + (cuerpo.isBlank() ? "" : ": " + cuerpo))))
                    .bodyToFlux(ObjetoExterno.class)
                    .collectList()
                    .block(timeout);
            return objetos == null ? List.of() : List.copyOf(objetos);
        } catch (ApiExternaException excepcion) {
            throw excepcion;
        } catch (RuntimeException excepcion) {
            throw new ApiExternaException(
                    "No se pudo consultar la API externa: " + excepcion.getMessage(), excepcion);
        }
    }
}
