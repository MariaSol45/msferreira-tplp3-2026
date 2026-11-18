package py.edu.uc.lp3.msferreira.minecraft.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "creepers")
@JsonTypeName("creeper")
public class Creeper extends EntidadHostil {
    private static final double RADIO_EXPLOSION_PREDETERMINADO = 5;
    private static final int DANO_EXPLOSION_PREDETERMINADO = 8;
    private int tiempoExplosion;

    protected Creeper() {
    }

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

    @JsonCreator
    public Creeper(
            @JsonProperty("salud") int salud,
            @JsonProperty("posicionX") double posicionX,
            @JsonProperty("posicionY") double posicionY,
            @JsonProperty("posicionZ") double posicionZ,
            @JsonProperty("velocidad") int velocidad,
            @JsonProperty("rangoDeteccion") double rangoDeteccion,
            @JsonProperty("danoAtaque") int danoAtaque,
            @JsonProperty("tiempoExplosion") int tiempoExplosion) {
        super(salud, posicionX, posicionY, posicionZ, velocidad, rangoDeteccion, danoAtaque);
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

    @Override
    public String comportamiento() {
        return "El creeper se acerca en silencio y explota en " + tiempoExplosion + " segundos.";
    }
}
