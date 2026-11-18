package py.edu.uc.lp3.msferreira.minecraft.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import py.edu.uc.lp3.msferreira.minecraft.domain.Aldeano;
import py.edu.uc.lp3.msferreira.minecraft.domain.Creeper;
import py.edu.uc.lp3.msferreira.minecraft.domain.EntidadGenerica;
import py.edu.uc.lp3.msferreira.minecraft.domain.Jugador;

@SpringBootTest
@Transactional
class EntidadRepositoryIntegrationTests {
    @Autowired
    private EntidadRepository entidadRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void limpiarBaseDeDatos() {
        entidadRepository.deleteAll();
        entidadRepository.flush();
    }

    @Test
    void persisteLaJerarquiaJoinedEnLasTablasCorrespondientes() {
        Creeper creeper = entidadRepository.save(new Creeper(20, 0, 0, 0, 1, 5, 8, 3));
        Jugador jugador = entidadRepository.save(new Jugador("Steve", 0, 20, 0, 0, 0, 1));
        Aldeano aldeano = entidadRepository.save(new Aldeano(20, 0, 0, 0, 1, true, "Granjero"));
        EntidadGenerica generica = entidadRepository.save(new EntidadGenerica(30, 0, 0, 0, 1));
        entidadRepository.flush();
        entityManager.clear();

        assertInstanceOf(Creeper.class, entidadRepository.findById(creeper.getId()).orElseThrow());
        assertInstanceOf(Jugador.class, entidadRepository.findById(jugador.getId()).orElseThrow());
        assertInstanceOf(Aldeano.class, entidadRepository.findById(aldeano.getId()).orElseThrow());
        assertInstanceOf(EntidadGenerica.class, entidadRepository.findById(generica.getId()).orElseThrow());
        assertEquals(1, contarFila("ENTIDADES", creeper.getId()));
        assertEquals(1, contarFila("ENTIDADES_HOSTILES", creeper.getId()));
        assertEquals(1, contarFila("CREEPERS", creeper.getId()));
        assertEquals(1, contarFila("JUGADORES", jugador.getId()));
        assertEquals(1, contarFila("ENTIDADES_PASIVAS", aldeano.getId()));
        assertEquals(1, contarFila("ALDEANOS", aldeano.getId()));
        assertEquals(1, contarFila("ENTIDADES_GENERICAS", generica.getId()));
    }

    @Test
    void consultaSoloEntidadesVivas() {
        entidadRepository.save(new Jugador("Vivo", 0, 20, 0, 0, 0, 1));
        entidadRepository.save(new Jugador("Muerto", 0, 0, 0, 0, 0, 1));
        entidadRepository.flush();

        assertEquals(1, entidadRepository.findBySaludGreaterThan(0).size());
        assertEquals("Vivo", ((Jugador) entidadRepository.findBySaludGreaterThan(0).getFirst()).getNombre());
    }

    private int contarFila(String tabla, Long id) {
        return jdbcTemplate.queryForObject(
                "select count(*) from " + tabla + " where id = ?", Integer.class, id
        );
    }
}
