package py.edu.uc.lp3.msferreira.minecraft.service.companero;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService.EstadoExplosion;

/** Cliente hacia A2 (Irina), según su OpenAPI /v3/api-docs. */
@Component
public class ClienteA2 extends ClienteCompanero {

    public ClienteA2(
            @Value("${companeros.a2.nombre:Irina}") String companero,
            @Value("${companeros.a2.base-url:http://100.87.95.24:8082}") String baseUrl,
            @Value("${companeros.a2.ruta-validacion:/api/minecraft/reglas/mecha-corta}") String rutaValidacion,
            @Value("${companeros.timeout-segundos:5}") long timeoutSegundos) {
        super("A2", companero, baseUrl, rutaValidacion, timeoutSegundos);
    }

    /** MechaCortaRequest: saludCreeper, creeper y objetivos con tipo, salud y posicion. */
    @Override
    protected Object armarCuerpo(EstadoExplosion estado) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("saludCreeper", estado.saludCreeper());
        cuerpo.put("creeper", estado.jugada().creeper());
        cuerpo.put("objetivos", estado.jugada().objetivos());
        return cuerpo;
    }

    @Override
    protected String describirCuerpo() {
        return "MechaCortaRequest (saludCreeper, creeper, objetivos)";
    }
}
