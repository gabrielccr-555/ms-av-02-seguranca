package aula.microservicos.seguranca.exception;

/**
 * Excecoes de negocio tratadas pelo GlobalExceptionHandler.
 */
public final class SegurancaExceptions {

    private SegurancaExceptions() {
    }

    /** 401 - usuario/senha invalidos ou usuario bloqueado */
    public static class CredenciaisInvalidasException extends RuntimeException {
        public CredenciaisInvalidasException(String msg) {
            super(msg);
        }
    }

    /** 404 - usuario nao localizado */
    public static class UsuarioNaoLocalizadoException extends RuntimeException {
        public UsuarioNaoLocalizadoException(String username) {
            super("Usuario nao localizado: " + username);
        }
    }

    /** 409 - usuario ja cadastrado */
    public static class UsuarioJaExisteException extends RuntimeException {
        public UsuarioJaExisteException(String username) {
            super("Usuario ja cadastrado: " + username);
        }
    }

    /** 400 - dados de entrada invalidos */
    public static class DadosInvalidosException extends RuntimeException {
        public DadosInvalidosException(String msg) {
            super(msg);
        }
    }
}
