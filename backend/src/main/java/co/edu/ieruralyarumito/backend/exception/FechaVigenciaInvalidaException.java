package co.edu.ieruralyarumito.backend.exception;

// Excepción utilizada cuando la fecha de vigencia no es válida.
public class FechaVigenciaInvalidaException extends RuntimeException {

    // Permite indicar por qué la fecha de vigencia fue rechazada.
    public FechaVigenciaInvalidaException(String mensaje) {
        super(mensaje);
    }
}