package py.edu.uc.lp3.msferreira.minecraft.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "entidades_hostiles")
public abstract class EntidadHostil extends Entidad {
    private double rangoDeteccion;
    private int danoAtaque;

    protected EntidadHostil() {
    }

    public EntidadHostil(int salud, double posicionX, double posicionY, double posicionZ,
                         int velocidad, double rangoDeteccion, int danoAtaque) {
        super(salud, posicionX, posicionY, posicionZ, velocidad);

        if (!Double.isFinite(rangoDeteccion) || rangoDeteccion < 0) {
            throw new IllegalArgumentException("El rango de detección debe ser finito y no negativo.");
        }

        if (danoAtaque <= 0) {
            throw new IllegalArgumentException("El daño de ataque debe ser mayor que 0.");
        }

        this.rangoDeteccion = rangoDeteccion;
        this.danoAtaque = danoAtaque;
    }

    public double getRangoDeteccion() {
        return rangoDeteccion;
    }

    public int getDanoAtaque() {
        return danoAtaque;
    }

    @Override
    protected void actualizarDatosEspecificos(Entidad datos) {
        EntidadHostil hostil = (EntidadHostil) datos;
        rangoDeteccion = hostil.rangoDeteccion;
        danoAtaque = hostil.danoAtaque;
    }

    @Override
    public void validarEstado() {
        super.validarEstado();
        if (!Double.isFinite(rangoDeteccion) || rangoDeteccion < 0) {
            throw new IllegalArgumentException("El rango de detección debe ser finito y no negativo.");
        }
        if (danoAtaque <= 0) {
            throw new IllegalArgumentException("El daño de ataque debe ser mayor que 0.");
        }
    }

    public void atacar(Entidad objetivo) {
        if (objetivo == null) {
            throw new IllegalArgumentException("El objetivo no puede ser nulo.");
        }

        System.out.println("La entidad hostil está atacando.");
        objetivo.recibirDano(danoAtaque);
    }

    public void atacar(Entidad objetivo, double distancia) {
        if (!Double.isFinite(distancia) || distancia < 0) {
            throw new IllegalArgumentException("La distancia debe ser finita y no negativa.");
        }

        if (distancia > rangoDeteccion) {
            throw new IllegalArgumentException("El objetivo está fuera del rango de detección.");
        }

        atacar(objetivo);
    }
    @Override
    public abstract String comportamiento();
}
