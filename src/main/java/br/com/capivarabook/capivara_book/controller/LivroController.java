package br.com.capivarabook.capivara_book.controller;

import br.com.capivarabook.capivara_book.dto.request.LivroRequestDTO;
import br.com.capivarabook.capivara_book.dto.response.LivroResponseDTO;
import br.com.capivarabook.capivara_book.entity.Genero;
import br.com.capivarabook.capivara_book.entity.StatusLivro;
import br.com.capivarabook.capivara_book.service.LivroService;
import jakarta.validation.Valid;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/livros")
@RequiredArgsConstructor
public class LivroController {
    private final LivroService livroService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LivroResponseDTO adicionarLivro(@Valid @RequestBody LivroRequestDTO req) {
        return livroService.adicionarLivro(req);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<LivroResponseDTO> buscarLivro(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String autor,
            @RequestParam(required = false) String editora,
            @RequestParam(required = false) Genero genero,
            @RequestParam(required = false) Integer ano) {
        return livroService.buscarLivro(titulo, autor, editora, genero, ano);
    }

    @GetMapping("/admin")
    @ResponseStatus(HttpStatus.OK)
    public List<LivroResponseDTO> buscarTodos(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String autor,
            @RequestParam(required = false) String editora,
            @RequestParam(required = false) Genero genero,
            @RequestParam(required = false) Integer ano) {
        return livroService.buscarTodos(titulo, autor, editora, genero, ano);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public LivroResponseDTO buscarPorId(@PathVariable Long id) {
        return livroService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public LivroResponseDTO editar(
            @PathVariable Long id,
            @Valid @RequestBody LivroRequestDTO req) {
        return livroService.editar(id, req);
    }

    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public LivroResponseDTO alterarStatus(
            @PathVariable Long id,
            @RequestParam StatusLivro status) {
        return livroService.alterarStatus(id, status);
    }
}
