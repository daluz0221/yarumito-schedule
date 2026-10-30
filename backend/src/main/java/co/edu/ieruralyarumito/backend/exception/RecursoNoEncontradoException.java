package co.edu.ieruralyarumito.backend.exception;

// Excepción utilizada cuando un recurso solicitado no existe.
public class RecursoNoEncontradoException extends RuntimeException {

    // Permite indicar qué recurso no fue encontrado.
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje); // Envía el mensaje a RuntimeException
    }
}
