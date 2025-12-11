package de.rieckpil.blog;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

  @Mock private BookRepository bookRepository;

  @Mock private OpenLibraryApiClient openLibraryApiClient;

  @Test
  void shouldThrowExceptionWhenBookWithIsbnAlreadyExists() {
    // Arrange
    BookService cut = new BookService(bookRepository, openLibraryApiClient);
    String existingIsbn = "9780134685991";

    when(bookRepository.findByIsbn(existingIsbn))
        .thenReturn(
            Optional.of(new Book(existingIsbn, "Effective Java", "Joshua Bloch", LocalDate.now())));

    BookCreationRequest request =
        new BookCreationRequest(existingIsbn, "Effective Java", "Joshua Bloch", LocalDate.now());

    // Act & Assert
    BookAlreadyExistsException exception =
        assertThrows(BookAlreadyExistsException.class, () -> cut.createBook(request));

    assertThat(exception.getMessage()).contains(existingIsbn);
  }

  @Test
  @DisplayName("Should create a book when ISBN does not exist")
  void shouldCreateBookWhenIsbnDoesNotExist() {
    // Arrange
    BookService cut = new BookService(bookRepository, openLibraryApiClient);
    String isbn = "9780134685991";
    String title = "Effective Java";
    String author = "Joshua Bloch";
    LocalDate publishedDate = LocalDate.now();

    BookCreationRequest request = new BookCreationRequest(isbn, title, author, publishedDate);

    Book savedBook = new Book(isbn, title, author, publishedDate);
    savedBook.setId(42L);

    BookMetadataResponse metadata =
        BookMetadataResponseMother.defaultBook().withIsbn13(isbn).withCoverId(8739161).build();

    when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());
    when(openLibraryApiClient.getBookByIsbn(isbn)).thenReturn(metadata);
    when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

    // Act
    Long bookId = cut.createBook(request);

    // Assert
    assertThat(bookId).isEqualTo(42L);

    ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
    verify(bookRepository).save(bookCaptor.capture());

    Book capturedBook = bookCaptor.getValue();
    assertThat(capturedBook.getIsbn()).isEqualTo(isbn);
    assertThat(capturedBook.getTitle()).isEqualTo(title);
    assertThat(capturedBook.getAuthor()).isEqualTo(author);
    assertThat(capturedBook.getPublishedDate()).isEqualTo(publishedDate);
    assertThat(capturedBook.getThumbnailUrl()).isEqualTo(metadata.getCoverUrl());
  }
}
