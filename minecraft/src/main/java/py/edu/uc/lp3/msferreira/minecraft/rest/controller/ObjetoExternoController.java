package py.edu.uc.lp3.msferreira.minecraft.rest.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import py.edu.uc.lp3.msferreira.minecraft.service.ApiExternaService;
import py.edu.uc.lp3.msferreira.minecraft.service.ApiExternaService.ObjetoExterno;

@RestController
@RequestMapping("/api/objetos-externos")
@Tag(name = "Objetos externos", description = "Consulta a la API externa restful-api.dev mediante WebClient.")
public class ObjetoExternoController {

    private final ApiExternaService apiExternaService;

    public ObjetoExternoController(ApiExternaService apiExternaService) {
        this.apiExternaService = apiExternaService;
    }

    @GetMapping
    @Operation(
            summary = "Listar objetos externos",
            description = "Obtiene la lista de objetos de https://api.restful-api.dev/objects. "
                    + "El campo data puede ser null y contener atributos variables."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lista de objetos externos obtenida correctamente.",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ObjetoExterno.class)))
    )
    @ApiResponse(
            responseCode = "502",
            description = "La API externa respondió con error, falló la conexión o agotó el tiempo de espera.",
            content = @Content(mediaType = "application/json")
    )
    public List<ObjetoExterno> listar() {
        return apiExternaService.listarObjetos();
    }
}
