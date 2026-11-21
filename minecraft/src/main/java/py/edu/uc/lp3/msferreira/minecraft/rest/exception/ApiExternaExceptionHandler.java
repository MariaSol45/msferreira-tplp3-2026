package py.edu.uc.lp3.msferreira.minecraft.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import py.edu.uc.lp3.msferreira.minecraft.service.ApiExternaException;

@RestControllerAdvice
public class ApiExternaExceptionHandler {

    @ExceptionHandler(ApiExternaException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorApiExterna manejarApiExterna(ApiExternaException excepcion) {
        return new ErrorApiExterna(
                "Error al consultar la API externa", excepcion.getMessage(), HttpStatus.BAD_GATEWAY.value());
    }

    public record ErrorApiExterna(String error, String mensaje, int estado) {
    }
}
