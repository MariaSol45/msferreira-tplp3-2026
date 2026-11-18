package py.edu.uc.lp3.msferreira.minecraft.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.edu.uc.lp3.msferreira.minecraft.domain.Aldeano;
import py.edu.uc.lp3.msferreira.minecraft.domain.Animal;
import py.edu.uc.lp3.msferreira.minecraft.domain.Creeper;
import py.edu.uc.lp3.msferreira.minecraft.domain.Entidad;
import py.edu.uc.lp3.msferreira.minecraft.domain.EntidadGenerica;
import py.edu.uc.lp3.msferreira.minecraft.domain.EntidadHostil;
import py.edu.uc.lp3.msferreira.minecraft.domain.Esqueleto;
import py.edu.uc.lp3.msferreira.minecraft.domain.Jugador;
import py.edu.uc.lp3.msferreira.minecraft.domain.UICapable;
import py.edu.uc.lp3.msferreira.minecraft.domain.Zombie;
import py.edu.uc.lp3.msferreira.minecraft.repository.EntidadRepository;

@Service
@Transactional
public class EntidadService {
    private static final int SALUD_MAXIMA_VANILLA = 20;
    private final EntidadRepository entidadRepository;

    public EntidadService(EntidadRepository entidadRepository) {
        this.entidadRepository = entidadRepository;
    }

    @Transactional(readOnly = true)
    public List<Entidad> listar() {
        return entidadRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Entidad> listarVivas() {
        return entidadRepository.findBySaludGreaterThan(0);
    }

    @Transactional(readOnly = true)
    public Entidad obtener(Long id) {
        validarId(id);
        return entidadRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe una entidad con id " + id + "."));
    }

    public Entidad crear(Entidad entidad) {
        validar(entidad, true);
        validarEntradaPersistible(entidad);
        return entidadRepository.save(entidad);
    }

    public Entidad actualizar(Long id, Entidad datos) {
        validarId(id);
        validar(datos, false);
        validarEntradaPersistible(datos);
        Entidad entidad = obtener(id);
        if (!entidad.getClass().equals(datos.getClass())) {
            throw new IllegalArgumentException("No se puede cambiar el tipo de una entidad existente.");
        }
        EntidadStateWriter.reemplazar(entidad, datos);
        return entidadRepository.save(entidad);
    }

    public void eliminar(Long id) {
        Entidad entidad = obtener(id);
        entidadRepository.delete(entidad);
    }

    public CreacionTransitoria crearTransitoria(
            String tipo, int salud, double x, double y, double z, int velocidad,
            double rango, int dano, Integer tiempoExplosion, String profesion,
            Boolean domesticable, Boolean montar, String nombre, int experiencia) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo es obligatorio.");
        }

        Entidad entidad;
        String constructorUtilizado = "completo";
        if (tipo.equalsIgnoreCase("creeper")) {
            if (tiempoExplosion == null) {
                entidad = new Creeper(salud, x, y, z, velocidad, rango, dano);
                constructorUtilizado = "simplificado";
            } else {
                entidad = new Creeper(salud, x, y, z, velocidad, rango, dano, tiempoExplosion);
            }
        } else if (tipo.equalsIgnoreCase("zombie")) {
            entidad = new Zombie(salud, x, y, z, velocidad, rango, dano);
        } else if (tipo.equalsIgnoreCase("esqueleto")) {
            entidad = new Esqueleto(salud, x, y, z, velocidad, rango, dano);
        } else if (tipo.equalsIgnoreCase("aldeano")) {
            entidad = new Aldeano(salud, x, y, z, velocidad,
                    domesticable == null || domesticable, profesion == null ? "Granjero" : profesion);
        } else if (tipo.equalsIgnoreCase("animal")) {
            entidad = new Animal(salud, x, y, z, velocidad,
                    domesticable != null && domesticable, montar != null && montar);
        } else if (tipo.equalsIgnoreCase("jugador")) {
            entidad = new Jugador(nombre, experiencia, salud, x, y, z, velocidad);
        } else {
            throw new IllegalArgumentException(
                    "Tipos válidos: creeper, zombie, esqueleto, aldeano, animal o jugador"
            );
        }

        validar(entidad, true);
        return new CreacionTransitoria(entidad, constructorUtilizado);
    }

    public ResultadoAtaque atacar(int saludObjetivo, double rango, int dano) {
        return atacarValidado(saludObjetivo, rango, dano, null);
    }

    public ResultadoAtaque atacar(int saludObjetivo, double rango, int dano, double distancia) {
        if (!Double.isFinite(distancia) || distancia < 0) {
            throw new IllegalArgumentException("La distancia debe ser finita y no negativa.");
        }
        if (distancia > rango) {
            throw new IllegalArgumentException("El objetivo está fuera del rango de detección.");
        }
        return atacarValidado(saludObjetivo, rango, dano, distancia);
    }

    @Transactional(readOnly = true)
    public UICapable obtenerElementoUI(
            Long id, String tipo, int salud, double x, double y, double z,
            int velocidad, String nombre, int experiencia, String profesion) {
        if (id != null) {
            Entidad entidad = obtener(id);
            if (!(entidad instanceof UICapable elementoUI)) {
                throw new IllegalArgumentException("La entidad con id " + id + " no implementa UICapable.");
            }
            return elementoUI;
        }

        if (tipo == null) {
            throw new IllegalArgumentException("Debe indicar id o tipo para consultar la UI.");
        }
        Entidad entidad;
        if (tipo.equalsIgnoreCase("jugador")) {
            entidad = new Jugador(nombre, experiencia, salud, x, y, z, velocidad);
        } else if (tipo.equalsIgnoreCase("aldeano")) {
            entidad = new Aldeano(salud, x, y, z, velocidad, true, profesion);
        } else {
            throw new IllegalArgumentException("Tipo válido: jugador o aldeano");
        }
        validar(entidad, true);
        return (UICapable) entidad;
    }

    void validarParaOperacion(Entidad entidad, boolean debeEstarViva) {
        validar(entidad, false);
        if (debeEstarViva && entidad.getSalud() == 0) {
            throw new IllegalArgumentException(
                    "La entidad " + entidad.getClass().getSimpleName() + " debe estar viva para participar."
            );
        }
    }

    int aplicarDano(Entidad objetivo, int cantidad) {
        validarParaOperacion(objetivo, false);
        if (cantidad <= 0) {
            throw new IllegalArgumentException("El daño debe ser mayor que 0.");
        }
        int saludFinal = Math.max(0, objetivo.getSalud() - cantidad);
        EntidadStateWriter.cambiarSalud(objetivo, saludFinal);
        return saludFinal;
    }

    private ResultadoAtaque atacarValidado(int saludObjetivo, double rango, int dano, Double distancia) {
        Creeper atacante = new Creeper(20, 0, 0, 0, 1, rango, dano);
        Entidad objetivo = new EntidadGenerica(saludObjetivo, 0, 0, 0, 1);
        validar(atacante, true);
        validar(objetivo, true);
        int saludInicial = objetivo.getSalud();
        aplicarDano(objetivo, atacante.getDanoAtaque());
        return new ResultadoAtaque(
                atacante, saludInicial, objetivo.getSalud(),
                distancia == null ? "atacar(Entidad)" : "atacar(Entidad, double)", distancia
        );
    }

    private void validar(Entidad entidad, boolean creacion) {
        if (entidad == null) {
            throw new IllegalArgumentException("La entidad es obligatoria.");
        }

        validarSalud(entidad, creacion);
        if (!Double.isFinite(entidad.getPosicionX())
                || !Double.isFinite(entidad.getPosicionY())
                || !Double.isFinite(entidad.getPosicionZ())) {
            throw new IllegalArgumentException("Las coordenadas deben ser valores finitos.");
        }
        if (entidad.getVelocidad() < 0) {
            throw new IllegalArgumentException("La velocidad no puede ser negativa.");
        }
        if (entidad instanceof EntidadHostil hostil) {
            if (!Double.isFinite(hostil.getRangoDeteccion()) || hostil.getRangoDeteccion() < 0) {
                throw new IllegalArgumentException("El rango de detección debe ser finito y no negativo.");
            }
            if (hostil.getDanoAtaque() <= 0) {
                throw new IllegalArgumentException("El daño de ataque debe ser mayor que 0.");
            }
        }
        if (entidad instanceof Creeper creeper && creeper.getTiempoExplosion() <= 0) {
            throw new IllegalArgumentException("El tiempo de explosión debe ser mayor que 0.");
        }
        if (entidad instanceof Jugador jugador) {
            if (jugador.getNombre() == null || jugador.getNombre().isBlank()) {
                throw new IllegalArgumentException("El nombre no puede ser nulo ni estar en blanco.");
            }
            if (jugador.getNivelEXP() < 0) {
                throw new IllegalArgumentException("El nivel de experiencia no puede ser negativo.");
            }
        }
        if (entidad instanceof Aldeano aldeano
                && (aldeano.getProfesion() == null || aldeano.getProfesion().isBlank())) {
            throw new IllegalArgumentException("La profesión no puede ser nula ni estar en blanco.");
        }
    }

    private void validarSalud(Entidad entidad, boolean creacion) {
        int saludMinima = creacion ? 1 : 0;
        if (entidad.getSalud() < saludMinima) {
            if (entidad instanceof Jugador) {
                throw new IllegalArgumentException(
                        creacion
                                ? "La salud inicial del Jugador debe estar entre 1 y 20."
                                : "La salud del Jugador debe estar entre 0 y 20."
                );
            }
            throw new IllegalArgumentException(
                    creacion ? "La salud inicial debe ser mayor que 0." : "La salud no puede ser negativa."
            );
        }

        if (tieneSaludMaximaVanilla(entidad) && entidad.getSalud() > SALUD_MAXIMA_VANILLA) {
            throw new IllegalArgumentException(
                    "La salud de " + entidad.getClass().getSimpleName() + " no puede superar 20."
            );
        }
    }

    private boolean tieneSaludMaximaVanilla(Entidad entidad) {
        return entidad instanceof Jugador
                || entidad instanceof Creeper
                || entidad instanceof Aldeano
                || entidad instanceof Zombie
                || entidad instanceof Esqueleto;
    }

    private void validarEntradaPersistible(Entidad entidad) {
        if (entidad.getId() != null) {
            throw new IllegalArgumentException("El id es generado por la persistencia y debe omitirse del cuerpo.");
        }
        Class<?> tipo = entidad.getClass();
        if (tipo != EntidadGenerica.class
                && tipo != Creeper.class
                && tipo != Jugador.class
                && tipo != Aldeano.class) {
            throw new IllegalArgumentException(
                    "El tipo " + tipo.getSimpleName() + " no forma parte de la jerarquía persistible."
            );
        }
    }

    private void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El id debe ser un número positivo.");
        }
    }

    public record CreacionTransitoria(Entidad entidad, String constructorUtilizado) {
    }

    public record ResultadoAtaque(
            Creeper atacante,
            int saludInicial,
            int saludFinal,
            String variante,
            Double distancia
    ) {
    }
}
