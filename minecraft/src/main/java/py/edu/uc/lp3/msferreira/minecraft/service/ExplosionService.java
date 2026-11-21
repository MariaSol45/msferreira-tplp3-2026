package py.edu.uc.lp3.msferreira.minecraft.service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.edu.uc.lp3.msferreira.minecraft.domain.Aldeano;
import py.edu.uc.lp3.msferreira.minecraft.domain.Creeper;
import py.edu.uc.lp3.msferreira.minecraft.domain.Entidad;
import py.edu.uc.lp3.msferreira.minecraft.domain.Jugador;
import py.edu.uc.lp3.msferreira.minecraft.repository.EntidadRepository;
import py.edu.uc.lp3.msferreira.minecraft.service.exception.ExplosionNoPreparadaException;
import py.edu.uc.lp3.msferreira.minecraft.service.exception.ObjetivoFueraDeRangoException;

@Service
public class ExplosionService {
    private final EntidadRepository entidadRepository;
    private final EntidadService entidadService;

    public ExplosionService(EntidadRepository entidadRepository, EntidadService entidadService) {
        this.entidadRepository = entidadRepository;
        this.entidadService = entidadService;
    }

    @Transactional
    public ResultadoExplosion simular(SolicitudExplosion solicitud) {
        if (solicitud == null) {
            throw new IllegalArgumentException("La solicitud de explosión es obligatoria.");
        }
        return simular(
                solicitud.creeperId(),
                solicitud.jugadorId(),
                solicitud.aldeanoId(),
                solicitud.tiempoTranscurrido()
        );
    }

    @Transactional
    public ResultadoExplosion simular(Long creeperId, Long jugadorId, Long aldeanoId, Integer tiempoTranscurrido) {
        validarTiempoTranscurrido(tiempoTranscurrido);

        Creeper creeper = obtenerComo(creeperId, Creeper.class);
        Jugador jugador = obtenerComo(jugadorId, Jugador.class);
        Aldeano aldeano = obtenerComo(aldeanoId, Aldeano.class);
        entidadService.validarParaOperacion(creeper, true);
        entidadService.validarParaOperacion(jugador, true);
        entidadService.validarParaOperacion(aldeano, true);
        List<ObjetivoSimulacion> objetivos = List.of(
                new ObjetivoSimulacion(jugador, jugador.getNombre()),
                new ObjetivoSimulacion(aldeano, aldeano.getProfesion())
        );

        if (tiempoTranscurrido < creeper.getTiempoExplosion()) {
            throw new ExplosionNoPreparadaException(
                    creeper.getTiempoExplosion(), tiempoTranscurrido
            );
        }

        // Se validan todos los objetivos antes de modificar su salud para evitar resultados parciales.
        for (ObjetivoSimulacion objetivo : objetivos) {
            validarObjetivoDentroDelRadio(creeper, objetivo.entidad());
        }

        List<ImpactoExplosion> impactos = new ArrayList<>();
        for (ObjetivoSimulacion objetivo : objetivos) {
            Entidad entidad = objetivo.entidad();
            int saludInicial = entidad.getSalud();
            entidadService.aplicarDano(entidad, creeper.getDanoExplosion());
            int danoRecibido = saludInicial - entidad.getSalud();
            impactos.add(new ImpactoExplosion(
                    entidad.getClass().getSimpleName(),
                    entidad.getId(),
                    objetivo.identificador(),
                    distancia(creeper, entidad),
                    saludInicial,
                    entidad.getSalud(),
                    danoRecibido
            ));
        }

        entidadRepository.saveAll(List.of(jugador, aldeano));

        return new ResultadoExplosion(
                "Explosión de Creeper",
                List.of("Creeper", "Jugador", "Aldeano"),
                creeper.getId(),
                creeper.getTiempoExplosion(),
                tiempoTranscurrido,
                creeper.getRadioExplosion(),
                creeper.getDanoExplosion(),
                impactos
        );
    }

    private void validarTiempoTranscurrido(Integer tiempoTranscurrido) {
        if (tiempoTranscurrido == null) {
            throw new IllegalArgumentException("El tiempo transcurrido es obligatorio.");
        }
        if (tiempoTranscurrido < 0) {
            throw new IllegalArgumentException("El tiempo transcurrido no puede ser negativo.");
        }
    }

    private <T extends Entidad> T obtenerComo(Long id, Class<T> tipoEsperado) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El id de " + tipoEsperado.getSimpleName() + " debe ser positivo.");
        }

        Entidad entidad = entidadRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe una entidad con id " + id + "."));
        if (!tipoEsperado.isInstance(entidad)) {
            throw new IllegalArgumentException(
                    "La entidad con id " + id + " debe ser de tipo " + tipoEsperado.getSimpleName() + "."
            );
        }
        return tipoEsperado.cast(entidad);
    }

    private void validarObjetivoDentroDelRadio(Creeper creeper, Entidad objetivo) {
        double distancia = distancia(creeper, objetivo);
        if (distancia > creeper.getRadioExplosion()) {
            throw new ObjetivoFueraDeRangoException(
                    objetivo.getClass().getSimpleName(), distancia, creeper.getRadioExplosion()
            );
        }
    }

    private double distancia(Entidad origen, Entidad objetivo) {
        double diferenciaX = origen.getPosicionX() - objetivo.getPosicionX();
        double diferenciaY = origen.getPosicionY() - objetivo.getPosicionY();
        double diferenciaZ = origen.getPosicionZ() - objetivo.getPosicionZ();
        return Math.hypot(Math.hypot(diferenciaX, diferenciaY), diferenciaZ);
    }

    private record ObjetivoSimulacion(Entidad entidad, String identificador) {
    }

    public record SolicitudExplosion(
            Long creeperId,
            Long jugadorId,
            Long aldeanoId,
            Integer tiempoTranscurrido
    ) {
    }

    public record ResultadoExplosion(
            String evento,
            List<String> clasesParticipantes,
            Long creeperId,
            int tiempoExplosion,
            int tiempoTranscurrido,
            double radioExplosion,
            int danoBase,
            List<ImpactoExplosion> objetivos
    ) {
    }

    public record ImpactoExplosion(
            String tipo,
            Long id,
            String identificador,
            double distancia,
            int saludInicial,
            int saludFinal,
            int danoRecibido
    ) {
    }
}
