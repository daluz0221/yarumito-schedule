package co.edu.ieruralyarumito.backend.exception;

// Excepción utilizada cuando un docente tiene responsabilidades activas incompatibles.
public class ResponsabilidadActivaException extends RuntimeException {

    // Permite indicar qué responsabilidad impide realizar la operación.
    public ResponsabilidadActivaException(String mensaje) {
        super(mensaje); // Envía el mensaje a RuntimeException
    }
}

