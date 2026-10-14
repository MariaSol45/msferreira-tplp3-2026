package py.edu.uc.lp3.msferreira.minecraft.rest.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import py.edu.uc.lp3.msferreira.minecraft.domain.Aldeano;
import py.edu.uc.lp3.msferreira.minecraft.domain.Creeper;
import py.edu.uc.lp3.msferreira.minecraft.domain.Entidad;
import py.edu.uc.lp3.msferreira.minecraft.domain.Jugador;
import py.edu.uc.lp3.msferreira.minecraft.domain.UICapable;

@RestController
public class EntidadController {

    @GetMapping("/entidad")
    public Map<String, Object> crearEntidad(
            @RequestParam String tipo,
            @RequestParam int salud,
            @RequestParam(defaultValue = "0") double x,
            @RequestParam(defaultValue = "0") double y,
            @RequestParam(defaultValue = "0") double z,
            @RequestParam(defaultValue = "1") int velocidad,
            @RequestParam(defaultValue = "10") double rango,
            @RequestParam(defaultValue = "5") int dano,
            @RequestParam(defaultValue = "3") int tiempoExplosion,
            @RequestParam(required = false) String profesion) {

        Entidad entidad;

        if (tipo.equalsIgnoreCase("creeper")) {
            entidad = new Creeper(
                    salud, x, y, z, velocidad,
                    rango, dano, tiempoExplosion
            );
        } else if (tipo.equalsIgnoreCase("aldeano")) {
            entidad = new Aldeano(
                    salud, x, y, z, velocidad,
                    true, profesion == null ? "Granjero" : profesion
            );
        } else {
            throw new IllegalArgumentException(
                    "Tipo válido: creeper o aldeano"
            );
        }

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("tipo", entidad.getClass().getSimpleName());
        respuesta.put("comportamiento", entidad.comportamiento());

        return respuesta;
    }

    @GetMapping("/ui")
    public Map<String, Object> obtenerDatosUI(
            @RequestParam String tipo,
            @RequestParam(defaultValue = "20") int salud,
            @RequestParam(defaultValue = "0") double x,
            @RequestParam(defaultValue = "0") double y,
            @RequestParam(defaultValue = "0") double z,
            @RequestParam(defaultValue = "1") int velocidad,
            @RequestParam(defaultValue = "Steve") String nombre,
            @RequestParam(defaultValue = "0") int experiencia,
            @RequestParam(defaultValue = "Granjero") String profesion) {

        UICapable elementoUI;

        if (tipo.equalsIgnoreCase("jugador")) {
            elementoUI = new Jugador(
                    nombre, experiencia, salud, x, y, z, velocidad
            );
        } else if (tipo.equalsIgnoreCase("aldeano")) {
            elementoUI = new Aldeano(
                    salud, x, y, z, velocidad, true, profesion
            );
        } else {
            throw new IllegalArgumentException(
                    "Tipo válido: jugador o aldeano"
            );
        }

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("tipo", elementoUI.getClass().getSimpleName());
        respuesta.put("imagen", elementoUI.getImagen());
        respuesta.put("posicion", elementoUI.getPosicion());
        respuesta.put("sonido", elementoUI.getSonido());
        respuesta.put("velocidad", elementoUI.getVelocidad());
        respuesta.put("mensaje", elementoUI.getMensajeUI());

        return respuesta;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> manejarEntradaInvalida(IllegalArgumentException excepcion) {
        Map<String, String> respuesta = new LinkedHashMap<>();
        respuesta.put("error", "Entrada inválida");
        respuesta.put("mensaje", excepcion.getMessage());
        return respuesta;
    }
}
