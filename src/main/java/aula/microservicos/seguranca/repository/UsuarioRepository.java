package aula.microservicos.seguranca.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import aula.microservicos.seguranca.entity.UsuarioEntity;

@Repository
public interface UsuarioRepository extends CrudRepository<UsuarioEntity, String> {
}
