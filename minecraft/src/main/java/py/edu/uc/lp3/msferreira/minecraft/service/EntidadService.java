package py.edu.uc.lp3.msferreira.minecraft.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.edu.uc.lp3.msferreira.minecraft.domain.Entidad;
import py.edu.uc.lp3.msferreira.minecraft.repository.EntidadRepository;

@Service
@Transactional
public class EntidadService {
    private final EntidadRepository entidadRepository;

    public EntidadService(EntidadRepository entidadRepository) {
        this.entidadRepository = entidadRepository;
    }

    @Transactional(readOnly = true)
    public List<Entidad> listar() {
        return entidadRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Entidad obtener(Long id) {
        return entidadRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe una entidad con id " + id + "."));
    }

    public Entidad crear(Entidad entidad) {
        validar(entidad);
        return entidadRepository.save(entidad);
    }

    public Entidad actualizar(Long id, Entidad datos) {
        validar(datos);

        Entidad entidad = obtener(id);
        entidad.actualizarDesde(datos);
        return entidadRepository.save(entidad);
    }

    public void eliminar(Long id) {
        Entidad entidad = obtener(id);
        entidadRepository.delete(entidad);
    }

    private void validar(Entidad entidad) {
        if (entidad == null) {
            throw new IllegalArgumentException("La entidad es obligatoria.");
        }
        entidad.validarEstado();
    }
}
