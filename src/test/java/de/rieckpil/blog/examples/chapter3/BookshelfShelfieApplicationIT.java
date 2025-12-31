package de.rieckpil.blog.examples.chapter3;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import de.rieckpil.blog.Book;
import de.rieckpil.blog.BookRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestRestTemplate
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookshelfShelfieApplicationIT {

  @Container @ServiceConnection
  static PostgreSQLContainer postgres =
      new PostgreSQLContainer("postgres:16-alpine")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private BookRepository bookRepository;

  static WireMockServer wireMockServer;

  @BeforeAll
  static void startWireMock() {
    wireMockServer = new WireMockServer(new WireMockConfiguration().dynamicPort());
    wireMockServer.start();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("book.metadata.api.url", () -> "http://localhost:" + wireMockServer.port());
  }

  @BeforeEach
  void setUp() {
    wireMockServer.resetAll(); // Clear all stubs
    this.bookRepository.deleteAll();
  }

  @Test
  void shouldCreateAndRetrieveBook() {

    String isbn = "978-0132350884";

    wireMockServer.stubFor(
        get(urlEqualTo("/isbn/" + isbn))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("978-0132350884-success.json")));

    String createRequest =
        """
      {
        "isbn": "978-0132350884",
        "title": "Clean Code",
        "author": "Robert C. Martin",
        "publishedDate": "2008-01-07"
      }
      """;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBasicAuth("admin", "admin");

    ResponseEntity<Void> createResponse =
        restTemplate.exchange(
            "/api/books", HttpMethod.POST, new HttpEntity<>(createRequest, headers), Void.class);

    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(createResponse.getHeaders().getLocation()).isNotNull();

    // Extract book ID from Location header
    String location = createResponse.getHeaders().getLocation().toString();
    String bookId = location.substring(location.lastIndexOf('/') + 1);

    ResponseEntity<Book> getResponse =
        restTemplate.withBasicAuth("user", "user").getForEntity("/api/books/" + bookId, Book.class);

    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    Book book = getResponse.getBody();
    assertThat(book).isNotNull();

    assertThat(book.getIsbn()).isEqualTo("978-0132350884");
    assertThat(book.getTitle()).isEqualTo("Clean Code");
    assertThat(book.getAuthor()).isEqualTo("Robert C. Martin");
    assertThat(book.getThumbnailUrl())
        .contains("https://covers.openlibrary.org/b/id/14840846-M.jpg");
  }
}
