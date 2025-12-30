package de.rieckpil.blog;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookControllerUnitTest {

  @Test
  void shouldCreateBook() {
    BookService mockService = mock(BookService.class);
    BookController controller = new BookController(mockService);

    when(mockService.createBook(any()))
      .thenReturn(1L);

    BookCreationRequest request = new BookCreationRequest(
      "9780132350884", "Clean Code", "Robert Martin",
      LocalDate.of(2008, 8, 1)
    );

    ResponseEntity<Void> response =
      controller.createBook(request, UriComponentsBuilder.fromUriString("/"));

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals("/api/books/1",
      response.getHeaders().getLocation().toString());
  }
}
