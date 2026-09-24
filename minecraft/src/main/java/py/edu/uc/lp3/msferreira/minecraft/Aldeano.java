package py.edu.uc.lp3.msferreira.minecraft;

public class Aldeano extends EntidadPasiva {
    private String profesion;

    public Aldeano(int salud, double posicionX, double posicionY, double posicionZ,
                   int velocidad, boolean domesticable, String profesion) {
        super(salud, posicionX, posicionY, posicionZ, velocidad, domesticable);
        this.profesion = profesion;
    }

    public void comercio() {
        System.out.println("El aldeano de profesión " + profesion + " está comerciando.");
    }
}
