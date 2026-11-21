package py.edu.uc.lp3.msferreira.minecraft.domain;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Jugada registrada por el escenario de POO-09: una explosión de Creeper que aceptaron A2 y A3.
 * Solo guarda estado; las validaciones viven en JugadaService.
 */
@Entity
@Table(name = "jugadas")
public class Jugada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo;
    private Long creeperId;
    private Long jugadorId;
    private Long aldeanoId;
    private int tiempoTranscurrido;
    private int saludFinalJugador;
    private int saludFinalAldeano;
    private String aceptadaPorA2;
    private String aceptadaPorA3;
    private Instant registradaEn;

    protected Jugada() {
    }

    public Jugada(String tipo, Long creeperId, Long jugadorId, Long aldeanoId, int tiempoTranscurrido,
                  int saludFinalJugador, int saludFinalAldeano, String aceptadaPorA2, String aceptadaPorA3,
                  Instant registradaEn) {
        this.tipo = tipo;
        this.creeperId = creeperId;
        this.jugadorId = jugadorId;
        this.aldeanoId = aldeanoId;
        this.tiempoTranscurrido = tiempoTranscurrido;
        this.saludFinalJugador = saludFinalJugador;
        this.saludFinalAldeano = saludFinalAldeano;
        this.aceptadaPorA2 = aceptadaPorA2;
        this.aceptadaPorA3 = aceptadaPorA3;
        this.registradaEn = registradaEn;
    }

    public Long getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public Long getCreeperId() {
        return creeperId;
    }

    public Long getJugadorId() {
        return jugadorId;
    }

    public Long getAldeanoId() {
        return aldeanoId;
    }

    public int getTiempoTranscurrido() {
        return tiempoTranscurrido;
    }

    public int getSaludFinalJugador() {
        return saludFinalJugador;
    }

    public int getSaludFinalAldeano() {
        return saludFinalAldeano;
    }

    public String getAceptadaPorA2() {
        return aceptadaPorA2;
    }

    public String getAceptadaPorA3() {
        return aceptadaPorA3;
    }

    public Instant getRegistradaEn() {
        return registradaEn;
    }
}
