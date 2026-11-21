package py.edu.uc.lp3.msferreira.minecraft.service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swagger.v3.oas.annotations.media.Schema;

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

        validarDetonacion(creeper.getTiempoExplosion(), tiempoTranscurrido);

        // Se validan todos los objetivos antes de modificar su salud para evitar resultados parciales.
        for (ObjetivoSimulacion objetivo : objetivos) {
            validarObjetivoDentroDelRadio(
                    objetivo.entidad().getClass().getSimpleName(),
                    distancia(creeper, objetivo.entidad()),
                    creeper.getRadioExplosion()
            );
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

    /**
     * Evalúa las dos reglas de la explosión sin leer ni modificar H2.
     * Es la operación que consumen los compañeros del grupo desde su propio servicio.
     */
    public ResultadoValidacion validar(SolicitudValidacion solicitud) {
        if (solicitud == null) {
            throw new IllegalArgumentException("La solicitud de validación es obligatoria.");
        }
        if (solicitud.tiempoExplosion() == null || solicitud.tiempoExplosion() <= 0) {
            throw new IllegalArgumentException("El tiempoExplosion debe ser mayor que 0.");
        }
        validarTiempoTranscurrido(solicitud.tiempoTranscurrido());
        if (solicitud.radioExplosion() == null || !Double.isFinite(solicitud.radioExplosion())
                || solicitud.radioExplosion() < 0) {
            throw new IllegalArgumentException("El radioExplosion debe ser finito y no negativo.");
        }
        validarPosicion("creeper", solicitud.creeper());
        if (solicitud.objetivos() == null || solicitud.objetivos().isEmpty()) {
            throw new IllegalArgumentException("Debe indicar al menos un objetivo.");
        }
        for (ObjetivoValidacion objetivo : solicitud.objetivos()) {
            if (objetivo == null || objetivo.tipo() == null || objetivo.tipo().isBlank()) {
                throw new IllegalArgumentException("Cada objetivo debe indicar su tipo.");
            }
            validarPosicion(objetivo.tipo(), objetivo.posicion());
        }

        validarDetonacion(solicitud.tiempoExplosion(), solicitud.tiempoTranscurrido());
        for (ObjetivoValidacion objetivo : solicitud.objetivos()) {
            validarObjetivoDentroDelRadio(
                    objetivo.tipo(), distancia(solicitud.creeper(), objetivo.posicion()), solicitud.radioExplosion()
            );
        }
        return new ResultadoValidacion(
                true,
                "La explosión cumple el tiempo de detonación y todos los objetivos están dentro del radio."
        );
    }

    private void validarDetonacion(int tiempoExplosion, int tiempoTranscurrido) {
        if (tiempoTranscurrido < tiempoExplosion) {
            throw new ExplosionNoPreparadaException(tiempoExplosion, tiempoTranscurrido);
        }
    }

    private void validarObjetivoDentroDelRadio(String tipoObjetivo, double distancia, double radioExplosion) {
        if (distancia > radioExplosion) {
            throw new ObjetivoFueraDeRangoException(tipoObjetivo, distancia, radioExplosion);
        }
    }

    private void validarPosicion(String participante, Posicion posicion) {
        if (posicion == null || !Double.isFinite(posicion.x()) || !Double.isFinite(posicion.y())
                || !Double.isFinite(posicion.z())) {
            throw new IllegalArgumentException("La posición de " + participante + " es obligatoria y debe ser finita.");
        }
    }

    private double distancia(Entidad origen, Entidad objetivo) {
        return distancia(Posicion.de(origen), Posicion.de(objetivo));
    }

    private double distancia(Posicion origen, Posicion objetivo) {
        return Math.hypot(Math.hypot(origen.x() - objetivo.x(), origen.y() - objetivo.y()), origen.z() - objetivo.z());
    }

    private record ObjetivoSimulacion(Entidad entidad, String identificador) {
    }

    @Schema(description = "Explosión a validar con las dos reglas, sin ids ni acceso a la base.")
    public record SolicitudValidacion(
            @Schema(description = "Segundos que necesita el Creeper para detonar; mayor que 0.", example = "3")
            Integer tiempoExplosion,
            @Schema(description = "Segundos transcurridos desde que se activó; no negativo.", example = "3")
            Integer tiempoTranscurrido,
            @Schema(description = "Radio de la explosión en bloques; finito y no negativo.", example = "5")
            Double radioExplosion,
            @Schema(description = "Posición del Creeper.")
            Posicion creeper,
            @Schema(description = "Objetivos alcanzados por la explosión; al menos uno.")
            List<ObjetivoValidacion> objetivos
    ) {
    }

    @Schema(description = "Objetivo de la explosión.")
    public record ObjetivoValidacion(
            @Schema(description = "Tipo del objetivo, por ejemplo jugador o aldeano.", example = "jugador") String tipo,
            @Schema(description = "Salud actual del objetivo (informativa).", example = "20") Integer salud,
            @Schema(description = "Posición del objetivo.") Posicion posicion
    ) {
    }

    @Schema(description = "Coordenadas en bloques.")
    public record Posicion(
            @Schema(example = "0") double x,
            @Schema(example = "0") double y,
            @Schema(example = "0") double z
    ) {
        public static Posicion de(Entidad entidad) {
            return new Posicion(entidad.getPosicionX(), entidad.getPosicionY(), entidad.getPosicionZ());
        }
    }

    @Schema(description = "Resultado cuando las dos reglas se cumplen.")
    public record ResultadoValidacion(
            @Schema(example = "true") boolean aceptada,
            @Schema(example = "La explosión cumple el tiempo de detonación y todos los objetivos están dentro del radio.")
            String mensaje
    ) {
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
