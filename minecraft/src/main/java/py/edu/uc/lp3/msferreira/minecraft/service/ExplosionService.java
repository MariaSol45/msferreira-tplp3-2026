package py.edu.uc.lp3.msferreira.minecraft.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import py.edu.uc.lp3.msferreira.minecraft.domain.Aldeano;
import py.edu.uc.lp3.msferreira.minecraft.domain.Creeper;
import py.edu.uc.lp3.msferreira.minecraft.domain.Entidad;
import py.edu.uc.lp3.msferreira.minecraft.domain.Jugador;

@Service
public class ExplosionService {
    public ResultadoExplosion simular(int tiempoTranscurrido,
                                      double posicionXJugador, double posicionXAldeano) {
        Creeper creeper = new Creeper(20, 0, 0, 0, 0);
        Entidad jugador = new Jugador("Steve", 0, 20, posicionXJugador, 0, 0, 0);
        Entidad aldeano = new Aldeano(20, posicionXAldeano, 0, 0, 0, true, "Granjero");
        List<ObjetivoSimulacion> objetivos = List.of(
                new ObjetivoSimulacion(jugador, "Steve"),
                new ObjetivoSimulacion(aldeano, "Granjero")
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
                    objetivo.identificador(),
                    creeper.distanciaA(entidad),
                    saludInicial,
                    entidad.getSalud(),
                    danoRecibido
            ));
        }

        return new ResultadoExplosion(
                "Explosión de Creeper",
                List.of("Creeper", "Jugador", "Aldeano"),
                creeper.getTiempoExplosion(),
                tiempoTranscurrido,
                creeper.getRadioExplosion(),
                creeper.getDanoExplosion(),
                impactos
        );
    }

    private record ObjetivoSimulacion(Entidad entidad, String identificador) {
    }

    public record ResultadoExplosion(
            String evento,
            List<String> clasesParticipantes,
            int tiempoExplosion,
            int tiempoTranscurrido,
            double radioExplosion,
            int danoBase,
            List<ImpactoExplosion> objetivos
    ) {
    }

    public record ImpactoExplosion(
            String tipo,
            String identificador,
            double distancia,
            int saludInicial,
            int saludFinal,
            int danoRecibido
    ) {
    }
}
