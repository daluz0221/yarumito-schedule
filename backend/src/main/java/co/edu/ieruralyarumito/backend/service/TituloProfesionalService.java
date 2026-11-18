package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarTituloProfesionalRequest;
import co.edu.ieruralyarumito.backend.dto.CrearTituloProfesionalRequest;
import co.edu.ieruralyarumito.backend.dto.TituloProfesionalResponse;

import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.TituloProfesional;

import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.exception.TransicionEstadoNoPermitidaException;

import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.repository.IdoneidadRepository;
import co.edu.ieruralyarumito.backend.repository.TituloProfesionalRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// Gestiona los títulos profesionales de los docentes.
@Service
public class TituloProfesionalService {

    private final TituloProfesionalRepository tituloProfesionalRepository;
    private final DocenteRepository docenteRepository;

    // H-04: permite verificar las idoneidades
    // que dependen de un título profesional.
    private final IdoneidadRepository idoneidadRepository;

    public TituloProfesionalService(
            TituloProfesionalRepository tituloProfesionalRepository,
            DocenteRepository docenteRepository,
            IdoneidadRepository idoneidadRepository) {

        this.tituloProfesionalRepository =
                tituloProfesionalRepository;

        this.docenteRepository =
                docenteRepository;

        this.idoneidadRepository =
                idoneidadRepository;
    }

    // Obtiene el docente asociado.
    private Docente obtenerDocente(UUID docenteId) {

        return docenteRepository.findById(docenteId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El docente no existe"));
    }

    // Obtiene un título profesional existente.
    private TituloProfesional obtenerTituloProfesional(UUID id) {

        return tituloProfesionalRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El título profesional no existe"));
    }

    // H-04: protege los títulos profesionales retirados
    // frente a modificaciones no permitidas.
    private void validarTituloActivo(
            TituloProfesional tituloProfesional) {

        if (!tituloProfesional.isActivo()) {

            throw new TransicionEstadoNoPermitidaException(
                    "El título profesional se encuentra retirado. "
                            + "Debe reactivarlo primero");
        }
    }

    // H-04: impide retirar un título utilizado como
    // soporte de idoneidades cuya vigencia no ha finalizado.
    private void validarIdoneidadesVigentes(UUID tituloId) {

        boolean existenIdoneidadesVigentes =
                idoneidadRepository
                        .existeIdoneidadVigentePorTitulo(
                                tituloId,
                                LocalDate.now());

        if (existenIdoneidadesVigentes) {

            throw new TransicionEstadoNoPermitidaException(
                    "No se puede retirar el título profesional "
                            + "porque respalda idoneidades vigentes. "
                            + "Primero debe finalizar la vigencia "
                            + "de las idoneidades relacionadas");
        }
    }

    // Convierte una entidad al DTO de respuesta.
    private TituloProfesionalResponse convertirAResponse(
            TituloProfesional tituloProfesional) {

        TituloProfesionalResponse response =
                new TituloProfesionalResponse();

        response.setId(
                tituloProfesional.getId());

        response.setDocenteId(
                tituloProfesional.getDocente().getId());

        response.setNivel(
                tituloProfesional.getNivel());

        response.setNombreTitulo(
                tituloProfesional.getNombreTitulo());

        response.setInstitucion(
                tituloProfesional.getInstitucion());

        response.setAnioGraduacion(
                tituloProfesional.getAnioGraduacion());

        response.setArchivoSoporte(
                tituloProfesional.getArchivoSoporte());

        // H-04: informa el estado del título profesional.
        response.setActivo(
                tituloProfesional.isActivo());

        return response;
    }

    // Registra un nuevo título profesional.
    @Transactional
    public TituloProfesionalResponse registrarTituloProfesional(
            CrearTituloProfesionalRequest request) {

        Docente docente =
                obtenerDocente(request.getDocenteId());

        TituloProfesional tituloProfesional =
                new TituloProfesional();

        tituloProfesional.setDocente(docente);
        tituloProfesional.setNivel(request.getNivel());

        tituloProfesional.setNombreTitulo(
                request.getNombreTitulo());

        tituloProfesional.setInstitucion(
                request.getInstitucion());

        tituloProfesional.setAnioGraduacion(
                request.getAnioGraduacion());

        tituloProfesional.setArchivoSoporte(
                request.getArchivoSoporte());

        // Los títulos nuevos comienzan activos.
        tituloProfesional.setActivo(true);

        TituloProfesional tituloGuardado =
                tituloProfesionalRepository.save(
                        tituloProfesional);

        return convertirAResponse(tituloGuardado);
    }

    // Consulta un título por identificador.
    // Incluye títulos retirados.
    @Transactional(readOnly = true)
    public TituloProfesionalResponse consultarTituloProfesional(
            UUID id) {

        return convertirAResponse(
                obtenerTituloProfesional(id));
    }

    // Conserva la consulta de todos los títulos
    // de un docente, incluidos los retirados.
    @Transactional(readOnly = true)
    public List<TituloProfesionalResponse> listarTitulosPorDocente(
            UUID docenteId) {

        obtenerDocente(docenteId);

        return tituloProfesionalRepository
                .findByDocente_Id(docenteId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // H-04: consulta únicamente títulos activos.
    @Transactional(readOnly = true)
    public List<TituloProfesionalResponse> listarTitulosActivosPorDocente(
            UUID docenteId) {

        obtenerDocente(docenteId);

        return tituloProfesionalRepository
                .findByDocente_IdAndActivoTrue(docenteId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // H-04: consulta los títulos retirados.
    @Transactional(readOnly = true)
    public List<TituloProfesionalResponse> listarTitulosRetiradosPorDocente(
            UUID docenteId) {

        obtenerDocente(docenteId);

        return tituloProfesionalRepository
                .findByDocente_IdAndActivoFalse(docenteId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // Actualiza un título profesional activo.
    @Transactional
    public TituloProfesionalResponse actualizarTituloProfesional(
            UUID id,
            ActualizarTituloProfesionalRequest request) {

        TituloProfesional tituloProfesional =
                obtenerTituloProfesional(id);

        validarTituloActivo(tituloProfesional);

        // El docente propietario no se modifica.
        tituloProfesional.setNivel(request.getNivel());

        tituloProfesional.setNombreTitulo(
                request.getNombreTitulo());

        tituloProfesional.setInstitucion(
                request.getInstitucion());

        tituloProfesional.setAnioGraduacion(
                request.getAnioGraduacion());

        tituloProfesional.setArchivoSoporte(
                request.getArchivoSoporte());

        TituloProfesional tituloActualizado =
                tituloProfesionalRepository.save(
                        tituloProfesional);

        return convertirAResponse(tituloActualizado);
    }

    // H-04: retira un título profesional
    // sin eliminarlo físicamente de PostgreSQL.
    @Transactional
    public TituloProfesionalResponse retirarTituloProfesional(
            UUID id) {

        TituloProfesional tituloProfesional =
                obtenerTituloProfesional(id);

        validarTituloActivo(tituloProfesional);

        // H-04: protege las idoneidades vigentes
        // que utilizan este título como soporte.
        validarIdoneidadesVigentes(id);

        tituloProfesional.setActivo(false);

        TituloProfesional tituloRetirado =
                tituloProfesionalRepository.save(
                        tituloProfesional);

        return convertirAResponse(tituloRetirado);
    }

    // H-04: reactiva un título retirado.
    @Transactional
    public TituloProfesionalResponse reactivarTituloProfesional(
            UUID id) {

        TituloProfesional tituloProfesional =
                obtenerTituloProfesional(id);

        if (tituloProfesional.isActivo()) {

            throw new TransicionEstadoNoPermitidaException(
                    "El título profesional ya se encuentra activo");
        }

        tituloProfesional.setActivo(true);

        TituloProfesional tituloReactivado =
                tituloProfesionalRepository.save(
                        tituloProfesional);

        return convertirAResponse(tituloReactivado);
    }
}
