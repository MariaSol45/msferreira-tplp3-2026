package py.edu.uc.lp3.msferreira.minecraft.service.companero;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService.SolicitudValidacion;

/** Cliente hacia A3 (Cecilia). Configuración: companeros.a3.* en application.properties. */
@Component
public class ClienteA3 extends ClienteCompanero {

    public ClienteA3(
            @Value("${companeros.a3.nombre:Cecilia}") String companero,
            @Value("${companeros.a3.base-url:http://localhost:8083}") String baseUrl,
            @Value("${companeros.a3.ruta-validacion:/explosiones/validar}") String rutaValidacion,
            @Value("${companeros.timeout-segundos:5}") long timeoutSegundos) {
        super("A3", companero, baseUrl, rutaValidacion, timeoutSegundos);
    }

    /**
     * Contrato propuesto al grupo: el mismo cuerpo que POST /explosiones/validar.
     * Cuando Cecilia publique su OpenAPI, este método se adapta a su esquema de pedido.
     */
    @Override
    protected Object armarCuerpo(SolicitudValidacion jugada) {
        return jugada;
    }
}
