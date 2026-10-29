package py.edu.uc.lp3.msferreira.minecraft.domain.exception;

public class ObjetivoFueraDeRangoException extends ExplosionException {
    public ObjetivoFueraDeRangoException(String tipoObjetivo, double distancia, double radioExplosion) {
        super("El objetivo " + tipoObjetivo + " está a " + distancia
                + " bloques y queda fuera del radio de explosión de " + radioExplosion + " bloques.");
    }
}
