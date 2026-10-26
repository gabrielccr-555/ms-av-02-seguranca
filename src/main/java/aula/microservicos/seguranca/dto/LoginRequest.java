package aula.microservicos.seguranca.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Login", description = "Credenciais do usuario. Tambem usado no cadastro (POST /)")
public record LoginRequest(
        @Schema(description = "Nome do usuario", example = "joao", requiredMode = Schema.RequiredMode.REQUIRED)
        String username,
        @Schema(description = "Senha do usuario (max. 10 caracteres)", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
        String senha) {
}
