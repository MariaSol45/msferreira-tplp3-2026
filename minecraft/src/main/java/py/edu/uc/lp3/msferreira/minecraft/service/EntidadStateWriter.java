package py.edu.uc.lp3.msferreira.minecraft.service;

import org.springframework.beans.DirectFieldAccessor;

import py.edu.uc.lp3.msferreira.minecraft.domain.Aldeano;
import py.edu.uc.lp3.msferreira.minecraft.domain.Creeper;
import py.edu.uc.lp3.msferreira.minecraft.domain.Entidad;
import py.edu.uc.lp3.msferreira.minecraft.domain.EntidadHostil;
import py.edu.uc.lp3.msferreira.minecraft.domain.EntidadPasiva;
import py.edu.uc.lp3.msferreira.minecraft.domain.Jugador;

/** Escritura interna de estado; las decisiones y validaciones pertenecen a los servicios. */
final class EntidadStateWriter {
    private EntidadStateWriter() {
    }

    static void reemplazar(Entidad destino, Entidad origen) {
        escribir(destino, "salud", origen.getSalud());
        escribir(destino, "posicionX", origen.getPosicionX());
        escribir(destino, "posicionY", origen.getPosicionY());
        escribir(destino, "posicionZ", origen.getPosicionZ());
        escribir(destino, "velocidad", origen.getVelocidad());

        if (destino instanceof EntidadHostil hostilDestino && origen instanceof EntidadHostil hostilOrigen) {
            escribir(hostilDestino, "rangoDeteccion", hostilOrigen.getRangoDeteccion());
            escribir(hostilDestino, "danoAtaque", hostilOrigen.getDanoAtaque());
        }
        if (destino instanceof EntidadPasiva pasivaDestino && origen instanceof EntidadPasiva pasivaOrigen) {
            escribir(pasivaDestino, "domesticable", pasivaOrigen.isDomesticable());
        }
        if (destino instanceof Creeper creeperDestino && origen instanceof Creeper creeperOrigen) {
            escribir(creeperDestino, "tiempoExplosion", creeperOrigen.getTiempoExplosion());
        }
        if (destino instanceof Jugador jugadorDestino && origen instanceof Jugador jugadorOrigen) {
            escribir(jugadorDestino, "nombre", jugadorOrigen.getNombre());
            escribir(jugadorDestino, "nivelEXP", jugadorOrigen.getNivelEXP());
        }
        if (destino instanceof Aldeano aldeanoDestino && origen instanceof Aldeano aldeanoOrigen) {
            escribir(aldeanoDestino, "profesion", aldeanoOrigen.getProfesion());
        }
    }

    static void cambiarSalud(Entidad entidad, int salud) {
        escribir(entidad, "salud", salud);
    }

    private static void escribir(Object objetivo, String propiedad, Object valor) {
        new DirectFieldAccessor(objetivo).setPropertyValue(propiedad, valor);
    }
}
