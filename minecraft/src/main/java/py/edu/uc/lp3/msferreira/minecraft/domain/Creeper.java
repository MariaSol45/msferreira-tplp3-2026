package py.edu.uc.lp3.msferreira.minecraft.domain;

public class Creeper extends EntidadHostil {
    private int tiempoExplosion;

    public Creeper(int salud, double posicionX, double posicionY, double posicionZ,
                   int velocidad, double rangoDeteccion, int danoAtaque) {
        this(salud, posicionX, posicionY, posicionZ, velocidad, rangoDeteccion, danoAtaque, 3);
    }

    public Creeper(int salud, double posicionX, double posicionY, double posicionZ,
                   int velocidad, double rangoDeteccion, int danoAtaque, int tiempoExplosion) {
        super(salud, posicionX, posicionY, posicionZ, velocidad, rangoDeteccion, danoAtaque);

        if (tiempoExplosion <= 0) {
            throw new IllegalArgumentException("El tiempo de explosión debe ser mayor que 0.");
        }

        this.tiempoExplosion = tiempoExplosion;
    }

    public void explotar() {
        System.out.println("El creeper explotará en " + tiempoExplosion + " segundos.");
    }
}
