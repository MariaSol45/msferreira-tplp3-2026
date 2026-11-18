package py.edu.uc.lp3.msferreira.minecraft.service;

public class ApiExternaException extends RuntimeException {

    public ApiExternaException(String mensaje) {
        super(mensaje);
    }

    public ApiExternaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
