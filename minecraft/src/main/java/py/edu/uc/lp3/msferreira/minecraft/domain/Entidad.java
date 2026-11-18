package py.edu.uc.lp3.msferreira.minecraft.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity
@Table(name = "entidades")
@Inheritance(strategy = InheritanceType.JOINED)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "tipo"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = EntidadGenerica.class, name = "entidad"),
        @JsonSubTypes.Type(value = Creeper.class, name = "creeper"),
        @JsonSubTypes.Type(value = Jugador.class, name = "jugador"),
        @JsonSubTypes.Type(value = Aldeano.class, name = "aldeano")
})
public abstract class Entidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int salud;
    private double posicionX;
    private double posicionY;
    private double posicionZ;
    private int velocidad;

    protected Entidad() {
    }

    protected Entidad(int salud, double posicionX, double posicionY, double posicionZ, int velocidad) {
        this.salud = salud;
        this.posicionX = posicionX;
        this.posicionY = posicionY;
        this.posicionZ = posicionZ;
        this.velocidad = velocidad;
    }

    public Long getId() {
        return id;
    }

    public int getSalud() {
        return salud;
    }

    public double getPosicionX() {
        return posicionX;
    }

    public double getPosicionY() {
        return posicionY;
    }

    public double getPosicionZ() {
        return posicionZ;
    }

    public String getPosicion() {
        return "(" + posicionX + ", " + posicionY + ", " + posicionZ + ")";
    }

    public double distanciaA(Entidad otra) {
        double diferenciaX = posicionX - otra.posicionX;
        double diferenciaY = posicionY - otra.posicionY;
        double diferenciaZ = posicionZ - otra.posicionZ;
        return Math.hypot(Math.hypot(diferenciaX, diferenciaY), diferenciaZ);
    }

    public int getVelocidad() {
        return velocidad;
    }

    public void mover() {
        System.out.println("La entidad se está moviendo.");
    }

    public void morir() {
        System.out.println("La entidad ha muerto.");
    }

    public abstract String comportamiento();
}
