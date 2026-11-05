package py.edu.uc.lp3.msferreira.minecraft.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import py.edu.uc.lp3.msferreira.minecraft.domain.exception.ExplosionNoPreparadaException;
import py.edu.uc.lp3.msferreira.minecraft.domain.exception.ObjetivoFueraDeRangoException;

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

        if (tiempoExplosion <= 0) {
            throw new IllegalArgumentException("El tiempo de explosión debe ser mayor que 0.");
        }

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

    @Override
    protected void actualizarDatosEspecificos(Entidad datos) {
        super.actualizarDatosEspecificos(datos);
        tiempoExplosion = ((Creeper) datos).tiempoExplosion;
    }

    @Override
    public void validarEstado() {
        super.validarEstado();
        if (tiempoExplosion <= 0) {
            throw new IllegalArgumentException("El tiempo de explosión debe ser mayor que 0.");
        }
    }

    public void explotar() {
        System.out.println("El creeper explotará en " + tiempoExplosion + " segundos.");
    }

    public void explotar(int tiempoTranscurrido) {
        if (tiempoTranscurrido < tiempoExplosion) {
            throw new ExplosionNoPreparadaException(tiempoExplosion, tiempoTranscurrido);
        }
    }

    public void validarObjetivoDentroDelRadio(Entidad objetivo) {
        double distancia = distanciaA(objetivo);
        if (distancia > getRadioExplosion()) {
            throw new ObjetivoFueraDeRangoException(
                    objetivo.getClass().getSimpleName(), distancia, getRadioExplosion()
            );
        }
    }

    public int aplicarDanoExplosion(Entidad objetivo) {
        validarObjetivoDentroDelRadio(objetivo);
        int saludInicial = objetivo.getSalud();
        objetivo.recibirDano(getDanoExplosion());
        return saludInicial - objetivo.getSalud();
    }

    @Override
    public String comportamiento() {
        return "El creeper se acerca en silencio y explota en " + tiempoExplosion + " segundos.";
    }
}
