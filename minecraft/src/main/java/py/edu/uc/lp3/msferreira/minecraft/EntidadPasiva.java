package py.edu.uc.lp3.msferreira.minecraft;

public class EntidadPasiva extends Entidad {
    private boolean domesticable;

    public EntidadPasiva(int salud, double posicionX, double posicionY, double posicionZ,
                         int velocidad, boolean domesticable) {
        super(salud, posicionX, posicionY, posicionZ, velocidad);
        this.domesticable = domesticable;
    }

    public void huir(Entidad amenaza) {
        System.out.println("La entidad pasiva está huyendo de una amenaza.");
    }
@Override
public String comportamiento() {
    return "La entidad pasiva huye.";
}
}
