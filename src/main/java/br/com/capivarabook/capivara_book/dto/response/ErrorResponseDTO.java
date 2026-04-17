package br.com.capivarabook.capivara_book.dto.response;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.http.HttpStatus;
import java.util.Map;

public record ErrorResponseDTO(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> fieldErrors
){
    public static ErrorResponseDTO of(HttpStatus hs, String msg) {
        return new ErrorResponseDTO(
                LocalDateTime.now(),
                hs.value(),
                hs.getReasonPhrase(),
                msg,
                null
        );
    }

    public static ErrorResponseDTO withFields(HttpStatus hs, String msg, Map<String, String> f) {
        return new ErrorResponseDTO(
                LocalDateTime.now(),
                hs.value(),
                hs.getReasonPhrase(),
                msg,
                f
        );
    }
}
