package aula.microservicos.seguranca.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import aula.microservicos.seguranca.dto.UsuarioResponse;
import aula.microservicos.seguranca.entity.LoginAttemptEntity;
import aula.microservicos.seguranca.entity.UsuarioEntity;
import aula.microservicos.seguranca.exception.SegurancaExceptions.CredenciaisInvalidasException;
import aula.microservicos.seguranca.exception.SegurancaExceptions.DadosInvalidosException;
import aula.microservicos.seguranca.exception.SegurancaExceptions.UsuarioJaExisteException;
import aula.microservicos.seguranca.exception.SegurancaExceptions.UsuarioNaoLocalizadoException;
import aula.microservicos.seguranca.metrics.LoginMetricas;
import aula.microservicos.seguranca.repository.LoginAttemptRepository;
import aula.microservicos.seguranca.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    /** "Caso o usuario falhe mais de uma vez no login ele deve ser bloqueado" */
    public static final int MAX_FALHAS_PERMITIDAS = 1;

    /** Tamanho da coluna senha na tabela usuarios: VARCHAR(10) */
    private static final int TAMANHO_MAX_SENHA = 10;

    public enum ResultadoLogin {
        VALIDO, INVALIDO, BLOQUEADO
    }

    private final UsuarioRepository usuarioRepo;
    private final LoginAttemptRepository attemptRepo;
    private final LoginMetricas metricas;

    public UsuarioService(UsuarioRepository usuarioRepo, LoginAttemptRepository attemptRepo,
            LoginMetricas metricas) {
        this.usuarioRepo = usuarioRepo;
        this.attemptRepo = attemptRepo;
        this.metricas = metricas;
    }

    // ------------------------------------------------------------------ login

    @Transactional
    public ResultadoLogin login(String username, String senha) {

        Optional<UsuarioEntity> usuario = usuarioRepo.findById(nvl(username));

        // usuario inexistente: login invalido (nao ha usuario a ser bloqueado)
        if (usuario.isEmpty()) {
            logger.info("Login invalido - usuario inexistente: {}", username);
            metricas.registrarLoginInvalido();
            return ResultadoLogin.INVALIDO;
        }

        LoginAttemptEntity attempt = obterAttempt(username);

        // usuario bloqueado: nao autentica mesmo com a senha correta
        if (attempt.isLocked()) {
            logger.info("Login invalido - usuario bloqueado: {}", username);
            metricas.registrarLoginInvalido();
            return ResultadoLogin.BLOQUEADO;
        }

        LocalDateTime agora = LocalDateTime.now();

        if (usuario.get().getSenha().equals(senha)) {
            attempt.setFailedAttempts(0);
            attempt.setUpdatedAt(agora);
            attemptRepo.save(attempt);
            metricas.registrarLoginValido();
            logger.info("Login valido: {}", username);
            return ResultadoLogin.VALIDO;
        }

        // senha incorreta
        attempt.setFailedAttempts(attempt.getFailedAttempts() + 1);
        attempt.setLastFailedAt(agora);
        attempt.setUpdatedAt(agora);

        if (attempt.getFailedAttempts() > MAX_FALHAS_PERMITIDAS) {
            attempt.setLocked(true);
            attempt.setLockedAt(agora);
            logger.info("Usuario bloqueado apos {} falhas: {}", attempt.getFailedAttempts(), username);
        }

        attemptRepo.save(attempt);
        metricas.registrarLoginInvalido();
        logger.info("Login invalido - senha incorreta: {} ({} falha(s))", username, attempt.getFailedAttempts());

        return attempt.isLocked() ? ResultadoLogin.BLOQUEADO : ResultadoLogin.INVALIDO;
    }

    // ---------------------------------------------------------- troca de senha

    @Transactional
    public void trocarSenha(String userid, String atual, String nova) {

        UsuarioEntity usuario = usuarioRepo.findById(nvl(userid))
                .orElseThrow(() -> new UsuarioNaoLocalizadoException(userid));

        if (obterAttempt(userid).isLocked()) {
            throw new CredenciaisInvalidasException("Usuario bloqueado: " + userid);
        }

        if (!usuario.getSenha().equals(atual)) {
            throw new CredenciaisInvalidasException("Senha atual invalida");
        }

        validarSenha(nova);

        usuario.setSenha(nova);
        usuarioRepo.save(usuario);
        logger.info("Senha alterada: {}", userid);
    }

    // ---------------------------------------------------------------- cadastro

    @Transactional
    public void cadastrar(String username, String senha) {

        if (username == null || username.isBlank() || username.length() > 100) {
            throw new DadosInvalidosException("username deve ser informado (max. 100 caracteres)");
        }
        validarSenha(senha);

        if (usuarioRepo.existsById(username)) {
            throw new UsuarioJaExisteException(username);
        }

        usuarioRepo.save(new UsuarioEntity(username, senha));
        logger.info("Usuario cadastrado: {}", username);
    }

    // ------------------------------------------------------------ desbloqueio

    @Transactional
    public void desbloquear(String userid) {

        if (!usuarioRepo.existsById(nvl(userid))) {
            throw new UsuarioNaoLocalizadoException(userid);
        }

        LoginAttemptEntity attempt = obterAttempt(userid);
        attempt.setLocked(false);
        attempt.setFailedAttempts(0);
        attempt.setLockedAt(null);
        attempt.setUpdatedAt(LocalDateTime.now());
        attemptRepo.save(attempt);
        logger.info("Usuario desbloqueado: {}", userid);
    }

    // ---------------------------------------------------------------- listagem

    public List<UsuarioResponse> listar() {
        List<UsuarioResponse> lista = new ArrayList<>();
        for (UsuarioEntity u : usuarioRepo.findAll()) {
            LoginAttemptEntity attempt = attemptRepo.findById(u.getUsername())
                    .orElse(new LoginAttemptEntity(u.getUsername()));
            lista.add(new UsuarioResponse(u.getUsername(), attempt.isLocked(), attempt.getFailedAttempts()));
        }
        return lista;
    }

    // ---------------------------------------------------------------- auxiliares

    private LoginAttemptEntity obterAttempt(String username) {
        return attemptRepo.findById(username).orElse(new LoginAttemptEntity(username));
    }

    private void validarSenha(String senha) {
        if (senha == null || senha.isBlank() || senha.length() > TAMANHO_MAX_SENHA) {
            throw new DadosInvalidosException("senha deve ser informada (max. " + TAMANHO_MAX_SENHA + " caracteres)");
        }
    }

    private String nvl(String valor) {
        return valor == null ? "" : valor;
    }
}
