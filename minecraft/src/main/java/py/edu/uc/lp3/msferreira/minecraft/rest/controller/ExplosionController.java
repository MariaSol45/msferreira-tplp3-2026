package py.edu.uc.lp3.msferreira.minecraft.rest.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService;
import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService.ResultadoExplosion;

@RestController
@RequestMapping("/explosiones")
public class ExplosionController {
    private final ExplosionService explosionService;

    public ExplosionController(ExplosionService explosionService) {
        this.explosionService = explosionService;
    }

    @PostMapping("/simular")
    public ResultadoExplosion simular(@RequestBody SolicitudExplosion solicitud) {
        return explosionService.simular(
                solicitud.tiempoTranscurrido(),
                solicitud.posicionXJugador(),
                solicitud.posicionXAldeano()
        );
    }

    public record SolicitudExplosion(
            int tiempoTranscurrido,
            double posicionXJugador,
            double posicionXAldeano
    ) {
    }
}
