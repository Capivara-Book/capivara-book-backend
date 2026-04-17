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

    // Como Funcionario HERDA de Usuario, o ID do usuario É o ID do funcionario.
    // O Spring busca pelo campo 'id' que está na classe pai (Usuario).
    boolean existsByIdAndStatus(Long id, StatusUsuario status);

    // Aqui usamos apenas 'findById', que já retorna Optional<Funcionario>
    // O Spring entende que o ID buscado é o identificador da entidade.
    // Se quiser manter o nome específico:
    Optional<Funcionario> findById(Long id);

    // Ajuste no método default para usar o campo correto 'id'
    default boolean existsByUsuarioId(Long id) {
        return existsByIdAndStatus(id, StatusUsuario.ATIVO);
    }
}
