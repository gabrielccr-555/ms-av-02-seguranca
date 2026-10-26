package aula.microservicos.seguranca.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TrocaSenha", description = "Senha atual e nova senha")
public record TrocaSenhaRequest(
        @Schema(description = "Senha atual", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
        String atual,
        @Schema(description = "Nova senha (max. 10 caracteres)", example = "nova123", requiredMode = Schema.RequiredMode.REQUIRED)
        String nova) {
}
