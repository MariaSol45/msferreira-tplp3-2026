package py.edu.uc.lp3.msferreira.minecraft.rest.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Índice", description = "Estado general de la API REST de Minecraft.")
public class IndexController {

    @GetMapping("/")
    @Operation(
            summary = "Estado de la API",
            description = "Devuelve un mensaje de estado, el dominio y la autora del proyecto."
    )
    @ApiResponse(responseCode = "200", description = "Estado de la API obtenido correctamente.")
    public Map<String, String> index() {
        Map<String, String> respuesta = new LinkedHashMap<>();

        respuesta.put("estado", "API funcionando");
        respuesta.put("dominio", "Minecraft");
        respuesta.put("autora", "María Sol Ferreira");

        return respuesta;
    }
}
