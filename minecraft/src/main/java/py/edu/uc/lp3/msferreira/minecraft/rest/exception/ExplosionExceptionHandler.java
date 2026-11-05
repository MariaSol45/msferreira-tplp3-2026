package py.edu.uc.lp3.msferreira.minecraft.rest.exception;

import java.util.NoSuchElementException;

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

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorExplosion manejarEntidadNoEncontrada(NoSuchElementException excepcion) {
        return new ErrorExplosion("Entidad no encontrada", excepcion.getMessage(), HttpStatus.NOT_FOUND.value());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorExplosion manejarEntidadInvalida(IllegalArgumentException excepcion) {
        return new ErrorExplosion("Entrada inválida", excepcion.getMessage(), HttpStatus.BAD_REQUEST.value());
    }

    public record ErrorExplosion(String error, String mensaje, int estado) {
    }
}
