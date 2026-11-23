package py.edu.uc.lp3.msferreira.minecraft.service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import py.edu.uc.lp3.msferreira.minecraft.domain.Jugada;
import py.edu.uc.lp3.msferreira.minecraft.repository.EntidadRepository;
import py.edu.uc.lp3.msferreira.minecraft.repository.JugadaRepository;
import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService.ResultadoExplosion;
import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService.EstadoExplosion;
import py.edu.uc.lp3.msferreira.minecraft.service.ExplosionService.SolicitudExplosion;
import py.edu.uc.lp3.msferreira.minecraft.service.companero.ClienteA2;
import py.edu.uc.lp3.msferreira.minecraft.service.companero.ClienteA3;
import py.edu.uc.lp3.msferreira.minecraft.service.exception.CompaneroException;
import py.edu.uc.lp3.msferreira.minecraft.service.exception.CreeperYaRegistradoException;
import py.edu.uc.lp3.msferreira.minecraft.service.exception.ExplosionException;

/**
 * Escenario de POO-09: una explosión de Creeper se registra solo si la aceptan A2 y A3.
 *
 * Orden: (1) mis dos reglas, localmente, para no consultar a nadie con una jugada que yo misma rechazo;
 * (2) A2; (3) A3, solo si A2 aceptó, así el rechazo tiene un único origen; (4) aplicar la explosión y
 * guardar la jugada en una sola transacción. Las llamadas HTTP quedan fuera de la transacción:
 * si A2 o A3 rechazan, todavía no se escribió nada en H2.
 */
@Service
public class JugadaService {
    static final String TIPO_EXPLOSION = "Explosión de Creeper";

    private static final Logger REGISTRO = LogManager.getLogger(JugadaService.class);

    private final ExplosionService explosionService;
    private final JugadaRepository jugadaRepository;
    private final EntidadRepository entidadRepository;
    private final ClienteA2 clienteA2;
    private final ClienteA3 clienteA3;
    private final TransactionTemplate transaccion;

    public JugadaService(ExplosionService explosionService, JugadaRepository jugadaRepository,
                         EntidadRepository entidadRepository,
                         ClienteA2 clienteA2, ClienteA3 clienteA3,
                         PlatformTransactionManager transactionManager) {
        this.explosionService = explosionService;
        this.jugadaRepository = jugadaRepository;
        this.entidadRepository = entidadRepository;
        this.clienteA2 = clienteA2;
        this.clienteA3 = clienteA3;
        this.transaccion = new TransactionTemplate(transactionManager);
    }

    public Jugada registrarExplosion(SolicitudExplosion solicitud) {
        REGISTRO.debug("Iniciando jugada: creeperId={}, jugadorId={}, aldeanoId={}, tiempoTranscurrido={}",
                solicitud.creeperId(), solicitud.jugadorId(), solicitud.aldeanoId(), solicitud.tiempoTranscurrido());
        EstadoExplosion estadoCreeper = explosionService.describir(solicitud);
        REGISTRO.debug("Estado leído desde H2: saludCreeper={}, yaDetonado={}",
                estadoCreeper.saludCreeper(), estadoCreeper.yaDetonado());
        try {
            explosionService.validar(estadoCreeper.jugada());
            REGISTRO.debug("Validación local A1 correcta; se continúa con A2 y A3");
        } catch (ExplosionException excepcion) {
            REGISTRO.debug("Validación local A1 rechazó ({}); decisión: no persistir ni consultar compañeras",
                    excepcion.getClass().getSimpleName());
            throw excepcion;
        }

        try {
            REGISTRO.debug("Consultando a A2 ({})", clienteA2.getCompanero());
            clienteA2.validar(estadoCreeper);
            REGISTRO.debug("Consultando a A3 ({})", clienteA3.getCompanero());
            clienteA3.validar(estadoCreeper);
        } catch (CompaneroException excepcion) {
            REGISTRO.debug("Decisión: no persistir; {} ({}) originó {}",
                    excepcion.getRol(), excepcion.getCompanero(), excepcion.getClass().getSimpleName());
            throw excepcion;
        }

        REGISTRO.debug("Decisión: persistir; A2={} y A3={} aceptaron", clienteA2.getCompanero(),
                clienteA3.getCompanero());
        Jugada guardada = transaccion.execute(estado -> {
            Long creeperId = solicitud.creeperId();
            entidadRepository.bloquearFilaPorId(creeperId)
                    .orElseThrow(() -> new NoSuchElementException("No existe una entidad con id " + creeperId + "."));
            // La aprobación remota puede haber usado un estado anterior al guardado de otra solicitud.
            if (jugadaRepository.existsByCreeperId(creeperId)) {
                REGISTRO.debug("Decisión: no persistir; ya hay una jugada para creeperId={}", creeperId);
                throw new CreeperYaRegistradoException(creeperId);
            }
            ResultadoExplosion resultado = explosionService.simular(solicitud);
            return jugadaRepository.save(new Jugada(
                    TIPO_EXPLOSION,
                    solicitud.creeperId(),
                    solicitud.jugadorId(),
                    solicitud.aldeanoId(),
                    solicitud.tiempoTranscurrido(),
                    resultado.objetivos().get(0).saludFinal(),
                    resultado.objetivos().get(1).saludFinal(),
                    clienteA2.getCompanero(),
                    clienteA3.getCompanero(),
                    Instant.now()
            ));
        });
        REGISTRO.debug("Guardado en H2 confirmado: jugadaId={}, creeperId={}", guardada.getId(),
                solicitud.creeperId());
        return guardada;
    }

    public List<Jugada> listar() {
        return jugadaRepository.findAllByOrderByIdAsc();
    }

    public Jugada obtener(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El id debe ser un número positivo.");
        }
        return jugadaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe una jugada con id " + id + "."));
    }

    public List<ClienteInfo> companeros() {
        return List.of(
                new ClienteInfo(clienteA2.getRol(), clienteA2.getCompanero(), clienteA2.getUrlValidacion()),
                new ClienteInfo(clienteA3.getRol(), clienteA3.getCompanero(), clienteA3.getUrlValidacion())
        );
    }

    public record ClienteInfo(String rol, String companero, String urlValidacion) {
    }
}
