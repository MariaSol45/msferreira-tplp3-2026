package py.edu.uc.lp3.msferreira.minecraft.service.exception;

/** El compañero no respondió, tardó demasiado o respondió 5xx: no se sabe si acepta la jugada. */
public class CompaneroNoDisponibleException extends CompaneroException {
    public CompaneroNoDisponibleException(String rol, String companero, String detalle, Throwable causa) {
        super(rol, companero, rol + " (" + companero + ") no está disponible: " + detalle, causa);
    }
}
