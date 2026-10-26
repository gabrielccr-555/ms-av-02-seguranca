package aula.microservicos.seguranca.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import aula.microservicos.seguranca.dto.MensagemResponse;
import aula.microservicos.seguranca.exception.SegurancaExceptions.CredenciaisInvalidasException;
import aula.microservicos.seguranca.exception.SegurancaExceptions.DadosInvalidosException;
import aula.microservicos.seguranca.exception.SegurancaExceptions.UsuarioJaExisteException;
import aula.microservicos.seguranca.exception.SegurancaExceptions.UsuarioNaoLocalizadoException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<MensagemResponse> credenciaisInvalidas(CredenciaisInvalidasException e) {
        return new ResponseEntity<>(new MensagemResponse(e.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UsuarioNaoLocalizadoException.class)
    public ResponseEntity<MensagemResponse> naoLocalizado(UsuarioNaoLocalizadoException e) {
        return new ResponseEntity<>(new MensagemResponse(e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UsuarioJaExisteException.class)
    public ResponseEntity<MensagemResponse> jaExiste(UsuarioJaExisteException e) {
        return new ResponseEntity<>(new MensagemResponse(e.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DadosInvalidosException.class)
    public ResponseEntity<MensagemResponse> dadosInvalidos(DadosInvalidosException e) {
        return new ResponseEntity<>(new MensagemResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<MensagemResponse> jsonInvalido(HttpMessageNotReadableException e) {
        return new ResponseEntity<>(new MensagemResponse("JSON invalido no corpo da requisicao"),
                HttpStatus.BAD_REQUEST);
    }
}
