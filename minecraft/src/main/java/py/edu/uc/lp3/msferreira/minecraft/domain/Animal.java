package py.edu.uc.lp3.msferreira.minecraft.domain;

public class Animal extends EntidadPasiva {
    private boolean montar;

    public Animal(int salud) {
        this(salud, 0, 0, 0, 1, false, false);
    }

    public Animal(int salud, double posicionX, double posicionY, double posicionZ,
                  int velocidad, boolean domesticable, boolean montar) {
        super(salud, posicionX, posicionY, posicionZ, velocidad, domesticable);
        this.montar = montar;
    }

    public boolean isMontar() {
        return montar;
    }

    @Override
    public String comportamiento() {
        if (isDomesticable() && montar) {
            return "El animal domesticado acompaña al jugador y puede ser montado.";
        }
        if (isDomesticable()) {
            return "El animal domesticado acompaña al jugador.";
        }
        return "El animal salvaje recorre el entorno y huye de las amenazas.";
    }
}
