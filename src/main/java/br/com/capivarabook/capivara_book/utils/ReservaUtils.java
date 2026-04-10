package br.com.capivarabook.capivara_book.utils;

import br.com.capivarabook.capivara_book.entity.*;
import java.util.List;

public class ReservaUtils {
    public static void expirarReservasVencidas(List<Reserva> reservas) {
        reservas.stream()
                .filter(Reserva::isExpiracao)
                .forEach(r -> r.setStatus(StatusReserva.EXPIRADO));
    }
}
