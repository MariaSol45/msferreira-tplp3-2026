package py.edu.uc.lp3.msferreira.minecraft;

public class Animal extends EntidadPasiva {
    private boolean montar;

    public Animal(int salud, double posicionX, double posicionY, double posicionZ,
                  int velocidad, boolean domesticable, boolean montar) {
        super(salud, posicionX, posicionY, posicionZ, velocidad, domesticable);
        this.montar = montar;
    }
}
