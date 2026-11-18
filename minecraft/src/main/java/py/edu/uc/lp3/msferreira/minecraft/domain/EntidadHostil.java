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
    public abstract String comportamiento();
}
