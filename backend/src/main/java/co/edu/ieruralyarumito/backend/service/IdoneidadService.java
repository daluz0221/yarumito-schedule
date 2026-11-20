package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarIdoneidadRequest;
import co.edu.ieruralyarumito.backend.dto.CrearIdoneidadRequest;
import co.edu.ieruralyarumito.backend.dto.FinalizarVigenciaIdoneidadRequest;
import co.edu.ieruralyarumito.backend.dto.IdoneidadResponse;

import co.edu.ieruralyarumito.backend.entity.Area;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.Idoneidad;
import co.edu.ieruralyarumito.backend.entity.TituloProfesional;
import co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad;

import co.edu.ieruralyarumito.backend.exception.FechaVigenciaInvalidaException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.exception.RelacionAcademicaInvalidaException;
import co.edu.ieruralyarumito.backend.exception.TransicionEstadoNoPermitidaException;

import co.edu.ieruralyarumito.backend.repository.AreaRepository;
import co.edu.ieruralyarumito.backend.repository.AsignaturaRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.repository.IdoneidadRepository;
import co.edu.ieruralyarumito.backend.repository.TituloProfesionalRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// Contiene la lógica de negocio para la gestión de idoneidades docentes.
@Service
public class IdoneidadService {

    private final IdoneidadRepository idoneidadRepository;
    private final DocenteRepository docenteRepository;
    private final AreaRepository areaRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final TituloProfesionalRepository tituloProfesionalRepository;

    public IdoneidadService(
            IdoneidadRepository idoneidadRepository,
            DocenteRepository docenteRepository,
            AreaRepository areaRepository,
            AsignaturaRepository asignaturaRepository,
            TituloProfesionalRepository tituloProfesionalRepository) {

        this.idoneidadRepository = idoneidadRepository;
        this.docenteRepository = docenteRepository;
        this.areaRepository = areaRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.tituloProfesionalRepository = tituloProfesionalRepository;
    }

    // Busca el docente indicado y rechaza la operación si no existe.
    private Docente obtenerDocente(UUID docenteId) {

        return docenteRepository.findById(docenteId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El docente no existe"));
    }

    // Busca el área indicada y rechaza la operación si no existe.
    private Area obtenerArea(UUID areaId) {

        return areaRepository.findById(areaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El área no existe"));
    }

    // Busca la asignatura indicada y rechaza la operación si no existe.
    private Asignatura obtenerAsignatura(UUID asignaturaId) {

        return asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La asignatura no existe"));
    }

    // Busca el título profesional indicado.
    private TituloProfesional obtenerTituloProfesional(UUID tituloSoporteId) {

        return tituloProfesionalRepository.findById(tituloSoporteId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El título profesional no existe"));
    }

    // Busca una idoneidad por su identificador.
    private Idoneidad obtenerIdoneidad(UUID id) {

        return idoneidadRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La idoneidad no existe"));
    }

    // Valida que la asignatura pertenezca al área indicada.
    private void validarAsignaturaPerteneceArea(
            Asignatura asignatura,
            Area area) {

        if (!asignatura.getArea().getId().equals(area.getId())) {

            throw new RelacionAcademicaInvalidaException(
                    "La asignatura no pertenece al área indicada");
        }
    }

    // Valida que el título pertenezca al docente de la idoneidad.
    private void validarTituloPerteneceDocente(
            TituloProfesional tituloProfesional,
            Docente docente) {

        if (!tituloProfesional.getDocente().getId()
                .equals(docente.getId())) {

            throw new RelacionAcademicaInvalidaException(
                    "El título profesional no pertenece al docente indicado");
        }
    }

    // H-04: un título retirado no puede respaldar
    // una nueva idoneidad ni sustituir el soporte
    // de una idoneidad durante su actualización.
    private void validarTituloActivo(
            TituloProfesional tituloProfesional) {

        if (!tituloProfesional.isActivo()) {

            throw new TransicionEstadoNoPermitidaException(
                    "No se puede utilizar un título profesional retirado "
                            + "como soporte de una idoneidad");
        }
    }

    // Valida que una idoneidad PRINCIPAL corresponda
    // al área de nombramiento del docente.
    private void validarAreaPrincipal(
            Docente docente,
            Area area,
            TipoIdoneidad tipo) {

        if (tipo == TipoIdoneidad.PRINCIPAL
                && (docente.getAreaNombramiento() == null
                || !docente.getAreaNombramiento()
                .getId().equals(area.getId()))) {

            throw new RelacionAcademicaInvalidaException(
                    "Una idoneidad PRINCIPAL debe corresponder "
                            + "al área de nombramiento del docente");
        }
    }

    // Valida las condiciones especiales de Media Técnica.
    private void validarDocenteMediaTecnica(
            Docente docente,
            Asignatura asignatura) {

        if (asignatura.isEsMediaTecnica()
                && asignatura.isRequiereDocenteExclusivo()
                && !docente.isEsExclusivoMediaTecnica()) {

            throw new RelacionAcademicaInvalidaException(
                    "La asignatura de Media Técnica requiere "
                            + "un docente exclusivo de Media Técnica");
        }
    }

    // Valida la justificación de idoneidades excepcionales.
    private void validarJustificacionExcepcional(
            TipoIdoneidad tipo,
            String justificacion) {

        if (tipo == TipoIdoneidad.EXCEPCIONAL
                && (justificacion == null || justificacion.isBlank())) {

            throw new RelacionAcademicaInvalidaException(
                    "Una idoneidad EXCEPCIONAL requiere justificación");
        }
    }

    // Valida que el periodo de vigencia sea coherente.
    private void validarRangoVigencia(
            LocalDate vigenteDesde,
            LocalDate vigenteHasta) {

        if (vigenteHasta != null
                && vigenteHasta.isBefore(vigenteDesde)) {

            throw new FechaVigenciaInvalidaException(
                    "La fecha de finalización no puede ser "
                            + "anterior al inicio de vigencia");
        }
    }

    // Evita idoneidades superpuestas para el mismo
    // docente, área y asignatura.
    private void validarSolapamientoIdoneidad(
            Idoneidad nuevaIdoneidad) {

        List<Idoneidad> existentes =
                idoneidadRepository.findByDocente_Id(
                        nuevaIdoneidad.getDocente().getId()
                );

        for (Idoneidad existente : existentes) {

            // Al editar, la propia idoneidad no debe compararse consigo misma.
            if (nuevaIdoneidad.getId() != null
                    && nuevaIdoneidad.getId().equals(existente.getId())) {
                continue;
            }

            boolean mismaArea =
                    existente.getArea().getId()
                            .equals(nuevaIdoneidad.getArea().getId());

            boolean mismaAsignatura;

            if (existente.getAsignatura() == null
                    && nuevaIdoneidad.getAsignatura() == null) {

                mismaAsignatura = true;

            } else if (existente.getAsignatura() != null
                    && nuevaIdoneidad.getAsignatura() != null) {

                mismaAsignatura =
                        existente.getAsignatura().getId()
                                .equals(nuevaIdoneidad.getAsignatura().getId());

            } else {

                mismaAsignatura = false;
            }

            if (!mismaArea || !mismaAsignatura) {
                continue;
            }

            LocalDate inicioNueva =
                    nuevaIdoneidad.getVigenteDesde();

            LocalDate finNueva =
                    nuevaIdoneidad.getVigenteHasta();

            LocalDate inicioExistente =
                    existente.getVigenteDesde();

            LocalDate finExistente =
                    existente.getVigenteHasta();

            boolean nuevaEmpiezaAntesDeQueTermineExistente =
                    finExistente == null
                            || !inicioNueva.isAfter(finExistente);

            boolean existenteEmpiezaAntesDeQueTermineNueva =
                    finNueva == null
                            || !inicioExistente.isAfter(finNueva);

            if (nuevaEmpiezaAntesDeQueTermineExistente
                    && existenteEmpiezaAntesDeQueTermineNueva) {

                throw new RelacionAcademicaInvalidaException(
                        "Ya existe una idoneidad para el mismo docente, "
                                + "área y asignatura con una vigencia superpuesta");
            }
        }
    }

    // Convierte una entidad Idoneidad al DTO de respuesta.
    private IdoneidadResponse convertirAResponse(
            Idoneidad idoneidad) {

        IdoneidadResponse response =
                new IdoneidadResponse();

        response.setId(idoneidad.getId());
        response.setDocenteId(idoneidad.getDocente().getId());
        response.setAreaId(idoneidad.getArea().getId());

        if (idoneidad.getAsignatura() != null) {

            response.setAsignaturaId(
                    idoneidad.getAsignatura().getId());
        }

        response.setTipo(idoneidad.getTipo());
        response.setJustificacion(idoneidad.getJustificacion());
        response.setVigenteDesde(idoneidad.getVigenteDesde());
        response.setVigenteHasta(idoneidad.getVigenteHasta());

        if (idoneidad.getTituloSoporte() != null) {

            response.setTituloSoporteId(
                    idoneidad.getTituloSoporte().getId());
        }

        if (idoneidad.getAprobadaPor() != null) {

            response.setAprobadaPorId(
                    idoneidad.getAprobadaPor().getId());
        }

        return response;
    }

    // Registra una nueva idoneidad docente.
    @Transactional
    public IdoneidadResponse registrarIdoneidad(
            CrearIdoneidadRequest request) {

        Docente docente =
                obtenerDocente(request.getDocenteId());

        Area area =
                obtenerArea(request.getAreaId());

        validarAreaPrincipal(
                docente,
                area,
                request.getTipo()
        );

        Idoneidad idoneidad = new Idoneidad();

        idoneidad.setDocente(docente);
        idoneidad.setArea(area);

        if (request.getAsignaturaId() != null) {

            Asignatura asignatura =
                    obtenerAsignatura(request.getAsignaturaId());

            validarAsignaturaPerteneceArea(
                    asignatura,
                    area
            );

            validarDocenteMediaTecnica(
                    docente,
                    asignatura
            );

            idoneidad.setAsignatura(asignatura);
        }

        idoneidad.setTipo(request.getTipo());

        // H-04: impide crear idoneidades respaldadas
        // por títulos profesionales retirados.
        if (request.getTituloSoporteId() != null) {

            TituloProfesional tituloSoporte =
                    obtenerTituloProfesional(
                            request.getTituloSoporteId());

            validarTituloPerteneceDocente(
                    tituloSoporte,
                    docente
            );

            validarTituloActivo(tituloSoporte);

            idoneidad.setTituloSoporte(tituloSoporte);
        }

        validarRangoVigencia(
                request.getVigenteDesde(),
                request.getVigenteHasta()
        );

        validarJustificacionExcepcional(
                request.getTipo(),
                request.getJustificacion()
        );

        idoneidad.setJustificacion(
                request.getJustificacion());

        idoneidad.setVigenteDesde(
                request.getVigenteDesde());

        idoneidad.setVigenteHasta(
                request.getVigenteHasta());

        validarSolapamientoIdoneidad(idoneidad);

        Idoneidad idoneidadGuardada =
                idoneidadRepository.save(idoneidad);

        return convertirAResponse(idoneidadGuardada);
    }

    // Consulta una idoneidad por identificador.
    @Transactional(readOnly = true)
    public IdoneidadResponse consultarIdoneidad(UUID id) {

        return convertirAResponse(
                obtenerIdoneidad(id));
    }

    // Consulta todas las idoneidades.
    @Transactional(readOnly = true)
    public List<IdoneidadResponse> listarIdoneidades() {

        return idoneidadRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // Consulta las idoneidades de un docente.
    @Transactional(readOnly = true)
    public List<IdoneidadResponse> listarIdoneidadesPorDocente(
            UUID docenteId) {

        obtenerDocente(docenteId);

        return idoneidadRepository.findByDocente_Id(docenteId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // Actualiza todos los datos editables de una idoneidad existente.
    @Transactional
    public IdoneidadResponse actualizarIdoneidad(
            UUID id,
            ActualizarIdoneidadRequest request) {

        Idoneidad idoneidad = obtenerIdoneidad(id);

        Docente docente = idoneidad.getDocente();
        Area area = obtenerArea(request.getAreaId());

        // Una idoneidad PRINCIPAL debe conservar la relación con el área
        // de nombramiento, incluso cuando se cambia el tipo o el área.
        validarAreaPrincipal(
                docente,
                area,
                request.getTipo()
        );

        // null elimina la asignatura específica y extiende la idoneidad
        // a toda el área.
        Asignatura asignatura = null;
        if (request.getAsignaturaId() != null) {
            asignatura = obtenerAsignatura(request.getAsignaturaId());
            validarAsignaturaPerteneceArea(asignatura, area);
            validarDocenteMediaTecnica(docente, asignatura);
        }

        // null elimina el título de soporte.
        TituloProfesional tituloSoporte = null;
        if (request.getTituloSoporteId() != null) {
            tituloSoporte =
                    obtenerTituloProfesional(request.getTituloSoporteId());

            validarTituloPerteneceDocente(
                    tituloSoporte,
                    docente
            );

            validarTituloActivo(tituloSoporte);
        }

        validarRangoVigencia(
                request.getVigenteDesde(),
                request.getVigenteHasta()
        );
        validarJustificacionExcepcional(
                request.getTipo(),
                request.getJustificacion()
        );

        idoneidad.setArea(area);
        idoneidad.setAsignatura(asignatura);
        idoneidad.setTipo(request.getTipo());
        idoneidad.setTituloSoporte(tituloSoporte);
        idoneidad.setJustificacion(request.getJustificacion());
        idoneidad.setVigenteDesde(request.getVigenteDesde());
        idoneidad.setVigenteHasta(request.getVigenteHasta());

        // Revalida posibles cruces después de cambiar área, asignatura o fechas.
        validarSolapamientoIdoneidad(idoneidad);

        Idoneidad idoneidadActualizada =
                idoneidadRepository.save(idoneidad);

        return convertirAResponse(idoneidadActualizada);
    }

    // Finaliza la vigencia sin eliminar la idoneidad.
    @Transactional
    public IdoneidadResponse finalizarVigencia(
            UUID id,
            FinalizarVigenciaIdoneidadRequest request) {

        Idoneidad idoneidad = obtenerIdoneidad(id);

        if (idoneidad.getVigenteHasta() != null) {

            throw new TransicionEstadoNoPermitidaException(
                    "La idoneidad ya tiene su vigencia finalizada");
        }

        if (request.getVigenteHasta()
                .isBefore(idoneidad.getVigenteDesde())) {

            throw new FechaVigenciaInvalidaException(
                    "La fecha de finalización no puede ser "
                            + "anterior al inicio de vigencia");
        }

        idoneidad.setVigenteHasta(
                request.getVigenteHasta());

        Idoneidad idoneidadActualizada =
                idoneidadRepository.save(idoneidad);

        return convertirAResponse(idoneidadActualizada);
    }
}
