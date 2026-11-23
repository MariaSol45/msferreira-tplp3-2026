package py.edu.uc.lp3.msferreira.minecraft.service.exception;

public class CreeperYaRegistradoException extends ExplosionException {
    public CreeperYaRegistradoException(Long creeperId) {
        super("Ya existe una jugada de explosión para el Creeper con id " + creeperId + ".");
    }
}
