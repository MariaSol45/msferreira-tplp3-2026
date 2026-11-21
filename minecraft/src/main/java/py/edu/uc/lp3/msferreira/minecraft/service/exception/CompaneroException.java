package py.edu.uc.lp3.msferreira.minecraft.service.exception;

/** Base de los errores que vienen de consultar a un compañero del grupo (A2 o A3). */
public abstract class CompaneroException extends RuntimeException {
    private final String rol;
    private final String companero;

    protected CompaneroException(String rol, String companero, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.rol = rol;
        this.companero = companero;
    }

    public String getRol() {
        return rol;
    }

    public String getCompanero() {
        return companero;
    }
}
