package br.com.capivarabook.capivara_book.utils;

import br.com.capivarabook.capivara_book.entity.*;
import java.time.LocalDate;
import java.util.List;

public class EmprestimoUtils {
    public static void sincronizarStatusAtraso(List<Emprestimo> emprestimos) {
        LocalDate hoje = LocalDate.now();
        emprestimos.stream()
                .filter(e -> (e.getStatus() == StatusEmprestimo.ATIVO
                        || e.getStatus() == StatusEmprestimo.RENOVADO)
                        && e.getDataPrevistaDevolucao().isBefore(hoje))
                .forEach(e -> e.setStatus(StatusEmprestimo.ATRASADO));
    }
}
