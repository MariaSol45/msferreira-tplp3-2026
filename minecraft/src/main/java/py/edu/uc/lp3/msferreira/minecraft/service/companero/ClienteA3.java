package py.edu.uc.lp3.msferreira.minecraft.service.companero;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService.EstadoExplosion;

/** Cliente hacia A3 (Cecilia), según su OpenAPI /v3/api-docs. */
@Component
public class ClienteA3 extends ClienteCompanero {

    public ClienteA3(
            @Value("${companeros.a3.nombre:Cecilia}") String companero,
            @Value("${companeros.a3.base-url:http://100.122.114.103:8083}") String baseUrl,
            @Value("${companeros.a3.ruta-validacion:/explosiones/validar}") String rutaValidacion,
            @Value("${companeros.timeout-segundos:5}") long timeoutSegundos) {
        super("A3", companero, baseUrl, rutaValidacion, timeoutSegundos);
    }

    /** ValidacionExplosionRequest: solo saludCreeper y yaDetonado, leídos de mi H2. */
    @Override
    protected Object armarCuerpo(EstadoExplosion estado) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("saludCreeper", estado.saludCreeper());
        cuerpo.put("yaDetonado", estado.yaDetonado());
        return cuerpo;
    }
}
