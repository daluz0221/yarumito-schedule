package co.edu.ieruralyarumito.backend.exception;

// Se lanza cuando el correo no existe o la contraseña no coincide.
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
