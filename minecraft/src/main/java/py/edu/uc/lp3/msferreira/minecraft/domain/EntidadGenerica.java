package py.edu.uc.lp3.msferreira.minecraft.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Conserva las entidades neutrales creadas antes de que la raíz fuera abstracta. */
@Entity
@Table(name = "entidades_genericas")
@JsonTypeName("entidad")
public class EntidadGenerica extends Entidad {
    protected EntidadGenerica() {
    }

    @JsonCreator
    public EntidadGenerica(
            @JsonProperty("salud") int salud,
            @JsonProperty("posicionX") double posicionX,
            @JsonProperty("posicionY") double posicionY,
            @JsonProperty("posicionZ") double posicionZ,
            @JsonProperty("velocidad") int velocidad) {
        super(salud, posicionX, posicionY, posicionZ, velocidad);
    }

    @Override
    public String comportamiento() {
        return "La entidad se comporta de forma neutral.";
    }
}
