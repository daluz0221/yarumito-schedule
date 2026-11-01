package co.edu.ieruralyarumito.backend.exception;

// Excepción utilizada cuando se solicita un cambio de estado no permitido.
public class TransicionEstadoNoPermitidaException extends RuntimeException {

    // Permite indicar por qué la transición de estado fue rechazada.
    public TransicionEstadoNoPermitidaException(String mensaje) {
        super(mensaje); // Envía el mensaje a RuntimeException
    }
}
