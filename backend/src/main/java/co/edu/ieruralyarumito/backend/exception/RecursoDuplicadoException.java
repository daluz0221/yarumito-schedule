package co.edu.ieruralyarumito.backend.exception;

// Excepción utilizada cuando se intenta registrar un dato que debe ser único.
public class RecursoDuplicadoException extends RuntimeException {

    // Permite indicar qué dato ya existe.
    public RecursoDuplicadoException(String mensaje) {
        super(mensaje); // Envía el mensaje a RuntimeException
    }
}
