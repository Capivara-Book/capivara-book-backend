package br.com.capivarabook.capivara_book.utils;

import br.com.capivarabook.capivara_book.entity.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static String formatarBR(LocalDate date) {
        return date == null ? "—" : date.format(BR);
    }

    public static LocalDate calcularPrazo(int dias) {
        return LocalDate.now().plusDays(dias);
    }
}
