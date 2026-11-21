package py.edu.uc.lp3.msferreira.minecraft.service.exception;

/** El compañero respondió 4xx: su regla no acepta la jugada. */
public class RechazoCompaneroException extends CompaneroException {
    private final int estadoCompanero;
    private final String respuestaCompanero;

    public RechazoCompaneroException(String rol, String companero, int estadoCompanero, String respuestaCompanero) {
        super(rol, companero, "La jugada fue rechazada por " + rol + " (" + companero + ").", null);
        this.estadoCompanero = estadoCompanero;
        this.respuestaCompanero = respuestaCompanero;
    }

    public int getEstadoCompanero() {
        return estadoCompanero;
    }

    public String getRespuestaCompanero() {
        return respuestaCompanero;
    }
}
