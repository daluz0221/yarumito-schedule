package co.edu.ieruralyarumito.backend.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus; // Códigos de estado HTTP
import org.springframework.http.ResponseEntity; // Permite construir la respuesta HTTP
import org.springframework.web.bind.annotation.ExceptionHandler; // Asocia una excepción con un método
import java.util.Map; // Permite devolver una respuesta sencilla en formato clave-valor


// Centraliza el manejo de excepciones generadas por la API.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Maneja los errores cuando un recurso solicitado no existe.
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> manejarRecursoNoEncontrado(
            RecursoNoEncontradoException exception) {

        // Devuelve el mensaje de error con código HTTP 404.
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("mensaje", exception.getMessage()));
    }

    // Maneja los errores cuando se intenta registrar un recurso duplicado.
    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<Map<String, String>> manejarRecursoDuplicado(
            RecursoDuplicadoException exception) {

        // Devuelve el mensaje de error con código HTTP 409.
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("mensaje", exception.getMessage()));
    }

    // Maneja los errores cuando se intenta realizar una transición de estado no permitida.
    @ExceptionHandler(TransicionEstadoNoPermitidaException.class)
    public ResponseEntity<Map<String, String>> manejarTransicionEstadoNoPermitida(
            TransicionEstadoNoPermitidaException exception) {

        // Devuelve el mensaje de error con código HTTP 409.
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("mensaje", exception.getMessage()));
    }

    // Maneja los errores cuando un docente tiene responsabilidades activas incompatibles.
    @ExceptionHandler(ResponsabilidadActivaException.class)
    public ResponseEntity<Map<String, String>> manejarResponsabilidadActiva(
            ResponsabilidadActivaException exception) {

        // Devuelve el mensaje de error con código HTTP 409.
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("mensaje", exception.getMessage()));
    }

    // Maneja los errores cuando la fecha de vigencia no es válida.
    @ExceptionHandler(FechaVigenciaInvalidaException.class)
    public ResponseEntity<Map<String, String>> manejarFechaVigenciaInvalida(
            FechaVigenciaInvalidaException exception) {

        // Devuelve el mensaje de error con código HTTP 400.
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", exception.getMessage()));
    }
}

