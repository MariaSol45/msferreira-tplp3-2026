package py.edu.uc.lp3.msferreira.minecraft.domain;

public interface UICapable {
    String getImagen();

    String getPosicion();

    String getSonido();

    int getVelocidad();

    default String getMensajeUI() {
        return "Entidad visible en " + getPosicion()
                + " con velocidad " + getVelocidad() + ".";
    }
}
