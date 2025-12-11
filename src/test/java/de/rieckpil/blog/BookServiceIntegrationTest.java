package de.rieckpil.blog;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class BookServiceIntegrationTest extends BaseIntegrationTest {

  @Autowired private BookService bookService;

  @Autowired private BookRepository bookRepository;

  @MockitoBean private OpenLibraryApiClient openLibraryApiClient;

  @BeforeEach
  void setUp() {
    bookRepository.deleteAll();
  }

  @Test
  @DisplayName("Should create book with metadata from external API")
  void shouldCreateBookWithMetadata() {
    // Arrange
    String isbn = "9780134685991";
    BookCreationRequest request =
        new BookCreationRequest(isbn, "Effective Java", "Joshua Bloch", LocalDate.of(2018, 1, 6));

    BookMetadataResponse metadata =
        BookMetadataResponseMother.defaultBook().withIsbn13(isbn).withCoverId(8739161).build();
    when(openLibraryApiClient.getBookByIsbn(isbn)).thenReturn(metadata);

    // Act
    Long bookId = bookService.createBook(request);

    // Assert
    assertThat(bookId).isNotNull();

    Book savedBook = bookRepository.findById(bookId).orElseThrow();
    assertThat(savedBook.getIsbn()).isEqualTo(isbn);
    assertThat(savedBook.getThumbnailUrl()).isEqualTo(metadata.getCoverUrl());
  }

  @Test
  @DisplayName("Should throw exception when book already exists")
  void shouldThrowExceptionWhenBookExists() {
    // Arrange
    Book existingBook =
        new Book("9780134685991", "Effective Java", "Joshua Bloch", LocalDate.now());
    bookRepository.save(existingBook);

    BookCreationRequest request =
        new BookCreationRequest("9780134685991", "Effective Java", "Joshua Bloch", LocalDate.now());

    // Act & Assert
    assertThatThrownBy(() -> bookService.createBook(request))
        .isInstanceOf(BookAlreadyExistsException.class)
        .hasMessageContaining("9780134685991");
  }

  @Test
  @DisplayName("Should retrieve all books from database")
  void shouldGetAllBooks() {
    // Arrange
    Book book1 = new Book("9780134685991", "Effective Java", "Joshua Bloch", LocalDate.now());
    Book book2 = new Book("9781617292545", "Spring in Action", "Craig Walls", LocalDate.now());
    bookRepository.saveAll(List.of(book1, book2));

    // Act
    List<Book> books = bookService.getAllBooks();

    // Assert
    assertThat(books).hasSize(2);
    assertThat(books)
        .extracting(Book::getIsbn)
        .containsExactlyInAnyOrder("9780134685991", "9781617292545");
  }
}
