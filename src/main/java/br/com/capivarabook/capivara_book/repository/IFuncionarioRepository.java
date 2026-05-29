package br.com.capivarabook.capivara_book.repository;

import br.com.capivarabook.capivara_book.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IFuncionarioRepository extends JpaRepository<Funcionario, Long> {

    boolean existsByMatricula(String matricula);

    boolean existsByIdAndStatus(Long id, StatusUsuario status);

    Optional<Funcionario> findById(Long id);

    default boolean existsByUsuarioId(Long id) {
        return existsByIdAndStatus(id, StatusUsuario.ATIVO);
    }

    List<Funcionario> findByStatus(StatusUsuario status);
}
