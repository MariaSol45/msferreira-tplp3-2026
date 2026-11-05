package py.edu.uc.lp3.msferreira.minecraft.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "aldeanos")
@JsonTypeName("aldeano")
public class Aldeano extends EntidadPasiva implements UICapable {
    private String profesion;

    protected Aldeano() {
    }

    public Aldeano(int salud, String profesion) {
        this(salud, 0, 0, 0, 1, true, profesion);
    }

    @JsonCreator
    public Aldeano(
            @JsonProperty("salud") int salud,
            @JsonProperty("posicionX") double posicionX,
            @JsonProperty("posicionY") double posicionY,
            @JsonProperty("posicionZ") double posicionZ,
            @JsonProperty("velocidad") int velocidad,
            @JsonProperty("domesticable") boolean domesticable,
            @JsonProperty("profesion") String profesion) {
        super(salud, posicionX, posicionY, posicionZ, velocidad, domesticable);

        if (profesion == null || profesion.isBlank()) {
            throw new IllegalArgumentException("La profesión no puede ser nula ni estar en blanco.");
        }

        this.profesion = profesion;
    }

    public String getProfesion() {
        return profesion;
    }

    @Override
    protected void actualizarDatosEspecificos(Entidad datos) {
        super.actualizarDatosEspecificos(datos);
        profesion = ((Aldeano) datos).profesion;
    }

    @Override
    public void validarEstado() {
        super.validarEstado();
        if (profesion == null || profesion.isBlank()) {
            throw new IllegalArgumentException("La profesión no puede ser nula ni estar en blanco.");
        }
    }

    public void comercio() {
        System.out.println("El aldeano de profesión " + profesion + " está comerciando.");
    }

    @Override
    public String getImagen() {
        return "aldeano.png";
    }

    @Override
    public String getSonido() {
        return "Sonido del aldeano";
    }

    @Override
    public String getMensajeUI() {
        return "El aldeano de profesión " + profesion + " está en " + getPosicion() + ".";
    }

    @Override
    public String comportamiento() {
        return "El aldeano de profesión " + profesion + " comercia con los jugadores.";
    }
}
