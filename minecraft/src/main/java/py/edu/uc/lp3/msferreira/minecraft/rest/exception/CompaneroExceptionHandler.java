package py.edu.uc.lp3.msferreira.minecraft.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.swagger.v3.oas.annotations.media.Schema;
import py.edu.uc.lp3.msferreira.minecraft.service.exception.CompaneroNoDisponibleException;
import py.edu.uc.lp3.msferreira.minecraft.service.exception.RechazoCompaneroException;

/** Traduce los errores de A2 y A3 a mi propia respuesta, indicando de quién vino. */
@RestControllerAdvice
public class CompaneroExceptionHandler {

    @ExceptionHandler(RechazoCompaneroException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorCompanero manejarRechazo(RechazoCompaneroException excepcion) {
        return new ErrorCompanero(
                "Jugada rechazada por un compañero",
                excepcion.getRol(),
                excepcion.getCompanero(),
                excepcion.getEstadoCompanero(),
                excepcion.getRespuestaCompanero(),
                excepcion.getMessage() + " La jugada no se guardó.",
                HttpStatus.CONFLICT.value()
        );
    }

    @ExceptionHandler(CompaneroNoDisponibleException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorCompanero manejarNoDisponible(CompaneroNoDisponibleException excepcion) {
        return new ErrorCompanero(
                "Compañero no disponible",
                excepcion.getRol(),
                excepcion.getCompanero(),
                null,
                null,
                excepcion.getMessage() + " La jugada no se guardó.",
                HttpStatus.BAD_GATEWAY.value()
        );
    }

    @Schema(description = "Error del escenario atribuido a A2 o A3.")
    public record ErrorCompanero(
            @Schema(example = "Jugada rechazada por un compañero") String error,
            @Schema(description = "Rol del compañero que originó el error.", example = "A2") String origen,
            @Schema(example = "Irina") String companero,
            @Schema(description = "Código HTTP que devolvió el compañero.", example = "422", nullable = true)
            Integer estadoCompanero,
            @Schema(description = "Cuerpo que devolvió el compañero, sin modificar.", nullable = true)
            String respuestaCompanero,
            @Schema(example = "La jugada fue rechazada por A2 (Irina). La jugada no se guardó.") String mensaje,
            @Schema(example = "409") int estado
    ) {
    }
}
