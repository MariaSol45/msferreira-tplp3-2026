package py.edu.uc.lp3.msferreira.minecraft;

public class Esqueleto extends EntidadHostil {
    public Esqueleto(int salud, double posicionX, double posicionY, double posicionZ,
                     int velocidad, double rangoDeteccion, int danoAtaque) {
        super(salud, posicionX, posicionY, posicionZ, velocidad, rangoDeteccion, danoAtaque);
    }

    public void dispararFlecha() {
        System.out.println("El esqueleto disparó una flecha.");
    }
}
