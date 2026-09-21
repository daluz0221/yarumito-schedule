package co.edu.ieruralyarumito.backend.exception;

// Excepción utilizada cuando una relación académica entre entidades no es válida.
public class RelacionAcademicaInvalidaException extends RuntimeException {

    // Permite indicar por qué la relación académica fue rechazada.
    public RelacionAcademicaInvalidaException(String mensaje) {
        super(mensaje);
    }
}