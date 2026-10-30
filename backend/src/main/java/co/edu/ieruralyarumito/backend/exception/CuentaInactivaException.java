package co.edu.ieruralyarumito.backend.exception;

// Se lanza cuando el usuario existe pero no está habilitado para iniciar sesión.
public class CuentaInactivaException extends RuntimeException {

    public CuentaInactivaException(String mensaje) {
        super(mensaje);
    }
}
