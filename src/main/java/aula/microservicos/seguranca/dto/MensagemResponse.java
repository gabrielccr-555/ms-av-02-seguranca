package aula.microservicos.seguranca.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Mensagem", description = "Mensagem de retorno da operacao")
public record MensagemResponse(
        @Schema(example = "Login efetuado com sucesso")
        String mensagem) {
}
