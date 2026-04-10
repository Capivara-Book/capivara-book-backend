package br.com.capivarabook.capivara_book.repository;

import br.com.capivarabook.capivara_book.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ILivroRepository extends JpaRepository<Livro, Long> {

    Optional<Livro> findByIsbn(String isbn);
    boolean existsByIsbn(String isbn);

    // buscarLivro() — busca parcial case-insensitive (seção 5.1)
    @Query("""
        SELECT livro FROM Livro livro
        WHERE (:titulo  IS NULL OR LOWER(livro.titulo)  LIKE LOWER(CONCAT('%', :titulo,  '%')))
            AND (:autor   IS NULL OR LOWER(livro.autor)   LIKE LOWER(CONCAT('%', :autor,   '%')))
            AND (:editora IS NULL OR LOWER(livro.editora) LIKE LOWER(CONCAT('%', :editora, '%')))
            AND (:genero  IS NULL OR livro.genero = :genero)
            AND (:ano     IS NULL OR livro.anoPublicacao = :ano)
            AND livro.status = 'DISPONIVEL'
    """)
    List<Livro> buscarLivro(
        @Param("titulo")  String titulo,
        @Param("autor")   String autor,
        @Param("editora") String editora,
        @Param("genero")  Genero genero,
        @Param("ano")     Integer ano
    );

    // Todos os livros (catálogo Admin — inclui INDISPONIVEL)
    @Query("""
        SELECT livro FROM Livro livro
        WHERE (:titulo  IS NULL OR LOWER(livro.titulo)  LIKE LOWER(CONCAT('%', :titulo,  '%')))
            AND (:autor   IS NULL OR LOWER(livro.autor)   LIKE LOWER(CONCAT('%', :autor,   '%')))
            AND (:editora IS NULL OR LOWER(livro.editora) LIKE LOWER(CONCAT('%', :editora, '%')))
            AND (:genero  IS NULL OR livro.genero = :genero)
            AND (:ano     IS NULL OR livro.anoPublicacao = :ano)
    """)
    List<Livro> buscarTodos(
        @Param("titulo")  String titulo,
        @Param("autor")   String autor,
        @Param("editora") String editora,
        @Param("genero")  Genero genero,
        @Param("ano")     Integer ano
    );
}
