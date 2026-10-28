package py.edu.uc.lp3.msferreira.minecraft.domain;

public class Zombie extends EntidadHostil {
    public Zombie(int salud) {
        this(salud, 0, 0, 0, 1, 10, 3);
    }

    public Zombie(int salud, double posicionX, double posicionY, double posicionZ,
                  int velocidad, double rangoDeteccion, int danoAtaque) {
        super(salud, posicionX, posicionY, posicionZ, velocidad, rangoDeteccion, danoAtaque);
    }

    public void infectarAldeano() {
        System.out.println("El zombie infectó a un aldeano.");
    }

    @Override
    public String comportamiento() {
        return "El zombie persigue aldeanos hasta " + getRangoDeteccion() + " bloques para infectarlos.";
    }
}
