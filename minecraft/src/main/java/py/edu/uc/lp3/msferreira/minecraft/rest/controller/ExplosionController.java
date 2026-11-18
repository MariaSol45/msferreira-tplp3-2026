package py.edu.uc.lp3.msferreira.minecraft.rest.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService;
import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService.ResultadoExplosion;
import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService.SolicitudExplosion;

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
    @ApiResponse(responseCode = "409", description = "El tiempo transcurrido es menor al tiempo de explosión.",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "422", description = "Algún objetivo está fuera del radio de explosión.",
            content = @Content(mediaType = "application/json"))
    public ResultadoExplosion simular(@RequestBody SolicitudExplosion solicitud) {
        return explosionService.simular(solicitud);
    }
}
