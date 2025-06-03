package de.rieckpil.blog;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/books")
public class BookController {

  private final BookService bookService;

  public BookController(BookService bookService) {
    this.bookService = bookService;
  }

  @GetMapping
  public List<Book> getAllBooks() {
    return bookService.getAllBooks();
  }

  @GetMapping("/{id}")
  public ResponseEntity<Book> getBookById(@PathVariable Long id) {
    return bookService
        .getBookById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<Void> createBook(
      @Valid @RequestBody BookCreationRequest request, UriComponentsBuilder uriComponentsBuilder) {
    Long id = bookService.createBook(request);

    return ResponseEntity.created(
            uriComponentsBuilder.path("/api/books/{id}").buildAndExpand(id).toUri())
        .build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<Book> updateBook(
      @PathVariable Long id, @Valid @RequestBody BookUpdateRequest request) {
    return bookService
        .updateBook(id, request)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
    return bookService.deleteBook(id)
        ? ResponseEntity.noContent().build()
        : ResponseEntity.notFound().build();
  }
}
