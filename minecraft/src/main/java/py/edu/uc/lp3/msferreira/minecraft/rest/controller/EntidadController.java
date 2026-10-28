package py.edu.uc.lp3.msferreira.minecraft.rest.controller;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import py.edu.uc.lp3.msferreira.minecraft.domain.Aldeano;
import py.edu.uc.lp3.msferreira.minecraft.domain.Creeper;
import py.edu.uc.lp3.msferreira.minecraft.domain.Entidad;
import py.edu.uc.lp3.msferreira.minecraft.domain.Jugador;
import py.edu.uc.lp3.msferreira.minecraft.domain.UICapable;
import py.edu.uc.lp3.msferreira.minecraft.service.EntidadService;

@RestController
public class EntidadController {
    private final EntidadService entidadService;

    public EntidadController(EntidadService entidadService) {
        this.entidadService = entidadService;
    }

    @GetMapping("/entidades")
    public List<Entidad> listar() {
        return entidadService.listar();
    }

    @GetMapping("/entidades/{id}")
    public Entidad obtener(@PathVariable Long id) {
        return entidadService.obtener(id);
    }

    @PostMapping("/entidades")
    public ResponseEntity<Entidad> crear(@RequestBody Entidad entidad) {
        Entidad creada = entidadService.crear(entidad);
        return ResponseEntity.created(URI.create("/entidades/" + creada.getId())).body(creada);
    }

    @PutMapping("/entidades/{id}")
    public Entidad actualizar(@PathVariable Long id, @RequestBody Entidad entidad) {
        return entidadService.actualizar(id, entidad);
    }

    @DeleteMapping("/entidades/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        entidadService.eliminar(id);
    }

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

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> manejarEntidadNoEncontrada(NoSuchElementException excepcion) {
        Map<String, String> respuesta = new LinkedHashMap<>();
        respuesta.put("error", "Entidad no encontrada");
        respuesta.put("mensaje", excepcion.getMessage());
        return respuesta;
    }
}
