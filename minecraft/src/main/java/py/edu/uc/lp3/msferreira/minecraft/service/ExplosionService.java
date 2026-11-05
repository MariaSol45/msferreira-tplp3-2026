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

@Service
public class ExplosionService {
    private final EntidadRepository entidadRepository;

    public ExplosionService(EntidadRepository entidadRepository) {
        this.entidadRepository = entidadRepository;
    }

    @Transactional
    public ResultadoExplosion simular(Long creeperId, Long jugadorId, Long aldeanoId, int tiempoTranscurrido) {
        Creeper creeper = obtenerComo(creeperId, Creeper.class);
        Jugador jugador = obtenerComo(jugadorId, Jugador.class);
        Aldeano aldeano = obtenerComo(aldeanoId, Aldeano.class);
        List<ObjetivoSimulacion> objetivos = List.of(
                new ObjetivoSimulacion(jugador, jugador.getNombre()),
                new ObjetivoSimulacion(aldeano, aldeano.getProfesion())
        );

        creeper.explotar(tiempoTranscurrido);

        // Se validan todos los objetivos antes de modificar su salud para evitar resultados parciales.
        for (ObjetivoSimulacion objetivo : objetivos) {
            creeper.validarObjetivoDentroDelRadio(objetivo.entidad());
        }

        List<ImpactoExplosion> impactos = new ArrayList<>();
        for (ObjetivoSimulacion objetivo : objetivos) {
            Entidad entidad = objetivo.entidad();
            int saludInicial = entidad.getSalud();
            int danoRecibido = creeper.aplicarDanoExplosion(entidad);
            impactos.add(new ImpactoExplosion(
                    entidad.getClass().getSimpleName(),
                    entidad.getId(),
                    objetivo.identificador(),
                    creeper.distanciaA(entidad),
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

    private <T extends Entidad> T obtenerComo(Long id, Class<T> tipoEsperado) {
        if (id == null) {
            throw new IllegalArgumentException("El id de " + tipoEsperado.getSimpleName() + " es obligatorio.");
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

    private record ObjetivoSimulacion(Entidad entidad, String identificador) {
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
