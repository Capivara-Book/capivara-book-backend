package br.com.capivarabook.capivara_book.repository;

import br.com.capivarabook.capivara_book.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByClienteId(Long clienteId);

    List<Reserva> findByStatus(StatusReserva status);

    List<Reserva> findByStatusIn(List<StatusReserva> statuses);

    List<Reserva> findByClienteIdAndStatusIn(Long clienteId, List<StatusReserva> statuses);

    @Query("""
        SELECT COUNT(reserva) > 0 FROM Reserva reserva
        WHERE reserva.cliente.id = :clienteId
            AND reserva.livro.id   = :livroId
            AND reserva.status = 'PENDENTE'
    """)
    boolean existsReservaAtiva(@Param("clienteId") Long clienteId,
                               @Param("livroId")   Long livroId);

    List<Reserva> findByLivroIdAndStatus(Long livroId, StatusReserva status);
}
