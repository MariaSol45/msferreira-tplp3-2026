package py.edu.uc.lp3.msferreira.minecraft.rest.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import py.edu.uc.lp3.msferreira.minecraft.rest.exception.ExplosionExceptionHandler.ErrorExplosion;
import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService;
import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService.ResultadoExplosion;
import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService.ResultadoValidacion;
import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService.SolicitudExplosion;
import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService.SolicitudValidacion;

@RestController
@RequestMapping("/explosiones")
@Tag(name = "Explosiones", description = "Simulación de explosiones de Creeper sobre Jugador y Aldeano.")
public class ExplosionController {
    private final ExplosionService explosionService;

    public ExplosionController(ExplosionService explosionService) {
        this.explosionService = explosionService;
    }

    @PostMapping("/simular")
    @Operation(summary = "Simular explosión", description = "Valida a los participantes, aplica el daño del Creeper "
            + "y devuelve el resultado. La simulación se revierte si algún objetivo queda fuera del radio.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Ids persistentes de Creeper, Jugador y Aldeano, y tiempo transcurrido en segundos.",
            required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SolicitudExplosion.class,
                            example = "{\"creeperId\":1,\"jugadorId\":2,\"aldeanoId\":3,\"tiempoTranscurrido\":3}"))
    )
    @ApiResponse(responseCode = "200", description = "Explosión simulada con el detalle de impactos.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResultadoExplosion.class)))
    @ApiResponse(responseCode = "400", description = "Solicitud nula, id inválido, tiempo ausente/negativo, "
            + "entidad de tipo incorrecto o participante muerto.",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "404", description = "No existe alguna de las entidades indicadas.",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "409", description = "Regla 1 (ExplosionNoPreparadaException): el tiempo "
            + "transcurrido es menor al tiempoExplosion del Creeper. No se aplica daño.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorExplosion.class),
                    examples = @ExampleObject(value = "{\"error\":\"Explosión no preparada\",\"mensaje\":\"El Creeper "
                            + "necesita 3 segundos para explotar, pero solo transcurrieron 2.\",\"estado\":409}")))
    @ApiResponse(responseCode = "422", description = "Regla 2 (ObjetivoFueraDeRangoException): algún objetivo "
            + "está a más distancia que el radio de explosión. No se aplica daño a ninguno.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorExplosion.class),
                    examples = @ExampleObject(value = "{\"error\":\"Objetivo fuera del radio de explosión\",\"mensaje\":"
                            + "\"El objetivo Aldeano está a 6.0 bloques y queda fuera del radio de explosión de 5.0 bloques.\","
                            + "\"estado\":422}")))
    public ResultadoExplosion simular(@RequestBody SolicitudExplosion solicitud) {
        return explosionService.simular(solicitud);
    }

    @PostMapping("/validar")
    @Operation(summary = "Validar explosión (para consumir desde otro servicio)",
            description = "Evalúa las dos reglas con los datos del cuerpo y no lee ni modifica H2. "
                    + "Regla 1: tiempoTranscurrido >= tiempoExplosion. Regla 2: cada objetivo a una distancia "
                    + "euclídea del Creeper menor o igual a radioExplosion. 200 significa que la jugada es válida.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SolicitudValidacion.class,
                            example = "{\"tiempoExplosion\":3,\"tiempoTranscurrido\":3,\"radioExplosion\":5,"
                                    + "\"creeper\":{\"x\":0,\"y\":0,\"z\":0},\"objetivos\":["
                                    + "{\"tipo\":\"jugador\",\"salud\":20,\"posicion\":{\"x\":2,\"y\":0,\"z\":0}},"
                                    + "{\"tipo\":\"aldeano\",\"salud\":20,\"posicion\":{\"x\":4,\"y\":0,\"z\":0}}]}")))
    @ApiResponse(responseCode = "200", description = "Las dos reglas se cumplen.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResultadoValidacion.class)))
    @ApiResponse(responseCode = "400", description = "Faltan datos o tienen valores imposibles.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorExplosion.class)))
    @ApiResponse(responseCode = "409", description = "Regla 1: el Creeper todavía no detonó.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorExplosion.class)))
    @ApiResponse(responseCode = "422", description = "Regla 2: algún objetivo está fuera del radio.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorExplosion.class)))
    public ResultadoValidacion validar(@RequestBody SolicitudValidacion solicitud) {
        return explosionService.validar(solicitud);
    }
}
