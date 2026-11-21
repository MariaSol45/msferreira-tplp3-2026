package py.edu.uc.lp3.msferreira.minecraft.service.exception;

/** El compañero respondió 4xx: su regla no acepta la jugada. */
public class RechazoCompaneroException extends CompaneroException {
    private final int estadoCompanero;
    private final String respuestaCompanero;
    private final String motivo;

    public RechazoCompaneroException(String rol, String companero, int estadoCompanero, String respuestaCompanero) {
        this(rol, companero, estadoCompanero, respuestaCompanero, null);
    }

    public RechazoCompaneroException(String rol, String companero, int estadoCompanero, String respuestaCompanero,
                                     String motivo) {
        super(rol, companero, "La jugada fue rechazada por " + rol + " (" + companero + ")."
                + (motivo == null || motivo.isBlank() ? "" : " Motivo: " + motivo + "."), null);
        this.estadoCompanero = estadoCompanero;
        this.respuestaCompanero = respuestaCompanero;
        this.motivo = motivo;
    }

    public int getEstadoCompanero() {
        return estadoCompanero;
    }

    public String getRespuestaCompanero() {
        return respuestaCompanero;
    }

    public String getMotivo() {
        return motivo;
    }
}
