package py.edu.uc.lp3.msferreira.minecraft.service.companero;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService.SolicitudValidacion;
import py.edu.uc.lp3.msferreira.minecraft.service.exception.CompaneroException;
import py.edu.uc.lp3.msferreira.minecraft.service.exception.CompaneroNoDisponibleException;
import py.edu.uc.lp3.msferreira.minecraft.service.exception.RechazoCompaneroException;

/**
 * Cliente HTTP hacia el servicio de un compañero del grupo.
 * La URL base y la ruta salen de application.properties (o variables de entorno),
 * así que si el compañero cambia una ruta en su OpenAPI alcanza con cambiar la configuración.
 * Cada hija decide cómo se arma el cuerpo según el OpenAPI de su compañero.
 */
public abstract class ClienteCompanero {
    private final String rol;
    private final String companero;
    private final String baseUrl;
    private final String rutaValidacion;
    private final RestClient restClient;

    protected ClienteCompanero(String rol, String companero, String baseUrl, String rutaValidacion,
                               long timeoutSegundos) {
        this.rol = rol;
        this.companero = companero;
        this.baseUrl = baseUrl;
        this.rutaValidacion = rutaValidacion;
        Duration timeout = Duration.ofSeconds(timeoutSegundos);
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(timeout).build());
        fabrica.setReadTimeout(timeout);
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(fabrica).build();
    }

    /** Arma el cuerpo que espera la ruta de validación del compañero, según su OpenAPI. */
    protected abstract Object armarCuerpo(SolicitudValidacion jugada);

    /**
     * Pide al compañero que valide la jugada. Termina normalmente si respondió 2xx;
     * lanza {@link RechazoCompaneroException} si respondió 4xx y
     * {@link CompaneroNoDisponibleException} si no respondió o respondió 5xx.
     */
    public void validar(SolicitudValidacion jugada) {
        try {
            restClient.post()
                    .uri(rutaValidacion)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(armarCuerpo(jugada))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (pedido, respuesta) -> {
                        throw new RechazoCompaneroException(
                                rol, companero, respuesta.getStatusCode().value(), leerCuerpo(respuesta));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (pedido, respuesta) -> {
                        throw new CompaneroNoDisponibleException(rol, companero,
                                "respondió " + respuesta.getStatusCode().value() + " " + leerCuerpo(respuesta), null);
                    })
                    .toBodilessEntity();
        } catch (CompaneroException excepcion) {
            throw excepcion;
        } catch (RestClientException excepcion) {
            throw new CompaneroNoDisponibleException(rol, companero,
                    "no se pudo llamar a " + baseUrl + rutaValidacion + " (" + causa(excepcion) + ")", excepcion);
        }
    }

    public String getRol() {
        return rol;
    }

    public String getCompanero() {
        return companero;
    }

    public String getUrlValidacion() {
        return baseUrl + rutaValidacion;
    }

    private static String causa(Exception excepcion) {
        for (Throwable actual = excepcion; actual != null; actual = actual.getCause()) {
            if (actual instanceof java.net.ConnectException) {
                return "conexión rechazada";
            }
            if (actual instanceof java.net.http.HttpTimeoutException) {
                return "tiempo de espera agotado";
            }
        }
        Throwable raiz = NestedExceptionUtils.getMostSpecificCause(excepcion);
        return raiz.getMessage() == null ? raiz.getClass().getSimpleName()
                : raiz.getClass().getSimpleName() + ": " + raiz.getMessage();
    }

    private static String leerCuerpo(ClientHttpResponse respuesta) throws IOException {
        try (InputStream cuerpo = respuesta.getBody()) {
            return new String(cuerpo.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
