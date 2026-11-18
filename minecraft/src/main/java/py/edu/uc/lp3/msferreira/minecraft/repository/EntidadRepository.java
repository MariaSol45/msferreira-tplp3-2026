package py.edu.uc.lp3.msferreira.minecraft.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import py.edu.uc.lp3.msferreira.minecraft.domain.Entidad;

public interface EntidadRepository extends JpaRepository<Entidad, Long> {
    List<Entidad> findBySaludGreaterThan(int salud);
}
