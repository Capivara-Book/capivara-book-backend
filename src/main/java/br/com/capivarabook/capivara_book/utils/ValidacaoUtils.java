package br.com.capivarabook.capivara_book.utils;

import br.com.capivarabook.capivara_book.entity.*;

public class ValidacaoUtils {
    public static boolean isbnValido(String isbn) {
        return isbn != null && isbn.matches("\\d{13}");
    }

    public static boolean cpfValido(String cpf) {
        return cpf != null && cpf.matches("\\d{11}");
    }
}
