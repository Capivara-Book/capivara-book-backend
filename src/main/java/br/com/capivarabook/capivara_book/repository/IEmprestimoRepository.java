package br.com.capivarabook.capivara_book.repository;

import br.com.capivarabook.capivara_book.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IEmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    List<Emprestimo> findByClienteId(Long clienteId);

    List<Emprestimo> findByStatus(StatusEmprestimo status);

    // Verifica duplicata ativa (RN03)
    @Query("""
        SELECT COUNT(emprestimo) > 0 FROM Emprestimo emprestimo
        WHERE emprestimo.cliente.id = :clienteId
            AND emprestimo.livro.id   = :livroId
            AND emprestimo.status IN ('ATIVO', 'RENOVADO', 'ATRASADO')
    """)
    boolean existsDuplicataAtiva(@Param("clienteId") Long clienteId,
                                 @Param("livroId")   Long livroId);

    @Query("""
        SELECT emprestimo FROM Emprestimo emprestimo
        WHERE emprestimo.status IN ('ATIVO', 'ATRASADO', 'RENOVADO')
            AND emprestimo.dataPrevistaDevolucao < CURRENT_DATE
    """)
    List<Emprestimo> findEmAtraso();

    @Query("""
        SELECT COUNT(emprestimo) FROM Emprestimo emprestimo
        WHERE emprestimo.cliente.id = :clienteId
            AND emprestimo.status IN ('ATIVO', 'RENOVADO', 'ATRASADO')
    """)
    long countAtivosDoCliente(@Param("clienteId") Long clienteId);
}
