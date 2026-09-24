package py.edu.uc.lp3.msferreira.minecraft;

public class Creeper extends EntidadHostil {
    private int tiempoExplosion;

    public Creeper(int salud, double posicionX, double posicionY, double posicionZ,
                   int velocidad, double rangoDeteccion, int danoAtaque, int tiempoExplosion) {
        super(salud, posicionX, posicionY, posicionZ, velocidad, rangoDeteccion, danoAtaque);
        this.tiempoExplosion = tiempoExplosion;
    }

    public void explotar() {
        System.out.println("El creeper explotará en " + tiempoExplosion + " segundos.");
    }
}
