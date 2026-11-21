package py.edu.uc.lp3.msferreira.minecraft.rest.controller;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import py.edu.uc.lp3.msferreira.minecraft.domain.Aldeano;
import py.edu.uc.lp3.msferreira.minecraft.domain.Animal;
import py.edu.uc.lp3.msferreira.minecraft.domain.Creeper;
import py.edu.uc.lp3.msferreira.minecraft.domain.Entidad;
import py.edu.uc.lp3.msferreira.minecraft.domain.EntidadGenerica;
import py.edu.uc.lp3.msferreira.minecraft.domain.EntidadHostil;
import py.edu.uc.lp3.msferreira.minecraft.domain.EntidadPasiva;
import py.edu.uc.lp3.msferreira.minecraft.domain.Jugador;
import py.edu.uc.lp3.msferreira.minecraft.domain.UICapable;
import py.edu.uc.lp3.msferreira.minecraft.service.EntidadService;
import py.edu.uc.lp3.msferreira.minecraft.service.EntidadService.CreacionTransitoria;
import py.edu.uc.lp3.msferreira.minecraft.service.EntidadService.ResultadoAtaque;

@RestController
@Tag(name = "Entidades", description = "CRUD y consultas de entidades Minecraft, incluida la interfaz UICapable.")
public class EntidadController {
    private final EntidadService entidadService;

    public EntidadController(EntidadService entidadService) {
        this.entidadService = entidadService;
    }

    @GetMapping("/entidades")
    @Operation(summary = "Listar entidades", description = "Devuelve todas las entidades persistidas en H2.")
    @ApiResponse(responseCode = "200", description = "Lista de entidades obtenida correctamente.",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = Entidad.class))))
    public List<Entidad> listar() {
        return entidadService.listar();
    }

    @GetMapping("/entidades/{id}")
    @Operation(summary = "Obtener entidad por id", description = "Busca una entidad persistida por su identificador.")
    @Parameter(name = "id", description = "Identificador positivo de la entidad.", required = true, example = "1")
    @ApiResponse(responseCode = "200", description = "Entidad encontrada.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Entidad.class)))
    @ApiResponse(responseCode = "400", description = "El id no es un número positivo.",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "404", description = "No existe una entidad con ese id.",
            content = @Content(mediaType = "application/json"))
    public Entidad obtener(@PathVariable Long id) {
        return entidadService.obtener(id);
    }

    @PostMapping("/entidades")
    @Operation(summary = "Crear entidad", description = "Persiste una entidad de la jerarquía persistible. "
            + "El id es generado por la base de datos y debe omitirse del cuerpo.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Entidad a crear; el campo tipo debe ser entidad, creeper, jugador o aldeano.",
            required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(oneOf = {EntidadGenerica.class, Creeper.class, Jugador.class, Aldeano.class}))
    )
    @ApiResponse(responseCode = "201", description = "Entidad creada.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Entidad.class)))
    @ApiResponse(responseCode = "400", description = "Cuerpo JSON inválido o entidad que no supera la validación.",
            content = @Content(mediaType = "application/json"))
    public ResponseEntity<Entidad> crear(@RequestBody Entidad entidad) {
        Entidad creada = entidadService.crear(entidad);
        return ResponseEntity.created(URI.create("/entidades/" + creada.getId())).body(creada);
    }

    @PutMapping("/entidades/{id}")
    @Operation(summary = "Actualizar entidad", description = "Reemplaza el estado de una entidad existente. "
            + "No se permite cambiar el tipo de la entidad.")
    @Parameter(name = "id", description = "Identificador positivo de la entidad.", required = true, example = "1")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Nuevo estado de la entidad, con el mismo tipo que la entidad existente.",
            required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(oneOf = {EntidadGenerica.class, Creeper.class, Jugador.class, Aldeano.class}))
    )
    @ApiResponse(responseCode = "200", description = "Entidad actualizada.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Entidad.class)))
    @ApiResponse(responseCode = "400", description = "Cuerpo JSON inválido, id inválido o datos no válidos.",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "404", description = "No existe una entidad con ese id.",
            content = @Content(mediaType = "application/json"))
    public Entidad actualizar(@PathVariable Long id, @RequestBody Entidad entidad) {
        return entidadService.actualizar(id, entidad);
    }

    @DeleteMapping("/entidades/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar entidad", description = "Elimina una entidad persistida por su identificador.")
    @Parameter(name = "id", description = "Identificador positivo de la entidad.", required = true, example = "1")
    @ApiResponse(responseCode = "204", description = "Entidad eliminada; sin cuerpo de respuesta.")
    @ApiResponse(responseCode = "400", description = "El id no es un número positivo.",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "404", description = "No existe una entidad con ese id.",
            content = @Content(mediaType = "application/json"))
    public void eliminar(@PathVariable Long id) {
        entidadService.eliminar(id);
    }

    @GetMapping("/entidad")
    @Operation(summary = "Crear entidad transitoria (POO-08)", description = "Crea una entidad en memoria "
            + "según el tipo indicado y muestra el constructor utilizado y su comportamiento. "
            + "No persiste la entidad.")
    @Parameter(name = "tipo", description = "creeper, zombie, esqueleto, aldeano, animal o jugador.",
            required = true, example = "creeper")
    @Parameter(name = "salud", description = "Salud inicial mayor que 0.", required = true, example = "20")
    @Parameter(name = "x", description = "Coordenada X; valor finito.", example = "0")
    @Parameter(name = "y", description = "Coordenada Y; valor finito.", example = "0")
    @Parameter(name = "z", description = "Coordenada Z; valor finito.", example = "0")
    @Parameter(name = "velocidad", description = "Velocidad no negativa.", example = "1")
    @Parameter(name = "rango", description = "Rango de detección para entidades hostiles; finito y no negativo.",
            example = "10")
    @Parameter(name = "dano", description = "Daño de ataque para entidades hostiles; mayor que 0.", example = "5")
    @Parameter(name = "tiempoExplosion", description = "Tiempo de explosión del Creeper; mayor que 0.",
            example = "3")
    @Parameter(name = "profesion", description = "Profesión del Aldeano; no puede estar en blanco.",
            example = "Granjero")
    @Parameter(name = "domesticable", description = "Indica si el Aldeano o Animal es domesticable.",
            example = "true")
    @Parameter(name = "montar", description = "Indica si el Animal se puede montar.", example = "false")
    @Parameter(name = "nombre", description = "Nombre del Jugador; no puede estar en blanco.", example = "Steve")
    @Parameter(name = "experiencia", description = "Nivel de experiencia del Jugador; no negativo.", example = "0")
    @ApiResponse(responseCode = "200", description = "Entidad transitoria creada y descrita en JSON.")
    @ApiResponse(responseCode = "400", description = "Parámetro obligatorio ausente o valor no válido.",
            content = @Content(mediaType = "application/json"))
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

        CreacionTransitoria creacion = entidadService.crearTransitoria(
                tipo, salud, x, y, z, velocidad, rango, dano, tiempoExplosion,
                profesion, domesticable, montar, nombre, experiencia
        );
        Entidad entidad = creacion.entidad();

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("tipo", entidad.getClass().getSimpleName());
        respuesta.put("salud", entidad.getSalud());
        respuesta.put("posicion", entidad.getPosicion());
        respuesta.put("velocidad", entidad.getVelocidad());
        respuesta.put("comportamiento", entidad.comportamiento());
        respuesta.put("constructor", creacion.constructorUtilizado());

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
    @Operation(summary = "Simular ataque de Creeper (POO-08)", description = "Aplica un ataque sobre una entidad "
            + "transitoria; si se indica distancia se valida que no supere el rango de detección.")
    @Parameter(name = "saludObjetivo", description = "Salud inicial del objetivo, mayor que 0.", example = "20")
    @Parameter(name = "rango", description = "Rango de detección del Creeper; finito y no negativo.", example = "10")
    @Parameter(name = "dano", description = "Daño del ataque; mayor que 0.", example = "5")
    @Parameter(name = "distancia", description = "Distancia al objetivo; finita, no negativa y menor o igual al rango.",
            example = "4.5")
    @ApiResponse(responseCode = "200", description = "Resultado del ataque con variante y salud final.")
    @ApiResponse(responseCode = "400", description = "Parámetro no válido o objetivo fuera del rango.",
            content = @Content(mediaType = "application/json"))
    public Map<String, Object> atacar(
            @RequestParam(defaultValue = "20") int saludObjetivo,
            @RequestParam(defaultValue = "10") double rango,
            @RequestParam(defaultValue = "5") int dano,
            @RequestParam(required = false) Double distancia) {

        ResultadoAtaque resultado = distancia == null
                ? entidadService.atacar(saludObjetivo, rango, dano)
                : entidadService.atacar(saludObjetivo, rango, dano, distancia);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("atacante", resultado.atacante().getClass().getSimpleName());
        respuesta.put("variante", resultado.variante());
        respuesta.put("saludInicial", resultado.saludInicial());
        respuesta.put("saludFinal", resultado.saludFinal());
        respuesta.put("danoAplicado", resultado.saludInicial() - resultado.saludFinal());
        if (distancia != null) {
            respuesta.put("distancia", distancia);
        }
        return respuesta;
    }

    @GetMapping("/ui")
    @Operation(summary = "Datos UI de una entidad (UICapable)", description = "Devuelve imagen, posición, sonido "
            + "y mensaje de UI de un Jugador o Aldeano, persistido (id) o transitorio (tipo).")
    @Parameter(name = "id", description = "Identificador de una entidad persistida que implemente UICapable.",
            example = "1")
    @Parameter(name = "tipo", description = "jugador o aldeano; obligatorio si no se indica id.", example = "jugador")
    @Parameter(name = "salud", description = "Salud de la entidad transitoria, mayor que 0.", example = "20")
    @Parameter(name = "x", description = "Coordenada X; valor finito.", example = "0")
    @Parameter(name = "y", description = "Coordenada Y; valor finito.", example = "0")
    @Parameter(name = "z", description = "Coordenada Z; valor finito.", example = "0")
    @Parameter(name = "velocidad", description = "Velocidad no negativa.", example = "1")
    @Parameter(name = "nombre", description = "Nombre del Jugador; no puede estar en blanco.", example = "Steve")
    @Parameter(name = "experiencia", description = "Nivel de experiencia del Jugador; no negativo.", example = "0")
    @Parameter(name = "profesion", description = "Profesión del Aldeano; no puede estar en blanco.",
            example = "Granjero")
    @ApiResponse(responseCode = "200", description = "Datos de UI de la entidad.")
    @ApiResponse(responseCode = "400", description = "Faltan id o tipo, el tipo no es válido o los datos no son válidos.",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "404", description = "No existe una entidad con el id indicado.",
            content = @Content(mediaType = "application/json"))
    public Map<String, Object> obtenerDatosUI(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String tipo,
            @RequestParam(defaultValue = "20") int salud,
            @RequestParam(defaultValue = "0") double x,
            @RequestParam(defaultValue = "0") double y,
            @RequestParam(defaultValue = "0") double z,
            @RequestParam(defaultValue = "1") int velocidad,
            @RequestParam(defaultValue = "Steve") String nombre,
            @RequestParam(defaultValue = "0") int experiencia,
            @RequestParam(defaultValue = "Granjero") String profesion) {

        UICapable elementoUI = entidadService.obtenerElementoUI(
                id, tipo, salud, x, y, z, velocidad, nombre, experiencia, profesion
        );

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("tipo", elementoUI.getClass().getSimpleName());
        respuesta.put("imagen", elementoUI.getImagen());
        respuesta.put("posicion", elementoUI.getPosicion());
        respuesta.put("sonido", elementoUI.getSonido());
        respuesta.put("velocidad", elementoUI.getVelocidad());
        respuesta.put("mensaje", elementoUI.getMensajeUI());

        return respuesta;
    }

}
