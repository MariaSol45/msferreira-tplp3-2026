package py.edu.uc.lp3.msferreira.minecraft.domain.exception;

public abstract class ExplosionException extends RuntimeException {
    protected ExplosionException(String mensaje) {
        super(mensaje);
    }
}
