package py.edu.uc.lp3.msferreira.minecraft.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;

import java.util.List;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import jakarta.persistence.EntityManager;
import py.edu.uc.lp3.msferreira.minecraft.domain.Aldeano;
import py.edu.uc.lp3.msferreira.minecraft.domain.Creeper;
import py.edu.uc.lp3.msferreira.minecraft.domain.Entidad;
import py.edu.uc.lp3.msferreira.minecraft.domain.Jugador;
import py.edu.uc.lp3.msferreira.minecraft.repository.EntidadRepository;

@SpringBootTest
class ExplosionRollbackIntegrationTests {
    @Autowired
    private ExplosionService explosionService;

    @MockitoSpyBean
    private EntidadRepository entidadRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Creeper creeper;
    private Jugador jugador;
    private Aldeano aldeano;

    @BeforeEach
    void prepararParticipantes() {
        reset(entidadRepository);
        entidadRepository.deleteAll();
        creeper = entidadRepository.save(new Creeper(20, 0, 0, 0, 0));
        jugador = entidadRepository.save(new Jugador("Steve", 0, 20, 2, 0, 0, 0));
        aldeano = entidadRepository.save(new Aldeano(20, 4, 0, 0, 0, true, "Granjero"));
    }

    @Test
    void revierteAmbasSaludesSiLaPersistenciaFallaDespuesDelDano() {
        doAnswer(invocacion -> {
            assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
            Iterable<Entidad> argumento = invocacion.getArgument(0);
            List<Entidad> entidades = StreamSupport.stream(argumento.spliterator(), false).toList();
            assertEquals(List.of(12, 12), entidades.stream().map(Entidad::getSalud).toList());
            entityManager.flush();
            assertEquals(12, consultarSalud(jugador.getId()));
            assertEquals(12, consultarSalud(aldeano.getId()));
            throw new IllegalStateException("Fallo posterior al flush simulado");
        })
                .when(entidadRepository).saveAll(any());

        assertThrows(IllegalStateException.class, () -> explosionService.simular(
                creeper.getId(), jugador.getId(), aldeano.getId(), 3
        ));

        reset(entidadRepository);
        assertEquals(20, consultarSalud(jugador.getId()));
        assertEquals(20, consultarSalud(aldeano.getId()));
    }

    private int consultarSalud(Long id) {
        return jdbcTemplate.queryForObject("select salud from entidades where id = ?", Integer.class, id);
    }
}
