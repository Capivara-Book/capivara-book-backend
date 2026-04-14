package br.com.capivarabook.capivara_book.service;

import br.com.capivarabook.capivara_book.dto.request.LivroRequestDTO;
import br.com.capivarabook.capivara_book.dto.response.LivroResponseDTO;
import br.com.capivarabook.capivara_book.entity.*;
import br.com.capivarabook.capivara_book.exception.*;
import br.com.capivarabook.capivara_book.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import lombok.*;

@Service
@RequiredArgsConstructor
public class LivroService {
    private final ILivroRepository livroRepository;
//    public LivroService(ILivroRepository r) { this.livroRepository = r; }

    @Transactional
    public LivroResponseDTO adicionarLivro(LivroRequestDTO req) {
        if (livroRepository.existsByIsbn(req.getIsbn()))
            throw new DuplicateResourceException("ISBN já cadastrado: " + req.getIsbn());
        Livro l = new Livro();
        l.setTitulo(req.getTitulo()); l.setAutor(req.getAutor());
        l.setEditora(req.getEditora()); l.setIsbn(req.getIsbn());
        l.setAnoPublicacao(req.getAnoPublicacao()); l.setGenero(req.getGenero());
        l.setExemplares(req.getExemplares());
        l.adicionarLivro();
        return LivroResponseDTO.from(livroRepository.save(l));
    }

    @Transactional(readOnly = true)
    public List<LivroResponseDTO> buscarLivro(String titulo, String autor,
                                           String editora, Genero genero, Integer ano) {
        return livroRepository.buscarLivro(titulo, autor, editora, genero, ano)
                .stream().map(LivroResponseDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public List<LivroResponseDTO> buscarTodos(String titulo, String autor,
                                           String editora, Genero genero, Integer ano) {
        return livroRepository.buscarTodos(titulo, autor, editora, genero, ano)
                .stream().map(LivroResponseDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public LivroResponseDTO buscarPorId(Long id) { return LivroResponseDTO.from(buscarLivro(id)); }

    @Transactional
    public LivroResponseDTO alterarStatus(Long id, StatusLivro novoStatus) {
        Livro l = buscarLivro(id);
        if (novoStatus == StatusLivro.INDISPONIVEL
                && l.getExemplaresDisponiveis() < l.getExemplares())
            throw new BusinessException("Livro com exemplares emprestados não pode ser inativado (RN06).");
        l.setStatus(novoStatus);
        return LivroResponseDTO.from(livroRepository.save(l));
    }

    @Transactional
    public LivroResponseDTO editar(Long id, LivroRequestDTO req) {
        Livro l = buscarLivro(id);
        if (!l.getIsbn().equals(req.getIsbn()) && livroRepository.existsByIsbn(req.getIsbn()))
            throw new DuplicateResourceException("ISBN já cadastrado: " + req.getIsbn());
        l.setTitulo(req.getTitulo()); l.setAutor(req.getAutor());
        l.setEditora(req.getEditora()); l.setIsbn(req.getIsbn());
        l.setAnoPublicacao(req.getAnoPublicacao()); l.setGenero(req.getGenero());
        return LivroResponseDTO.from(livroRepository.save(l));
    }

    public Livro buscarLivro(Long id) {
        return livroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livro", id));
    }
}
