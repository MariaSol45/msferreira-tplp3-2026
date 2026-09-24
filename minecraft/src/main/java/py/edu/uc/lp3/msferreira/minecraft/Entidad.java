package py.edu.uc.lp3.msferreira.minecraft;

public abstract class Entidad {
	private int salud;
	private double posicionX;
	private double posicionY;
	private double posicionZ;
	private int velocidad;

    public Entidad(int salud, double posicionX, double posicionY, double posicionZ, int velocidad) {

	if (salud <= 0) {
    	throw new IllegalArgumentException("La salud debe ser mayor que 0.");
	}

	if (velocidad < 0) {
    	throw new IllegalArgumentException("La velocidad no puede ser negativa.");
	}	

        this.salud = salud;
        this.posicionX = posicionX;
        this.posicionY = posicionY;
        this.posicionZ = posicionZ;
        this.velocidad = velocidad;
    }

    public void mover() {
        System.out.println("La entidad se está moviendo.");
    }

    public void recibirDano(int cantidad) {
    if (cantidad <= 0) {
        return;
    }

    salud = Math.max(0, salud - cantidad);

    if (salud == 0) {
        morir();
    }
}

    public void morir() {
        System.out.println("La entidad ha muerto.");
    }
public abstract String comportamiento();
}
