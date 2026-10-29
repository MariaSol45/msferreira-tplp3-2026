package py.edu.uc.lp3.msferreira.minecraft.domain;

import py.edu.uc.lp3.msferreira.minecraft.domain.exception.ExplosionNoPreparadaException;
import py.edu.uc.lp3.msferreira.minecraft.domain.exception.ObjetivoFueraDeRangoException;

public class Creeper extends EntidadHostil {
    private static final double RADIO_EXPLOSION_PREDETERMINADO = 5;
    private static final int DANO_EXPLOSION_PREDETERMINADO = 8;
    private int tiempoExplosion;

    public Creeper(int salud, double posicionX, double posicionY, double posicionZ, int velocidad) {
        this(
                salud, posicionX, posicionY, posicionZ, velocidad,
                RADIO_EXPLOSION_PREDETERMINADO, DANO_EXPLOSION_PREDETERMINADO
        );
    }

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

    public int getTiempoExplosion() {
        return tiempoExplosion;
    }

    public double getRadioExplosion() {
        return getRangoDeteccion();
    }

    public int getDanoExplosion() {
        return getDanoAtaque();
    }

    public void explotar() {
        System.out.println("El creeper explotará en " + tiempoExplosion + " segundos.");
    }

    public void explotar(int tiempoTranscurrido) {
        if (tiempoTranscurrido < tiempoExplosion) {
            throw new ExplosionNoPreparadaException(tiempoExplosion, tiempoTranscurrido);
        }
    }

    public void validarObjetivoDentroDelRadio(Entidad objetivo) {
        double distancia = distanciaA(objetivo);
        if (distancia > getRadioExplosion()) {
            throw new ObjetivoFueraDeRangoException(
                    objetivo.getClass().getSimpleName(), distancia, getRadioExplosion()
            );
        }
    }

    public int aplicarDanoExplosion(Entidad objetivo) {
        validarObjetivoDentroDelRadio(objetivo);
        int saludInicial = objetivo.getSalud();
        objetivo.recibirDano(getDanoExplosion());
        return saludInicial - objetivo.getSalud();
    }

    @Override
    public String comportamiento() {
        return "El creeper se acerca en silencio y explota en " + tiempoExplosion + " segundos.";
    }
}
