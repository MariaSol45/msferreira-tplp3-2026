package py.edu.uc.lp3.msferreira.minecraft;

public class EntidadHostil extends Entidad {
    private double rangoDeteccion;
    private int danoAtaque;

    public EntidadHostil(int salud, double posicionX, double posicionY, double posicionZ,
                         int velocidad, double rangoDeteccion, int danoAtaque) {
        super(salud, posicionX, posicionY, posicionZ, velocidad);

	if (rangoDeteccion < 0) {
    	throw new IllegalArgumentException("El rango de detección no puede ser negativo.");
	}

	if (danoAtaque < 0) {
    	throw new IllegalArgumentException("El daño de ataque no puede ser negativo.");
	}

        this.rangoDeteccion = rangoDeteccion;
        this.danoAtaque = danoAtaque;
    }

    public void atacar(Entidad objetivo) {
        System.out.println("La entidad hostil está atacando.");
        objetivo.recibirDano(danoAtaque);
    }
@Override
public String comportamiento() {
    return "La entidad hostil ataca.";
}
}
