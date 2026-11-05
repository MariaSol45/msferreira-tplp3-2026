package py.edu.uc.lp3.msferreira.minecraft.domain;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "jugadores")
@JsonTypeName("jugador")
public class Jugador extends Entidad implements UICapable {
    private String nombre;
    private int nivelEXP;

    protected Jugador() {
    }

    public Jugador(String nombre) {
        this(nombre, 0, 20, 0, 0, 0, 1);
    }

    @JsonCreator
    public Jugador(
            @JsonProperty("nombre") String nombre,
            @JsonProperty("nivelEXP") @JsonAlias("experiencia") int nivelEXP,
            @JsonProperty("salud") int salud,
            @JsonProperty("posicionX") double posicionX,
            @JsonProperty("posicionY") double posicionY,
            @JsonProperty("posicionZ") double posicionZ,
            @JsonProperty("velocidad") int velocidad) {
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

    @Override
    protected void actualizarDatosEspecificos(Entidad datos) {
        Jugador jugador = (Jugador) datos;
        nombre = jugador.nombre;
        nivelEXP = jugador.nivelEXP;
    }

    @Override
    public void validarEstado() {
        super.validarEstado();
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede ser nulo ni estar en blanco.");
        }
        if (nivelEXP < 0) {
            throw new IllegalArgumentException("El nivel de experiencia no puede ser negativo.");
        }
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
