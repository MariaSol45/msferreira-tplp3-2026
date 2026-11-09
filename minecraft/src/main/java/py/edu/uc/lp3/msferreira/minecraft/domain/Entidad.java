package py.edu.uc.lp3.msferreira.minecraft.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
        setSalud(salud);
        setPosicionX(posicionX);
        setPosicionY(posicionY);
        setPosicionZ(posicionZ);
        setVelocidad(velocidad);
    }

    public Long getId() {
        return id;
    }

    public int getSalud() {
        return salud;
    }

    public void setSalud(int salud) {
        if (salud < 0) {
            throw new IllegalArgumentException("La salud no puede ser negativa.");
        }
        this.salud = salud;
    }

    public double getPosicionX() {
        return posicionX;
    }

    public void setPosicionX(double posicionX) {
        validarCoordenada(posicionX);
        this.posicionX = posicionX;
    }

    public double getPosicionY() {
        return posicionY;
    }

    public void setPosicionY(double posicionY) {
        validarCoordenada(posicionY);
        this.posicionY = posicionY;
    }

    public double getPosicionZ() {
        return posicionZ;
    }

    public void setPosicionZ(double posicionZ) {
        validarCoordenada(posicionZ);
        this.posicionZ = posicionZ;
    }

    public String getPosicion() {
        return "(" + posicionX + ", " + posicionY + ", " + posicionZ + ")";
    }

    public double distanciaA(Entidad otra) {
        if (otra == null) {
            throw new IllegalArgumentException("La otra entidad no puede ser nula.");
        }

        double diferenciaX = posicionX - otra.posicionX;
        double diferenciaY = posicionY - otra.posicionY;
        double diferenciaZ = posicionZ - otra.posicionZ;
        return Math.sqrt(diferenciaX * diferenciaX + diferenciaY * diferenciaY + diferenciaZ * diferenciaZ);
    }

    public int getVelocidad() {
        return velocidad;
    }

    public void setVelocidad(int velocidad) {
        if (velocidad < 0) {
            throw new IllegalArgumentException("La velocidad no puede ser negativa.");
        }
        this.velocidad = velocidad;
    }

    public final void actualizarDesde(Entidad datos) {
        if (datos == null) {
            throw new IllegalArgumentException("Los datos de la entidad son obligatorios.");
        }
        if (!getClass().equals(datos.getClass())) {
            throw new IllegalArgumentException("No se puede cambiar el tipo de una entidad existente.");
        }

        datos.validarEstado();
        setSalud(datos.getSalud());
        setPosicionX(datos.getPosicionX());
        setPosicionY(datos.getPosicionY());
        setPosicionZ(datos.getPosicionZ());
        setVelocidad(datos.getVelocidad());
        actualizarDatosEspecificos(datos);
        validarEstado();
    }

    protected void actualizarDatosEspecificos(Entidad datos) {
    }

    public void validarEstado() {
        if (salud < 0) {
            throw new IllegalArgumentException("La salud no puede ser negativa.");
        }
        if (velocidad < 0) {
            throw new IllegalArgumentException("La velocidad no puede ser negativa.");
        }
        validarCoordenada(posicionX);
        validarCoordenada(posicionY);
        validarCoordenada(posicionZ);
    }

    @PrePersist
    @PreUpdate
    private void validarAntesDePersistir() {
        validarEstado();
    }

    private static void validarCoordenada(double coordenada) {
        if (!Double.isFinite(coordenada)) {
            throw new IllegalArgumentException("Las coordenadas deben ser valores finitos.");
        }
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
