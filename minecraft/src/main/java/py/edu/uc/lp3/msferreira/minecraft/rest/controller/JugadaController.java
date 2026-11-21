package py.edu.uc.lp3.msferreira.minecraft.rest.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import py.edu.uc.lp3.msferreira.minecraft.domain.Jugada;
import py.edu.uc.lp3.msferreira.minecraft.rest.exception.CompaneroExceptionHandler.ErrorCompanero;
import py.edu.uc.lp3.msferreira.minecraft.rest.exception.ExplosionExceptionHandler.ErrorExplosion;
import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService.SolicitudExplosion;
import py.edu.uc.lp3.msferreira.minecraft.service.JugadaService;
import py.edu.uc.lp3.msferreira.minecraft.service.JugadaService.ClienteInfo;

@RestController
@RequestMapping("/jugadas")
@Tag(name = "Jugadas (POO-09)", description = "Escenario cruzado: una explosión se guarda solo si A2 y A3 la aceptan.")
public class JugadaController {
    private final JugadaService jugadaService;

    public JugadaController(JugadaService jugadaService) {
        this.jugadaService = jugadaService;
    }

    @PostMapping("/explosion")
    @Operation(summary = "Registrar explosión validada por A2 y A3",
            description = "Lee al Creeper, al Jugador y al Aldeano de H2, evalúa mis dos reglas, consulta a A2 y, "
                    + "si acepta, a A3. Solo si las dos aceptan aplica el daño y guarda la jugada. "
                    + "Si alguna rechaza, H2 queda igual y la respuesta indica el origen.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SolicitudExplosion.class,
                            example = "{\"creeperId\":1,\"jugadorId\":2,\"aldeanoId\":3,\"tiempoTranscurrido\":3}")))
    @ApiResponse(responseCode = "201", description = "A2 y A3 aceptaron; la jugada quedó guardada.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Jugada.class)))
    @ApiResponse(responseCode = "400", description = "Datos inválidos o participante de otro tipo o muerto.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorExplosion.class)))
    @ApiResponse(responseCode = "404", description = "No existe alguno de los ids.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorExplosion.class)))
    @ApiResponse(responseCode = "409", description = "Rechazo: de A2 o A3 (campo origen) o de mi regla de detonación.",
            content = @Content(mediaType = "application/json", schema = @Schema(oneOf = {ErrorCompanero.class,
                    ErrorExplosion.class})))
    @ApiResponse(responseCode = "422", description = "Mi regla de radio: algún objetivo fuera del radio.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorExplosion.class)))
    @ApiResponse(responseCode = "502", description = "A2 o A3 no respondió (campo origen); no se guardó nada.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorCompanero.class)))
    public ResponseEntity<Jugada> registrarExplosion(@RequestBody SolicitudExplosion solicitud) {
        Jugada jugada = jugadaService.registrarExplosion(solicitud);
        return ResponseEntity.created(URI.create("/jugadas/" + jugada.getId())).body(jugada);
    }

    @GetMapping
    @Operation(summary = "Listar jugadas guardadas")
    @ApiResponse(responseCode = "200", description = "Jugadas en orden de registro.",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Jugada.class))))
    public List<Jugada> listar() {
        return jugadaService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una jugada por id")
    @ApiResponse(responseCode = "200", description = "Jugada encontrada.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Jugada.class)))
    @ApiResponse(responseCode = "404", description = "No existe una jugada con ese id.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorExplosion.class)))
    public Jugada obtener(@PathVariable Long id) {
        return jugadaService.obtener(id);
    }

    @GetMapping("/companeros")
    @Operation(summary = "Ver a quién consulta este servicio",
            description = "Muestra rol, nombre y URL de validación configurados para A2 y A3.")
    @ApiResponse(responseCode = "200", description = "Configuración actual de A2 y A3.",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ClienteInfo.class))))
    public List<ClienteInfo> companeros() {
        return jugadaService.companeros();
    }
}
