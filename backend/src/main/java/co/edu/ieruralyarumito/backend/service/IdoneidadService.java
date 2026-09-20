package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.CrearIdoneidadRequest;
import co.edu.ieruralyarumito.backend.dto.IdoneidadResponse;
import co.edu.ieruralyarumito.backend.dto.ActualizarIdoneidadRequest;
import co.edu.ieruralyarumito.backend.entity.Area;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.Idoneidad;
import co.edu.ieruralyarumito.backend.entity.TituloProfesional;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.repository.AreaRepository;
import co.edu.ieruralyarumito.backend.repository.AsignaturaRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.repository.IdoneidadRepository;
import co.edu.ieruralyarumito.backend.repository.TituloProfesionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.util.List;
import co.edu.ieruralyarumito.backend.dto.FinalizarVigenciaIdoneidadRequest;
import co.edu.ieruralyarumito.backend.exception.FechaVigenciaInvalidaException;
import co.edu.ieruralyarumito.backend.exception.TransicionEstadoNoPermitidaException;
import co.edu.ieruralyarumito.backend.exception.RelacionAcademicaInvalidaException;
import java.time.LocalDate;
import co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad;


// Contiene la lógica de negocio para la gestión de idoneidades docentes.
@Service
public class IdoneidadService {

    // Acceso a los datos de idoneidad.
    private final IdoneidadRepository idoneidadRepository;

    // Permite consultar docentes.
    private final DocenteRepository docenteRepository;

    // Permite consultar áreas.
    private final AreaRepository areaRepository;

    // Permite consultar asignaturas.
    private final AsignaturaRepository asignaturaRepository;

    // Permite consultar títulos profesionales.
    private final TituloProfesionalRepository tituloProfesionalRepository;

    // Inyección de dependencias mediante constructor.
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

    // Valida que la asignatura pertenezca al área indicada para la idoneidad.
    private void validarAsignaturaPerteneceArea(
            Asignatura asignatura,
            Area area) {

        if (!asignatura.getArea().getId().equals(area.getId())) {
            throw new RelacionAcademicaInvalidaException(
                    "La asignatura no pertenece al área indicada");
        }
    }

    // Valida que el título profesional pertenezca al docente de la idoneidad.
    private void validarTituloPerteneceDocente(
            TituloProfesional tituloProfesional,
            Docente docente) {

        if (!tituloProfesional.getDocente().getId().equals(docente.getId())) {
            throw new RelacionAcademicaInvalidaException(
                    "El título profesional no pertenece al docente indicado");
        }
    }

    // Valida que una idoneidad PRINCIPAL corresponda al área de nombramiento del docente.
    private void validarAreaPrincipal(
            Docente docente,
            Area area,
            TipoIdoneidad tipo) {

        if (tipo == TipoIdoneidad.PRINCIPAL
                && (docente.getAreaNombramiento() == null
                || !docente.getAreaNombramiento().getId().equals(area.getId()))) {

            throw new RelacionAcademicaInvalidaException(
                    "Una idoneidad PRINCIPAL debe corresponder al área de nombramiento del docente");
        }
    }

    // Valida que una asignatura de Media Técnica que exige exclusividad
// sea asociada únicamente a un docente exclusivo de Media Técnica.
    private void validarDocenteMediaTecnica(
            Docente docente,
            Asignatura asignatura) {

        if (asignatura.isEsMediaTecnica()
                && asignatura.isRequiereDocenteExclusivo()
                && !docente.isEsExclusivoMediaTecnica()) {

            throw new RelacionAcademicaInvalidaException(
                    "La asignatura de Media Técnica requiere un docente exclusivo de Media Técnica");
        }
    }

    // Valida que una idoneidad EXCEPCIONAL tenga una justificación.
    private void validarJustificacionExcepcional(
            TipoIdoneidad tipo,
            String justificacion) {

        if (tipo == TipoIdoneidad.EXCEPCIONAL
                && (justificacion == null || justificacion.isBlank())) {

            throw new RelacionAcademicaInvalidaException(
                    "Una idoneidad EXCEPCIONAL requiere justificación");
        }
    }

    // Valida que no exista otra idoneidad del mismo docente,
// área y asignatura con un periodo de vigencia superpuesto.
    private void validarSolapamientoIdoneidad(Idoneidad nuevaIdoneidad) {

        List<Idoneidad> existentes =
                idoneidadRepository.findByDocente_Id(
                        nuevaIdoneidad.getDocente().getId()
                );

        for (Idoneidad existente : existentes) {

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
                        "Ya existe una idoneidad para el mismo docente, área y asignatura con una vigencia superpuesta");
            }
        }
    }

    // Valida que el rango de vigencia tenga fechas coherentes.
    private void validarRangoVigencia(
            LocalDate vigenteDesde,
            LocalDate vigenteHasta) {

        if (vigenteHasta != null
                && vigenteHasta.isBefore(vigenteDesde)) {

            throw new FechaVigenciaInvalidaException(
                    "La fecha de finalización no puede ser anterior al inicio de vigencia");
        }
    }

    // Busca el título profesional indicado y rechaza la operación si no existe.
    private TituloProfesional obtenerTituloProfesional(UUID tituloSoporteId) {
        return tituloProfesionalRepository.findById(tituloSoporteId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El título profesional no existe"));
    }

    // Convierte una entidad Idoneidad en el DTO que devuelve la API.
    private IdoneidadResponse convertirAResponse(Idoneidad idoneidad) {

        IdoneidadResponse response = new IdoneidadResponse();

        response.setId(idoneidad.getId());
        response.setDocenteId(idoneidad.getDocente().getId());
        response.setAreaId(idoneidad.getArea().getId());

        // La asignatura es opcional.
        if (idoneidad.getAsignatura() != null) {
            response.setAsignaturaId(idoneidad.getAsignatura().getId());
        }

        response.setTipo(idoneidad.getTipo());
        response.setJustificacion(idoneidad.getJustificacion());
        response.setVigenteDesde(idoneidad.getVigenteDesde());
        response.setVigenteHasta(idoneidad.getVigenteHasta());

        // El título profesional es opcional.
        if (idoneidad.getTituloSoporte() != null) {
            response.setTituloSoporteId(idoneidad.getTituloSoporte().getId());
        }

        // El usuario aprobador es opcional.
        if (idoneidad.getAprobadaPor() != null) {
            response.setAprobadaPorId(idoneidad.getAprobadaPor().getId());
        }
        return response;
    }

    // Busca la idoneidad indicada y rechaza la operación si no existe.
    private Idoneidad obtenerIdoneidad(UUID id) {
        return idoneidadRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La idoneidad no existe"));
    }

    // Registra una nueva idoneidad docente.
    @Transactional
    public IdoneidadResponse registrarIdoneidad(CrearIdoneidadRequest request) {

        // Valida que el docente exista.
        Docente docente = obtenerDocente(request.getDocenteId());

        // Valida que el área exista.
        Area area = obtenerArea(request.getAreaId());

        // Valida que una idoneidad PRINCIPAL use el área de nombramiento del docente.
        validarAreaPrincipal(
                docente,
                area,
                request.getTipo()
        );

        // Construye la entidad Idoneidad.
        Idoneidad idoneidad = new Idoneidad();

        idoneidad.setDocente(docente);
        idoneidad.setArea(area);

        // Asocia la asignatura únicamente cuando fue enviada.
        if (request.getAsignaturaId() != null) {
            Asignatura asignatura =
                    obtenerAsignatura(request.getAsignaturaId());

            // Valida que la asignatura pertenezca al área seleccionada.
            validarAsignaturaPerteneceArea(asignatura, area);

            // Valida las condiciones especiales de Media Técnica.
            validarDocenteMediaTecnica(
                    docente,
                    asignatura
            );

            idoneidad.setAsignatura(asignatura);
        }

        // Establece el tipo de idoneidad.
        idoneidad.setTipo(request.getTipo());

        // Asocia el título profesional únicamente cuando fue enviado.
        if (request.getTituloSoporteId() != null) {
            TituloProfesional tituloSoporte =
                    obtenerTituloProfesional(request.getTituloSoporteId());

            // Valida que el título pertenezca al docente de la idoneidad.
            validarTituloPerteneceDocente(tituloSoporte, docente);

            idoneidad.setTituloSoporte(tituloSoporte);
        }

        // Valida que las fechas de vigencia sean coherentes.
        validarRangoVigencia(
                request.getVigenteDesde(),
                request.getVigenteHasta()
        );
        validarJustificacionExcepcional(
                request.getTipo(),
                request.getJustificacion()
        );
        // Completa los datos de la idoneidad.
        idoneidad.setJustificacion(request.getJustificacion());
        idoneidad.setVigenteDesde(request.getVigenteDesde());
        idoneidad.setVigenteHasta(request.getVigenteHasta());

        // Evita duplicados o vigencias superpuestas
        // para la misma habilitación del docente.
        validarSolapamientoIdoneidad(idoneidad);

        // Guarda la idoneidad en la base de datos.
        Idoneidad idoneidadGuardada =
                idoneidadRepository.save(idoneidad);

        return convertirAResponse(idoneidadGuardada);
    }

    // Consulta una idoneidad por su identificador.
    @Transactional(readOnly = true)
    public IdoneidadResponse consultarIdoneidad(UUID id) {

        Idoneidad idoneidad = idoneidadRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La idoneidad no existe"));

        return convertirAResponse(idoneidad);
    }

    // Lista todas las idoneidades registradas.
    @Transactional(readOnly = true)
    public List<IdoneidadResponse> listarIdoneidades() {

        return idoneidadRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // Lista las idoneidades asociadas a un docente existente.
    @Transactional(readOnly = true)
    public List<IdoneidadResponse> listarIdoneidadesPorDocente(UUID docenteId) {

        // Valida que el docente exista.
        obtenerDocente(docenteId);

        return idoneidadRepository.findByDocente_Id(docenteId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // Actualiza únicamente los datos permitidos de una idoneidad existente.
    @Transactional
    public IdoneidadResponse actualizarIdoneidad(
            UUID id,
            ActualizarIdoneidadRequest request) {

        // Valida que la idoneidad exista.
        Idoneidad idoneidad = obtenerIdoneidad(id);

        // Actualiza el título profesional de soporte cuando fue enviado.
        if (request.getTituloSoporteId() != null) {

            TituloProfesional tituloSoporte =
                    obtenerTituloProfesional(request.getTituloSoporteId());

            validarTituloPerteneceDocente(
                    tituloSoporte,
                    idoneidad.getDocente()
            );

            idoneidad.setTituloSoporte(tituloSoporte);
        }

        // Actualiza la justificación cuando fue enviada.
        if (request.getJustificacion() != null) {
            idoneidad.setJustificacion(request.getJustificacion());
        }

        // Una idoneidad EXCEPCIONAL siempre debe conservar justificación.
        validarJustificacionExcepcional(
                idoneidad.getTipo(),
                idoneidad.getJustificacion()
        );

        Idoneidad idoneidadActualizada =
                idoneidadRepository.save(idoneidad);

        return convertirAResponse(idoneidadActualizada);
    }

    // Finaliza la vigencia de una idoneidad sin eliminar el registro.
    @Transactional
    public IdoneidadResponse finalizarVigencia(
            UUID id,
            FinalizarVigenciaIdoneidadRequest request) {

        // Valida que la idoneidad exista.
        Idoneidad idoneidad = obtenerIdoneidad(id);

        // Evita finalizar nuevamente una idoneidad que ya tiene fecha de cierre.
        if (idoneidad.getVigenteHasta() != null) {
            throw new TransicionEstadoNoPermitidaException(
                    "La idoneidad ya tiene su vigencia finalizada");
        }

        // Valida que la fecha de finalización no sea anterior al inicio de vigencia.
        if (request.getVigenteHasta().isBefore(idoneidad.getVigenteDesde())) {
            throw new FechaVigenciaInvalidaException(
                    "La fecha de finalización no puede ser anterior al inicio de vigencia");
        }

        // Registra la fecha de finalización.
        idoneidad.setVigenteHasta(request.getVigenteHasta());

        // Guarda el cambio conservando el registro.
        Idoneidad idoneidadActualizada =
                idoneidadRepository.save(idoneidad);

        return convertirAResponse(idoneidadActualizada);
    }
}
