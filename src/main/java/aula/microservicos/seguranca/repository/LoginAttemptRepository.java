package aula.microservicos.seguranca.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import aula.microservicos.seguranca.entity.LoginAttemptEntity;

@Repository
public interface LoginAttemptRepository extends CrudRepository<LoginAttemptEntity, String> {

    // SELECT COUNT(*) FROM login_attempts WHERE locked = TRUE
    long countByLockedTrue();
}
