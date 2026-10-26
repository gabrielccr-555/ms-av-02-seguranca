package aula.microservicos.seguranca.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import aula.microservicos.seguranca.dto.LoginRequest;
import aula.microservicos.seguranca.dto.MensagemResponse;
import aula.microservicos.seguranca.dto.TrocaSenhaRequest;
import aula.microservicos.seguranca.dto.UsuarioResponse;
import aula.microservicos.seguranca.service.UsuarioService;
import aula.microservicos.seguranca.service.UsuarioService.ResultadoLogin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "usuarios", description = "Login e gerenciamento de usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    // ------------------------------------------------------------ POST /login

    @PostMapping("/login")
    @Operation(summary = "Efetua login", description = "Valida usuario e senha na tabela usuarios. "
            + "Na segunda falha consecutiva o usuario e bloqueado (tabela login_attempts). Acesso publico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario e senha validos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuario e senha invalidos ou usuario bloqueado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MensagemResponse.class))) })
    public ResponseEntity<MensagemResponse> login(@RequestBody LoginRequest req) {

        ResultadoLogin resultado = service.login(req.username(), req.senha());

        return switch (resultado) {
            case VALIDO -> new ResponseEntity<>(new MensagemResponse("Login efetuado com sucesso"), HttpStatus.OK);
            case BLOQUEADO -> new ResponseEntity<>(new MensagemResponse("Usuario bloqueado"), HttpStatus.UNAUTHORIZED);
            default -> new ResponseEntity<>(new MensagemResponse("Usuario ou senha invalidos"), HttpStatus.UNAUTHORIZED);
        };
    }

    // ------------------------------------------------ PUT /trocarsenha/{userid}

    @PutMapping("/trocarsenha/{userid}")
    @Operation(summary = "Troca a senha", description = "Confere a senha atual com a base de dados e grava a nova. Acesso publico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha alterada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Nova senha invalida (vazia ou com mais de 10 caracteres)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Senha atual invalida ou usuario bloqueado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario nao localizado", content = @Content) })
    public ResponseEntity<MensagemResponse> trocarSenha(
            @Parameter(description = "username do usuario", example = "joao") @PathVariable String userid,
            @RequestBody TrocaSenhaRequest req) {

        service.trocarSenha(userid, req.atual(), req.nova());
        return new ResponseEntity<>(new MensagemResponse("Senha alterada com sucesso"), HttpStatus.OK);
    }

    // ------------------------------------------------------------------ POST /

    @PostMapping("/")
    @Operation(summary = "Cadastra usuario", description = "Cadastra um novo usuario na tabela usuarios. Requer ROLE_MANUTENCAO.",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario cadastrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem a role ROLE_MANUTENCAO", content = @Content),
            @ApiResponse(responseCode = "409", description = "Usuario ja cadastrado", content = @Content) })
    public ResponseEntity<MensagemResponse> cadastrar(@RequestBody LoginRequest req) {

        service.cadastrar(req.username(), req.senha());
        return new ResponseEntity<>(new MensagemResponse("Usuario cadastrado: " + req.username()), HttpStatus.CREATED);
    }

    // --------------------------------------------------- PUT /unlock/{userid}

    @PutMapping("/unlock/{userid}")
    @Operation(summary = "Desbloqueia usuario", description = "Remove o bloqueio e zera as tentativas invalidas. Requer ROLE_ADMIN.",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario desbloqueado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem a role ROLE_ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario nao localizado", content = @Content) })
    public ResponseEntity<MensagemResponse> desbloquear(
            @Parameter(description = "username do usuario", example = "joao") @PathVariable String userid) {

        service.desbloquear(userid);
        return new ResponseEntity<>(new MensagemResponse("Usuario desbloqueado: " + userid), HttpStatus.OK);
    }

    // ------------------------------------------------------------------- GET /

    @GetMapping("/")
    @Operation(summary = "Lista usuarios", description = "Retorna todos os usuarios cadastrados. Requer ROLE_MANUTENCAO.",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuarios",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UsuarioResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem a role ROLE_MANUTENCAO", content = @Content) })
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return new ResponseEntity<>(service.listar(), HttpStatus.OK);
    }
}
