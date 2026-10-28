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
        entidad.setId(null);
        return entidadRepository.save(entidad);
    }

    public Entidad actualizar(Long id, Entidad datos) {
        validar(datos);

        Entidad entidad = obtener(id);
        entidad.setSalud(datos.getSalud());
        entidad.setPosicionX(datos.getPosicionX());
        entidad.setPosicionY(datos.getPosicionY());
        entidad.setPosicionZ(datos.getPosicionZ());
        entidad.setVelocidad(datos.getVelocidad());
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
        if (entidad.getSalud() <= 0) {
            throw new IllegalArgumentException("La salud debe ser mayor que 0.");
        }
        if (entidad.getVelocidad() < 0) {
            throw new IllegalArgumentException("La velocidad no puede ser negativa.");
        }
        if (!Double.isFinite(entidad.getPosicionX())
                || !Double.isFinite(entidad.getPosicionY())
                || !Double.isFinite(entidad.getPosicionZ())) {
            throw new IllegalArgumentException("Las coordenadas deben ser valores finitos.");
        }
    }
}
