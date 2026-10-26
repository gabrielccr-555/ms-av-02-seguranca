package aula.microservicos.seguranca.config;

import static org.springframework.security.config.Customizer.withDefaults;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests((requests) -> requests
                // end-points publicos
                .requestMatchers(HttpMethod.POST, "/login").permitAll()
                .requestMatchers(HttpMethod.PUT, "/trocarsenha/**").permitAll()
                // end-points protegidos por role (o prefixo ROLE_ e incluido automaticamente)
                .requestMatchers(HttpMethod.POST, "/").hasRole("MANUTENCAO")
                .requestMatchers(HttpMethod.GET, "/").hasRole("MANUTENCAO")
                .requestMatchers(HttpMethod.PUT, "/unlock/**").hasRole("ADMIN")
                // documentacao, observabilidade e console do banco
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers(PathRequest.toH2Console()).permitAll()
                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated());

        // autenticacao via header Authorization: Basic (sem formulario padrao,
        // pois o POST /login e um end-point proprio da aplicacao)
        http.formLogin(config -> config.disable());
        http.httpBasic(withDefaults());

        http.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.csrf(csrf -> csrf.disable());

        // necessario para o console do H2 (usa frames)
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    /**
     * Operadores (admin / manutencao) e suas roles ficam nas tabelas
     * users e authorities, gerenciadas pelo JdbcUserDetailsManager.
     */
    @Bean
    public JdbcUserDetailsManager userDetailsService(DataSource datasource) {
        return new JdbcUserDetailsManager(datasource);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
