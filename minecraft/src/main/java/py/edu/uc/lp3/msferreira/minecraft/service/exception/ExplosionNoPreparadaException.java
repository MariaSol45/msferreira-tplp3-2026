package py.edu.uc.lp3.msferreira.minecraft.service.exception;

public class ExplosionNoPreparadaException extends ExplosionException {
    public ExplosionNoPreparadaException(int tiempoExplosion, int tiempoTranscurrido) {
        super("El Creeper necesita " + tiempoExplosion
                + " segundos para explotar, pero solo transcurrieron " + tiempoTranscurrido + ".");
    }
}
