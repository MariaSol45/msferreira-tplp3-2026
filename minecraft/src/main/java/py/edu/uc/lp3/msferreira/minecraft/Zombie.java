package py.edu.uc.lp3.msferreira.minecraft;

public class Zombie extends EntidadHostil {
    public Zombie(int salud, double posicionX, double posicionY, double posicionZ,
                  int velocidad, double rangoDeteccion, int danoAtaque) {
        super(salud, posicionX, posicionY, posicionZ, velocidad, rangoDeteccion, danoAtaque);
    }

    public void infectarAldeano() {
        System.out.println("El zombie infectó a un aldeano.");
    }
}
