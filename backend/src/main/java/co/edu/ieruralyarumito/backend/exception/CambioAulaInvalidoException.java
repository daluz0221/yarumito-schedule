package co.edu.ieruralyarumito.backend.exception;

// Excepción utilizada cuando un cambio solicitado sobre un aula
// no cumple las reglas definidas para su actualización o versionamiento.
public class CambioAulaInvalidoException extends RuntimeException {

    public CambioAulaInvalidoException(String mensaje) {
        super(mensaje);
    }
}
