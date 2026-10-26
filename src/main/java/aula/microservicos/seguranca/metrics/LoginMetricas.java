package aula.microservicos.seguranca.metrics;

import org.springframework.stereotype.Component;

import aula.microservicos.seguranca.repository.LoginAttemptRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Indicadores expostos em /actuator/metrics e /actuator/prometheus:
 * - login.validos      (counter) -> login_validos_total
 * - login.invalidos    (counter) -> login_invalidos_total
 * - usuarios.bloqueados (gauge)  -> usuarios_bloqueados (lido da tabela login_attempts)
 */
@Component
public class LoginMetricas {

    private final Counter loginsValidos;
    private final Counter loginsInvalidos;

    public LoginMetricas(MeterRegistry registry, LoginAttemptRepository attemptRepo) {

        this.loginsValidos = Counter.builder("login.validos")
                .description("Quantidade de logins validos")
                .register(registry);

        this.loginsInvalidos = Counter.builder("login.invalidos")
                .description("Quantidade de logins invalidos")
                .register(registry);

        Gauge.builder("usuarios.bloqueados", attemptRepo, LoginAttemptRepository::countByLockedTrue)
                .description("Total de usuarios bloqueados")
                .register(registry);
    }

    public void registrarLoginValido() {
        loginsValidos.increment();
    }

    public void registrarLoginInvalido() {
        loginsInvalidos.increment();
    }
}
