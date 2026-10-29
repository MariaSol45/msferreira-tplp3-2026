package py.edu.uc.lp3.msferreira.minecraft.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import py.edu.uc.lp3.msferreira.minecraft.domain.exception.ExplosionNoPreparadaException;
import py.edu.uc.lp3.msferreira.minecraft.domain.exception.ObjetivoFueraDeRangoException;

@RestControllerAdvice
public class ExplosionExceptionHandler {

    @ExceptionHandler(ExplosionNoPreparadaException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorExplosion manejarExplosionNoPreparada(ExplosionNoPreparadaException excepcion) {
        return new ErrorExplosion("Explosión no preparada", excepcion.getMessage(), HttpStatus.CONFLICT.value());
    }

    @ExceptionHandler(ObjetivoFueraDeRangoException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ErrorExplosion manejarObjetivoFueraDeRango(ObjetivoFueraDeRangoException excepcion) {
        return new ErrorExplosion(
                "Objetivo fuera del radio de explosión",
                excepcion.getMessage(),
                HttpStatus.UNPROCESSABLE_CONTENT.value()
        );
    }

    public record ErrorExplosion(String error, String mensaje, int estado) {
    }
}
