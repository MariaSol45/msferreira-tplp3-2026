package py.edu.uc.lp3.msferreira.minecraft.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "entidades")
public class Entidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int salud;
    private double posicionX;
    private double posicionY;
    private double posicionZ;
    private int velocidad;

    public Entidad() {
    }

    public Entidad(int salud, double posicionX, double posicionY, double posicionZ, int velocidad) {
        if (salud <= 0) {
            throw new IllegalArgumentException("La salud debe ser mayor que 0.");
        }

        if (velocidad < 0) {
            throw new IllegalArgumentException("La velocidad no puede ser negativa.");
        }

        if (!Double.isFinite(posicionX) || !Double.isFinite(posicionY) || !Double.isFinite(posicionZ)) {
            throw new IllegalArgumentException("Las coordenadas deben ser valores finitos.");
        }

        this.salud = salud;
        this.posicionX = posicionX;
        this.posicionY = posicionY;
        this.posicionZ = posicionZ;
        this.velocidad = velocidad;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getSalud() {
        return salud;
    }

    public void setSalud(int salud) {
        this.salud = salud;
    }

    public double getPosicionX() {
        return posicionX;
    }

    public void setPosicionX(double posicionX) {
        this.posicionX = posicionX;
    }

    public double getPosicionY() {
        return posicionY;
    }

    public void setPosicionY(double posicionY) {
        this.posicionY = posicionY;
    }

    public double getPosicionZ() {
        return posicionZ;
    }

    public void setPosicionZ(double posicionZ) {
        this.posicionZ = posicionZ;
    }

    public String getPosicion() {
        return "(" + posicionX + ", " + posicionY + ", " + posicionZ + ")";
    }

    public int getVelocidad() {
        return velocidad;
    }

    public void setVelocidad(int velocidad) {
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

    public String comportamiento() {
        return "La entidad se comporta de forma neutral.";
    }
}
