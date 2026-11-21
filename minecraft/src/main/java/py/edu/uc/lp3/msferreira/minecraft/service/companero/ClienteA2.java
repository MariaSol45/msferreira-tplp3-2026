package py.edu.uc.lp3.msferreira.minecraft.service.companero;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService.SolicitudValidacion;

/** Cliente hacia A2 (Irina). Configuración: companeros.a2.* en application.properties. */
@Component
public class ClienteA2 extends ClienteCompanero {

    public ClienteA2(
            @Value("${companeros.a2.nombre:Irina}") String companero,
            @Value("${companeros.a2.base-url:http://localhost:8082}") String baseUrl,
            @Value("${companeros.a2.ruta-validacion:/explosiones/validar}") String rutaValidacion,
            @Value("${companeros.timeout-segundos:5}") long timeoutSegundos) {
        super("A2", companero, baseUrl, rutaValidacion, timeoutSegundos);
    }

    /**
     * Contrato propuesto al grupo: el mismo cuerpo que POST /explosiones/validar.
     * Cuando Irina publique su OpenAPI, este método se adapta a su esquema de pedido.
     */
    @Override
    protected Object armarCuerpo(SolicitudValidacion jugada) {
        return jugada;
    }
}
