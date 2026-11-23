package py.edu.uc.lp3.msferreira.minecraft.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import py.edu.uc.lp3.msferreira.minecraft.domain.Entidad;

public interface EntidadRepository extends JpaRepository<Entidad, Long> {
    List<Entidad> findBySaludGreaterThan(int salud);

    @Query(value = "select id from entidades where id = :id for update", nativeQuery = true)
    Optional<Long> bloquearFilaPorId(@Param("id") Long id);
}
