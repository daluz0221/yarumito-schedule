package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ParametroInstitucionalResponse;
import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.ParametroInstitucional;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.repository.AnioEscolarRepository;
import co.edu.ieruralyarumito.backend.repository.ParametroInstitucionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

// Contiene la lógica de negocio para los parámetros institucionales.
@Service
public class ParametroInstitucionalService {

    // Claves institucionales controladas por el backend.
    public static final String HORAS_DOCENTE_SEMANALES =
            "HORAS_DOCENTE_SEMANALES";

    public static final String MAX_HORAS_EXTRA =
            "MAX_HORAS_EXTRA";

    // Tipos técnicos admitidos por el ER V2.1.
    private static final Set<String> TIPOS_DATO_VALIDOS =
            Set.of("INT", "DECIMAL", "BOOL", "STRING");

    private final ParametroInstitucionalRepository parametroInstitucionalRepository;
    private final AnioEscolarRepository anioEscolarRepository;

    public ParametroInstitucionalService(
            ParametroInstitucionalRepository parametroInstitucionalRepository,
            AnioEscolarRepository anioEscolarRepository) {

        this.parametroInstitucionalRepository =
                parametroInstitucionalRepository;

        this.anioEscolarRepository =
                anioEscolarRepository;
    }

    // Inicializa los parámetros institucionales obligatorios de un año escolar.
    @Transactional
    public void inicializarParametrosParaAnio(
            AnioEscolar anioEscolar) {

        crearParametroSiNoExiste(
                anioEscolar,
                HORAS_DOCENTE_SEMANALES,
                "22",
                "INT",
                "Carga ordinaria docente semanal");

        crearParametroSiNoExiste(
                anioEscolar,
                MAX_HORAS_EXTRA,
                "10",
                "INT",
                "Máximo de horas extra semanales");
    }

    // Crea un parámetro controlado por el sistema únicamente si aún no existe.
    private void crearParametroSiNoExiste(
            AnioEscolar anioEscolar,
            String clave,
            String valor,
            String tipoDato,
            String descripcion) {

        if (parametroInstitucionalRepository
                .existsByAnioEscolar_IdAndClave(
                        anioEscolar.getId(),
                        clave)) {

            return;
        }

        validarTipoDato(tipoDato);
        validarValorSegunTipo(valor, tipoDato);

        ParametroInstitucional parametro =
                new ParametroInstitucional();

        parametro.setAnioEscolar(anioEscolar);
        parametro.setClave(clave);
        parametro.setValor(valor);
        parametro.setTipoDato(tipoDato);
        parametro.setDescripcion(descripcion);

        parametroInstitucionalRepository.save(parametro);
    }

    // Lista los parámetros institucionales asociados a un año escolar.
    @Transactional(readOnly = true)
    public List<ParametroInstitucionalResponse> listarPorAnio(
            UUID anioEscolarId) {

        validarAnioEscolarExiste(anioEscolarId);

        return parametroInstitucionalRepository
                .findByAnioEscolar_Id(anioEscolarId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // Consulta un parámetro institucional por año escolar y clave.
    @Transactional(readOnly = true)
    public ParametroInstitucionalResponse consultarPorAnioYClave(
            UUID anioEscolarId,
            String clave) {

        validarAnioEscolarExiste(anioEscolarId);

        ParametroInstitucional parametro =
                parametroInstitucionalRepository
                        .findByAnioEscolar_IdAndClave(
                                anioEscolarId,
                                clave)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "El parámetro institucional solicitado no está disponible para el año escolar"));

        return convertirAResponse(parametro);
    }

    // Obtiene el valor de un parámetro para uso interno de otras reglas de negocio.
    @Transactional(readOnly = true)
    public String obtenerValor(
            UUID anioEscolarId,
            String clave) {

        ParametroInstitucional parametro =
                parametroInstitucionalRepository
                        .findByAnioEscolar_IdAndClave(
                                anioEscolarId,
                                clave)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "No fue posible completar la operación porque falta una configuración institucional requerida"));

        validarTipoDato(parametro.getTipoDato());
        validarValorSegunTipo(
                parametro.getValor(),
                parametro.getTipoDato());

        return parametro.getValor();
    }

    // Valida que el año escolar exista.
    private void validarAnioEscolarExiste(
            UUID anioEscolarId) {

        if (!anioEscolarRepository.existsById(anioEscolarId)) {
            throw new RecursoNoEncontradoException(
                    "El año escolar no existe");
        }
    }

    // Valida que el tipo de dato corresponda a uno de los valores admitidos.
    private void validarTipoDato(
            String tipoDato) {

        if (tipoDato == null
                || !TIPOS_DATO_VALIDOS.contains(tipoDato)) {

            throw new IllegalArgumentException(
                    "El tipo de dato del parámetro institucional no es válido");
        }
    }

    // Valida que el valor pueda interpretarse según su tipo técnico.
    private void validarValorSegunTipo(
            String valor,
            String tipoDato) {

        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    "El valor del parámetro institucional no puede estar vacío");
        }

        try {
            switch (tipoDato) {

                case "INT" ->
                        Integer.parseInt(valor);

                case "DECIMAL" ->
                        new java.math.BigDecimal(valor);

                case "BOOL" -> {
                    if (!valor.equalsIgnoreCase("true")
                            && !valor.equalsIgnoreCase("false")) {

                        throw new IllegalArgumentException();
                    }
                }

                case "STRING" -> {
                    // No requiere conversión adicional.
                }

                default ->
                        throw new IllegalArgumentException();
            }

        } catch (NumberFormatException exception) {

            throw new IllegalArgumentException(
                    "El valor del parámetro institucional no corresponde con su tipo de dato");
        } catch (IllegalArgumentException exception) {

            throw new IllegalArgumentException(
                    "El valor del parámetro institucional no corresponde con su tipo de dato");
        }
    }

    // Convierte la entidad en el DTO devuelto por la API.
    private ParametroInstitucionalResponse convertirAResponse(
            ParametroInstitucional parametro) {

        ParametroInstitucionalResponse response =
                new ParametroInstitucionalResponse();

        response.setId(parametro.getId());
        response.setAnioEscolarId(
                parametro.getAnioEscolar().getId());
        response.setClave(parametro.getClave());
        response.setValor(parametro.getValor());
        response.setTipoDato(parametro.getTipoDato());
        response.setDescripcion(parametro.getDescripcion());

        return response;
    }
}
