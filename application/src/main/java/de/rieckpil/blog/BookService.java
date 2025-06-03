package de.rieckpil.blog;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class BookService {

  private final BookRepository bookRepository;
  private final OpenLibraryApiClient openLibraryApiClient;

  public BookService(BookRepository bookRepository, OpenLibraryApiClient openLibraryApiClient) {
    this.bookRepository = bookRepository;
    this.openLibraryApiClient = openLibraryApiClient;
  }

  public Long createBook(BookCreationRequest request) {
    if (bookRepository.findByIsbn(request.isbn()).isPresent()) {
      throw new BookAlreadyExistsException(request.isbn());
    }

    Book book =
        new Book(request.isbn(), request.title(), request.author(), request.publishedDate());

    BookMetadataResponse metadata = openLibraryApiClient.getBookByIsbn(request.isbn());

    book.setThumbnailUrl(metadata.getCoverUrl());

    Book savedBook = bookRepository.save(book);
    return savedBook.getId();
  }

  public List<Book> getAllBooks() {
    return bookRepository.findAll();
  }

  public Optional<Book> getBookById(Long id) {
    return bookRepository.findById(id);
  }

  public Optional<Book> updateBook(Long id, BookUpdateRequest request) {
    return bookRepository
        .findById(id)
        .map(
            book -> {
              book.setTitle(request.title());
              book.setAuthor(request.author());
              book.setPublishedDate(request.publishedDate());
              book.setStatus(request.status());
              return bookRepository.save(book);
            });
  }

  public boolean deleteBook(Long id) {
    return bookRepository
        .findById(id)
        .map(
            book -> {
              bookRepository.delete(book);
              return true;
            })
        .orElse(false);
  }
}
