package de.rieckpil.blog;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class BookControllerIntegrationTest extends BaseIntegrationTest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private BookRepository bookRepository;

  @MockitoBean private OpenLibraryApiClient openLibraryApiClient;

  private String baseUrl;

  @BeforeEach
  void setUp() {
    baseUrl = "http://localhost:" + port;
    bookRepository.deleteAll();
  }

  @Test
  @DisplayName("Should create book via REST API")
  void shouldCreateBookViaApi() {
    // Arrange
    BookMetadataResponse metadata =
        BookMetadataResponseMother.defaultBook().withCoverId(8739161).build();
    when(openLibraryApiClient.getBookByIsbn(anyString())).thenReturn(metadata);

    String requestBody =
        """
      {
        "isbn": "9780134685991",
        "title": "Effective Java",
        "author": "Joshua Bloch",
        "publishedDate": "2018-01-06"
      }
      """;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBasicAuth("user", "password");
    HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

    // Act
    ResponseEntity<Void> response =
        restTemplate.exchange(baseUrl + "/api/books", HttpMethod.POST, request, Void.class);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getHeaders().getLocation()).isNotNull();

    assertThat(bookRepository.count()).isEqualTo(1);
    Book savedBook = bookRepository.findAll().get(0);
    assertThat(savedBook.getIsbn()).isEqualTo("9780134685991");
  }

  @Test
  @DisplayName("Should get book by ID via REST API")
  void shouldGetBookById() {
    // Arrange
    Book book = new Book("9780134685991", "Effective Java", "Joshua Bloch", LocalDate.now());
    Book savedBook = bookRepository.save(book);

    HttpHeaders headers = new HttpHeaders();
    headers.setBasicAuth("user", "password");
    HttpEntity<Void> request = new HttpEntity<>(headers);

    // Act
    ResponseEntity<Book> response =
        restTemplate.exchange(
            baseUrl + "/api/books/" + savedBook.getId(), HttpMethod.GET, request, Book.class);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getIsbn()).isEqualTo("9780134685991");
  }

  @Test
  @DisplayName("Should return 401 when accessing without authentication")
  void shouldReturn401WithoutAuth() {
    // Act
    ResponseEntity<Void> response = restTemplate.getForEntity(baseUrl + "/api/books", Void.class);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }
}
