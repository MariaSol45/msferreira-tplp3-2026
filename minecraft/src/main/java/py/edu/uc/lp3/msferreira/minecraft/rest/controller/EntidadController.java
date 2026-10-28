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
import py.edu.uc.lp3.msferreira.minecraft.domain.Animal;
import py.edu.uc.lp3.msferreira.minecraft.domain.Creeper;
import py.edu.uc.lp3.msferreira.minecraft.domain.Entidad;
import py.edu.uc.lp3.msferreira.minecraft.domain.EntidadHostil;
import py.edu.uc.lp3.msferreira.minecraft.domain.EntidadPasiva;
import py.edu.uc.lp3.msferreira.minecraft.domain.Esqueleto;
import py.edu.uc.lp3.msferreira.minecraft.domain.Jugador;
import py.edu.uc.lp3.msferreira.minecraft.domain.UICapable;
import py.edu.uc.lp3.msferreira.minecraft.domain.Zombie;
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
            @RequestParam(required = false) Integer tiempoExplosion,
            @RequestParam(required = false) String profesion,
            @RequestParam(required = false) Boolean domesticable,
            @RequestParam(required = false) Boolean montar,
            @RequestParam(defaultValue = "Steve") String nombre,
            @RequestParam(defaultValue = "0") int experiencia) {

        Entidad entidad;
        String constructorUtilizado = "completo";

        if (tipo.equalsIgnoreCase("creeper")) {
            if (tiempoExplosion == null) {
                entidad = new Creeper(salud, x, y, z, velocidad, rango, dano);
                constructorUtilizado = "simplificado";
            } else {
                entidad = new Creeper(salud, x, y, z, velocidad, rango, dano, tiempoExplosion);
            }
        } else if (tipo.equalsIgnoreCase("zombie")) {
            entidad = new Zombie(salud, x, y, z, velocidad, rango, dano);
        } else if (tipo.equalsIgnoreCase("esqueleto")) {
            entidad = new Esqueleto(salud, x, y, z, velocidad, rango, dano);
        } else if (tipo.equalsIgnoreCase("aldeano")) {
            entidad = new Aldeano(
                    salud, x, y, z, velocidad,
                    domesticable == null || domesticable,
                    profesion == null ? "Granjero" : profesion
            );
        } else if (tipo.equalsIgnoreCase("animal")) {
            entidad = new Animal(
                    salud, x, y, z, velocidad,
                    domesticable != null && domesticable,
                    montar != null && montar
            );
        } else if (tipo.equalsIgnoreCase("jugador")) {
            entidad = new Jugador(nombre, experiencia, salud, x, y, z, velocidad);
        } else {
            throw new IllegalArgumentException(
                    "Tipos válidos: creeper, zombie, esqueleto, aldeano, animal o jugador"
            );
        }

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("tipo", entidad.getClass().getSimpleName());
        respuesta.put("salud", entidad.getSalud());
        respuesta.put("posicion", entidad.getPosicion());
        respuesta.put("velocidad", entidad.getVelocidad());
        respuesta.put("comportamiento", entidad.comportamiento());
        respuesta.put("constructor", constructorUtilizado);

        if (entidad instanceof EntidadHostil hostil) {
            respuesta.put("rangoDeteccion", hostil.getRangoDeteccion());
            respuesta.put("danoAtaque", hostil.getDanoAtaque());
        }
        if (entidad instanceof EntidadPasiva pasiva) {
            respuesta.put("domesticable", pasiva.isDomesticable());
        }
        if (entidad instanceof Creeper creeper) {
            respuesta.put("tiempoExplosion", creeper.getTiempoExplosion());
        }
        if (entidad instanceof Aldeano aldeano) {
            respuesta.put("profesion", aldeano.getProfesion());
        }
        if (entidad instanceof Animal animal) {
            respuesta.put("montar", animal.isMontar());
        }
        if (entidad instanceof Jugador jugador) {
            respuesta.put("nombre", jugador.getNombre());
            respuesta.put("experiencia", jugador.getNivelEXP());
        }

        return respuesta;
    }

    @GetMapping("/entidad/atacar")
    public Map<String, Object> atacar(
            @RequestParam(defaultValue = "20") int saludObjetivo,
            @RequestParam(defaultValue = "10") double rango,
            @RequestParam(defaultValue = "5") int dano,
            @RequestParam(required = false) Double distancia) {

        Creeper atacante = new Creeper(20, 0, 0, 0, 1, rango, dano);
        Entidad objetivo = new Entidad(saludObjetivo, 0, 0, 0, 1);
        int saludInicial = objetivo.getSalud();
        String variante;

        if (distancia == null) {
            atacante.atacar(objetivo);
            variante = "atacar(Entidad)";
        } else {
            atacante.atacar(objetivo, distancia);
            variante = "atacar(Entidad, double)";
        }

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("atacante", atacante.getClass().getSimpleName());
        respuesta.put("variante", variante);
        respuesta.put("saludInicial", saludInicial);
        respuesta.put("saludFinal", objetivo.getSalud());
        respuesta.put("danoAplicado", saludInicial - objetivo.getSalud());
        if (distancia != null) {
            respuesta.put("distancia", distancia);
        }
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
