package py.edu.uc.lp3.msferreira.minecraft.domain;

public class Aldeano extends EntidadPasiva implements UICapable {
    private String profesion;

    public Aldeano(int salud, double posicionX, double posicionY, double posicionZ,
                   int velocidad, boolean domesticable, String profesion) {
        super(salud, posicionX, posicionY, posicionZ, velocidad, domesticable);

        if (profesion == null || profesion.isBlank()) {
            throw new IllegalArgumentException("La profesión no puede ser nula ni estar en blanco.");
        }

        this.profesion = profesion;
    }

    public void comercio() {
        System.out.println("El aldeano de profesión " + profesion + " está comerciando.");
    }

    @Override
    public String getImagen() {
        return "aldeano.png";
    }

    @Override
    public String getSonido() {
        return "Sonido del aldeano";
    }

    @Override
    public String getMensajeUI() {
        return "El aldeano de profesión " + profesion + " está en " + getPosicion() + ".";
    }
}
