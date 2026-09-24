package py.edu.uc.lp3.msferreira.minecraft;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EntidadController {

    @GetMapping("/entidad")
    public Map<String, Object> crearEntidad(
            @RequestParam String tipo,
            @RequestParam int salud,
            @RequestParam(defaultValue = "0") double x,
            @RequestParam(defaultValue = "0") double y,
            @RequestParam(defaultValue = "0") double z,
            @RequestParam(defaultValue = "1") int velocidad) {

        Entidad entidad;

        if (tipo.equalsIgnoreCase("creeper")) {
            entidad = new Creeper(
                    salud, x, y, z, velocidad,
                    10.0, 5, 3
            );
        } else if (tipo.equalsIgnoreCase("aldeano")) {
            entidad = new Aldeano(
                    salud, x, y, z, velocidad,
                    true, "Granjero"
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
}