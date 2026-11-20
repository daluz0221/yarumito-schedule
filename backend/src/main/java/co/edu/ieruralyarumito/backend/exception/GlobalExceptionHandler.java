package co.edu.ieruralyarumito.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

// Centraliza el manejo de excepciones generadas por la API.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Maneja los errores cuando un recurso solicitado no existe.
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> manejarRecursoNoEncontrado(
            RecursoNoEncontradoException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "mensaje",
                        exception.getMessage()));
    }

    // Maneja los errores cuando se intenta registrar un recurso duplicado.
    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<Map<String, String>> manejarRecursoDuplicado(
            RecursoDuplicadoException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "mensaje",
                        exception.getMessage()));
    }

    // Maneja los errores cuando se intenta realizar
    // una transición de estado no permitida.
    @ExceptionHandler(TransicionEstadoNoPermitidaException.class)
    public ResponseEntity<Map<String, String>> manejarTransicionEstadoNoPermitida(
            TransicionEstadoNoPermitidaException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "mensaje",
                        exception.getMessage()));
    }

    // Maneja los errores cuando un docente tiene
    // responsabilidades activas incompatibles.
    @ExceptionHandler(ResponsabilidadActivaException.class)
    public ResponseEntity<Map<String, String>> manejarResponsabilidadActiva(
            ResponsabilidadActivaException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "mensaje",
                        exception.getMessage()));
    }

    // Maneja los errores cuando la fecha de vigencia no es válida.
    @ExceptionHandler(FechaVigenciaInvalidaException.class)
    public ResponseEntity<Map<String, String>> manejarFechaVigenciaInvalida(
            FechaVigenciaInvalidaException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "mensaje",
                        exception.getMessage()));
    }

    // Maneja los conflictos de programación de horario
    // (docente ocupado, aula ocupada, grupo ocupado).
    @ExceptionHandler(ConflictoHorarioException.class)
    public ResponseEntity<Map<String, String>> manejarConflictoHorario(
            ConflictoHorarioException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "codigo",
                        exception.getCodigo(),
                        "mensaje",
                        exception.getMessage()));
    }

    // Maneja los errores cuando una relación académica
    // entre entidades no es válida.
    @ExceptionHandler(RelacionAcademicaInvalidaException.class)
    public ResponseEntity<Map<String, String>> manejarRelacionAcademicaInvalida(
            RelacionAcademicaInvalidaException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "mensaje",
                        exception.getMessage()));
    }

    // Maneja operaciones de Aula que entran en conflicto
    // con sus reglas de actualización, historial o eliminación.
    @ExceptionHandler(CambioAulaInvalidoException.class)
    public ResponseEntity<Map<String, String>> manejarCambioAulaInvalido(
            CambioAulaInvalidoException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "mensaje",
                        exception.getMessage()));
    }

    // El informe publicado y otras reglas de estado lanzan este error
    // con un mensaje listo para mostrar.
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> manejarEstadoInvalido(
            IllegalStateException exception) {

        String mensaje = exception.getMessage();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "mensaje",
                        mensaje == null || mensaje.isBlank()
                                ? "La operación no es válida en el estado actual."
                                : mensaje));
    }

    // Maneja validaciones funcionales inválidas
    // detectadas por los servicios.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarArgumentoInvalido(
            IllegalArgumentException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "mensaje",
                        exception.getMessage()));
    }

    // Maneja correo o contraseña incorrectos en el login.
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Map<String, String>> manejarCredencialesInvalidas(
            CredencialesInvalidasException exception) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "mensaje",
                        exception.getMessage()));
    }

    // Maneja el intento de acceso de una cuenta deshabilitada.
    @ExceptionHandler(CuentaInactivaException.class)
    public ResponseEntity<Map<String, String>> manejarCuentaInactiva(
            CuentaInactivaException exception) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "mensaje",
                        exception.getMessage()));
    }

    // H-10: entrega mensajes claros cuando un DTO
    // incumple las restricciones de validación.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacionCampos(
            MethodArgumentNotValidException exception) {

        List<String> errores = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> {
                    String detalle = error.getDefaultMessage() == null
                            ? "Valor inválido"
                            : error.getDefaultMessage();

                    return error.getField() + ": " + detalle;
                })
                .distinct()
                .sorted()
                .toList();

        String mensaje = errores.isEmpty()
                ? "Los datos enviados no son válidos"
                : String.join("; ", errores);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "mensaje", mensaje,
                        "errores", errores));
    }

    // H-10: maneja JSON mal formado o tipos incompatibles
    // sin exponer detalles internos de Spring/Jackson.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> manejarJsonInvalido(
            HttpMessageNotReadableException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "mensaje",
                        "El cuerpo de la solicitud no es válido. "
                                + "Verifique el formato JSON "
                                + "y los tipos de datos enviados."));
    }
}
