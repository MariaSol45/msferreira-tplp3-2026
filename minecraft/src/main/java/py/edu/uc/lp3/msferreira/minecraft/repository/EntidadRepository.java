package py.edu.uc.lp3.msferreira.minecraft.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import py.edu.uc.lp3.msferreira.minecraft.domain.Entidad;

public interface EntidadRepository extends JpaRepository<Entidad, Long> {
}
