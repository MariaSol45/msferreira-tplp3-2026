package py.edu.uc.lp3.msferreira.minecraft.domain;

public class Jugador extends Entidad implements UICapable {
    private String nombre;
    private int nivelEXP;

    public Jugador(String nombre) {
        this(nombre, 0, 20, 0, 0, 0, 1);
    }

    public Jugador(String nombre, int nivelEXP, int salud, double posicionX,
                   double posicionY, double posicionZ, int velocidad) {
        super(salud, posicionX, posicionY, posicionZ, velocidad);

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede ser nulo ni estar en blanco.");
        }

        if (nivelEXP < 0) {
            throw new IllegalArgumentException("El nivel de experiencia no puede ser negativo.");
        }

        this.nombre = nombre;
        this.nivelEXP = nivelEXP;
    }

    public String getNombre() {
        return nombre;
    }

    public int getNivelEXP() {
        return nivelEXP;
    }

    public void construir() {
        System.out.println(nombre + " está construyendo.");
    }

    public void craftear() {
        System.out.println(nombre + " está crafteando.");
    }

    public void interactuar() {
        System.out.println(nombre + " está interactuando.");
    }

    @Override
    public String getImagen() {
        return "jugador.png";
    }

    @Override
    public String getSonido() {
        return "Sonido del jugador";
    }

    @Override
    public String comportamiento() {
        return "El jugador " + nombre + " interactúa con el mundo con nivel de experiencia " + nivelEXP + ".";
    }
}
