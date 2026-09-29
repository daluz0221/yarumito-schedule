package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarGradoRequest;
import co.edu.ieruralyarumito.backend.dto.CrearGradoRequest;
import co.edu.ieruralyarumito.backend.dto.GradoResponse;
import co.edu.ieruralyarumito.backend.entity.Grado;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.repository.GradoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// Contiene la lógica de negocio para la gestión de grados escolares.
@Service
public class GradoService {

    private final GradoRepository gradoRepository;

    // Inyección de dependencias mediante constructor.
    public GradoService(GradoRepository gradoRepository) {
        this.gradoRepository = gradoRepository;
    }

    // Configura automáticamente los datos institucionales
    // correspondientes al grado escolar indicado.
    private void configurarSegunNivel(
            Grado grado,
            int nivel) {

        grado.setNivel(nivel);

        switch (nivel) {

            case 6 -> {
                grado.setNombre("SEXTO");
                grado.setEsMedia(false);
                grado.setPrioridadAsignacion(5);
                grado.setHorasSemanalesEsperadas(30);
            }

            case 7 -> {
                grado.setNombre("SÉPTIMO");
                grado.setEsMedia(false);
                grado.setPrioridadAsignacion(4);
                grado.setHorasSemanalesEsperadas(30);
            }

            case 8 -> {
                grado.setNombre("OCTAVO");
                grado.setEsMedia(false);
                grado.setPrioridadAsignacion(3);
                grado.setHorasSemanalesEsperadas(30);
            }

            case 9 -> {
                grado.setNombre("NOVENO");
                grado.setEsMedia(false);
                grado.setPrioridadAsignacion(2);
                grado.setHorasSemanalesEsperadas(30);
            }

            case 10 -> {
                grado.setNombre("DÉCIMO");
                grado.setEsMedia(true);
                grado.setPrioridadAsignacion(1);
                grado.setHorasSemanalesEsperadas(37);
            }

            case 11 -> {
                grado.setNombre("UNDÉCIMO");
                grado.setEsMedia(true);
                grado.setPrioridadAsignacion(1);
                grado.setHorasSemanalesEsperadas(37);
            }

            default -> throw new IllegalArgumentException(
                    "El nivel del grado debe estar entre 6 y 11");
        }
    }

    // Convierte la entidad Grado en el DTO devuelto por la API.
    private GradoResponse convertirAResponse(Grado grado) {

        return new GradoResponse(
                grado.getId(),
                grado.getNivel(),
                grado.getNombre(),
                grado.isEsMedia(),
                grado.getPrioridadAsignacion(),
                grado.getHorasSemanalesEsperadas()
        );
    }

    // Registra un nuevo grado escolar.
    @Transactional
    public GradoResponse registrarGrado(
            CrearGradoRequest request) {

        Grado grado = new Grado();

        configurarSegunNivel(
                grado,
                request.getNivel());

        Grado gradoGuardado =
                gradoRepository.save(grado);

        return convertirAResponse(gradoGuardado);
    }

    // Consulta un grado por su identificador.
    @Transactional(readOnly = true)
    public GradoResponse consultarGrado(UUID id) {

        Grado grado = gradoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El grado no existe"));

        return convertirAResponse(grado);
    }

    // Lista los grados registrados con paginación.
    @Transactional(readOnly = true)
    public Page<GradoResponse> listarGrados(
            Pageable pageable) {

        return gradoRepository.findAll(pageable)
                .map(this::convertirAResponse);
    }

    // Actualiza un grado y recalcula automáticamente
    // todos los datos dependientes de su nivel.
    @Transactional
    public GradoResponse actualizarGrado(
            UUID id,
            ActualizarGradoRequest request) {

        Grado grado = gradoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El grado no existe"));

        configurarSegunNivel(
                grado,
                request.getNivel());

        Grado gradoActualizado =
                gradoRepository.save(grado);

        return convertirAResponse(gradoActualizado);
    }
}
