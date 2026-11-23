package py.edu.uc.lp3.msferreira.minecraft.service.companero;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService.EstadoExplosion;
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
    private static final Logger REGISTRO = LogManager.getLogger(ClienteCompanero.class);
    private static final JsonMapper MAPEO = JsonMapper.builder().build();

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
    protected abstract Object armarCuerpo(EstadoExplosion estado);

    /** Nombre del esquema del cuerpo, para el log DEBUG sin exponer valores. */
    protected abstract String describirCuerpo();

    /**
     * Pide al compañero que valide la jugada. Termina normalmente si respondió 2xx con aceptada=true;
     * lanza {@link RechazoCompaneroException} si respondió 4xx y
     * {@link CompaneroNoDisponibleException} si no respondió, respondió 5xx o dio un cuerpo inesperado.
     */
    public void validar(EstadoExplosion estado) {
        Object cuerpo = armarCuerpo(estado);
        REGISTRO.debug("Preparando solicitud para {} ({}) según esquema {}", rol, companero, describirCuerpo());
        ResponseEntity<String> respuesta;
        try {
            REGISTRO.debug("Enviando POST a {} (destino {} {})", rol, companero, getUrlValidacion());
            respuesta = restClient.post()
                    .uri(rutaValidacion)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(cuerpo)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (pedido, respuestaHttp) -> {
                        String respuestaCruda = leerCuerpo(respuestaHttp);
                        RespuestaCompanero interpretada = interpretar(respuestaCruda);
                        REGISTRO.debug("HTTP {} recibido de {} ({}): rechazo identificado, motivo={}",
                                respuestaHttp.getStatusCode().value(), rol, companero, interpretada.motivo());
                        throw new RechazoCompaneroException(
                                rol, companero, respuestaHttp.getStatusCode().value(), respuestaCruda,
                                interpretada.motivo()
                        );
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (pedido, respuestaHttp) -> {
                        String respuestaCruda = leerCuerpo(respuestaHttp);
                        REGISTRO.debug("HTTP {} recibido de {} ({}): no disponible, se traduce a 502",
                                respuestaHttp.getStatusCode().value(), rol, companero);
                        throw new CompaneroNoDisponibleException(rol, companero,
                                "respondió " + respuestaHttp.getStatusCode().value() + " " + respuestaCruda, null);
                    })
                    .toEntity(String.class);
        } catch (CompaneroException excepcion) {
            throw excepcion;
        } catch (RestClientException excepcion) {
            REGISTRO.debug("Fallo de comunicación con {} ({}): {}; se traduce a 502",
                    rol, companero, causa(excepcion));
            throw new CompaneroNoDisponibleException(rol, companero,
                    "no se pudo llamar a " + baseUrl + rutaValidacion + " (" + causa(excepcion) + ")", excepcion);
        }

        int estadoHttp = respuesta.getStatusCode().value();
        REGISTRO.debug("HTTP {} recibido de {} ({})", estadoHttp, rol, companero);
        RespuestaCompanero interpretada = interpretar(respuesta.getBody());
        if (!Boolean.TRUE.equals(interpretada.aceptada())) {
            REGISTRO.debug("HTTP {} de {} ({}) sin aceptada=true: inválido, se traduce a 502",
                    estadoHttp, rol, companero);
            throw new CompaneroNoDisponibleException(rol, companero,
                    "respuesta inesperada al validar (se esperaba aceptada=true)", null);
        }
        REGISTRO.debug("Aceptación confirmada: HTTP {} de {} ({}), regla={}",
                estadoHttp, rol, companero, interpretada.regla());
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

    private static RespuestaCompanero interpretar(String json) {
        if (json == null || json.isBlank()) {
            return new RespuestaCompanero(null, null, null, null);
        }
        try {
            JsonNode nodo = MAPEO.readTree(json);
            if (nodo == null || !nodo.isObject()) {
                return new RespuestaCompanero(null, null, null, null);
            }
            JsonNode aceptada = nodo.get("aceptada");
            Boolean valorAceptada = aceptada != null && aceptada.isBoolean() ? aceptada.asBoolean() : null;
            return new RespuestaCompanero(
                    valorAceptada,
                    primerTexto(nodo, "codigo", "tipo", "error"),
                    primerTexto(nodo, "mensaje", "message"),
                    primerTexto(nodo, "regla")
            );
        } catch (JacksonException excepcion) {
            REGISTRO.debug("Respuesta no interpretable: {}", excepcion.getClass().getSimpleName());
            return new RespuestaCompanero(null, null, null, null);
        }
    }

    private static String primerTexto(JsonNode nodo, String... nombres) {
        for (String nombre : nombres) {
            JsonNode valor = nodo.get(nombre);
            if (valor != null && valor.isTextual() && !valor.asText().isBlank()) {
                return valor.asText();
            }
        }
        return null;
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

    private record RespuestaCompanero(Boolean aceptada, String motivo, String mensaje, String regla) {
    }
}
