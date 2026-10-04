package co.edu.ieruralyarumito.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
}
