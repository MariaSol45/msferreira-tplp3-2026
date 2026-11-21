package py.edu.uc.lp3.msferreira.minecraft.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import py.edu.uc.lp3.msferreira.minecraft.domain.Jugada;

public interface JugadaRepository extends JpaRepository<Jugada, Long> {
    List<Jugada> findAllByOrderByIdAsc();
}
