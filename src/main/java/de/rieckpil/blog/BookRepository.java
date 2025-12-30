package de.rieckpil.blog;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

  /**
   * PostgreSQL-specific: Full text search on book titles with ranking. Uses PostgreSQL's
   * to_tsvector and to_tsquery for sophisticated text searching with ranking based on relevance.
   *
   * @param searchTerms the search terms (e.g. "adventure dragons fantasy")
   * @return list of books matching the search terms, ordered by relevance
   */
  @Query(
      value =
          """
    SELECT * FROM books
    WHERE to_tsvector('english', title) @@ plainto_tsquery('english', :searchTerms)
    ORDER BY ts_rank(to_tsvector('english', title), plainto_tsquery('english', :searchTerms)) DESC
    """,
      nativeQuery = true)
  List<Book> searchBooksByTitleWithRanking(@Param("searchTerms") String searchTerms);

  /**
   * PostgreSQL-specific: Fuzzy string matching using trigram similarity. Finds books with similar
   * titles even with typos or slight variations. Requires the pg_trgm extension.
   *
   * @param title the approximate title to search for
   * @param similarityThreshold the minimum similarity threshold (0.0-1.0)
   * @return list of books with similar titles, ordered by similarity
   */
  @Query(
      value =
          """
    SELECT * FROM books
    WHERE similarity(title, :title) > :similarityThreshold
    ORDER BY similarity(title, :title) DESC
    """,
      nativeQuery = true)
  List<Book> findBooksByTitleFuzzy(
      @Param("title") String title, @Param("similarityThreshold") double similarityThreshold);

  /**
   * Find a book by its ISBN.
   *
   * @param isbn the ISBN to search for
   * @return the book with the given ISBN, if found
   */
  Optional<Book> findByIsbn(String isbn);

  List<Book> findByAuthorContainingIgnoreCase(String author);
}
