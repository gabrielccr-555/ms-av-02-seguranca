package aula.microservicos.seguranca.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Controle de Login", version = "1.0",
        description = "MS-AV-02 - Seguranca e Observabilidade: login, troca de senha, cadastro, "
                + "listagem e desbloqueio de usuarios"))
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic",
        description = "Operadores cadastrados no JdbcUserDetailsManager: admin/admin (ROLE_ADMIN) "
                + "e manutencao/manutencao (ROLE_MANUTENCAO)")
public class OpenApiConfig {
}
