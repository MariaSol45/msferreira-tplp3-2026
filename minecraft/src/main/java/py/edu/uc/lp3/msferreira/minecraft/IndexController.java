package py.edu.uc.lp3.msferreira.minecraft;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

    @GetMapping("/")
    public Map<String, String> index() {
        Map<String, String> respuesta = new LinkedHashMap<>();

        respuesta.put("estado", "API funcionando");
        respuesta.put("dominio", "Minecraft");
        respuesta.put("autora", "María Sol Ferreira");

        return respuesta;
    }
}