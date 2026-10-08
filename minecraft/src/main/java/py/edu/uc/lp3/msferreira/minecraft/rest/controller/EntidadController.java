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

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> manejarEntradaInvalida(IllegalArgumentException excepcion) {
        Map<String, String> respuesta = new LinkedHashMap<>();
        respuesta.put("error", "Entrada inválida");
        respuesta.put("mensaje", excepcion.getMessage());
        return respuesta;
    }
}
