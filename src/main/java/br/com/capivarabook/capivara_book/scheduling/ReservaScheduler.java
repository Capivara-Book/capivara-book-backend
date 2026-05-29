package br.com.capivarabook.capivara_book.scheduling;

import br.com.capivarabook.capivara_book.service.ReservaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservaScheduler {

    private final ReservaService reservaService;

    // Executa todos os dias às 02h00 (horário do servidor).
    // Ajuste o cron conforme necessidade: "0 0 2 * * *" = seg a dom, 02:00.
    @Scheduled(cron = "0 0 2 * * *")
    public void expirarReservasVencidas() {
        log.info("[ReservaScheduler] Iniciando expiração de reservas vencidas...");
        try {
            reservaService.expirarReservasVencidas();
            log.info("[ReservaScheduler] Expiração concluída com sucesso.");
        } catch (Exception ex) {
            log.error("[ReservaScheduler] Erro ao expirar reservas: {}", ex.getMessage(), ex);
        }
    }
}
