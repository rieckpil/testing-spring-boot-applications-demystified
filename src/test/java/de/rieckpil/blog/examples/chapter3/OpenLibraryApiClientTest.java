package de.rieckpil.blog.examples.chapter3;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import de.rieckpil.blog.BookMetadataResponse;
import de.rieckpil.blog.OpenLibraryApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OpenLibraryApiClientTest {

  @RegisterExtension
  static WireMockExtension wireMockServer =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  private OpenLibraryApiClient cut;

  @BeforeEach
  void setUp() {
    WebClient webClient = WebClient.builder().baseUrl(wireMockServer.baseUrl()).build();

    cut = new OpenLibraryApiClient(webClient);
  }

  @Test
  @DisplayName("Should return book metadata when API returns valid response")
  void shouldReturnBookMetadataWhenApiReturnsValidResponse() {
    // Arrange
    String isbn = "978-0132350884";

    wireMockServer.stubFor(
        get("/isbn/" + isbn)
            .willReturn(
                aResponse()
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBodyFile(isbn + "-success.json")));

    // Act
    BookMetadataResponse result = cut.getBookByIsbn(isbn);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.title()).isEqualTo("Clean Code");
    assertThat(result.isbn13().get(0)).isEqualTo("9780132350884");
    assertThat(result.numberOfPages()).isEqualTo(431);
    assertThat(result.getCoverUrl()).contains("14840846");
  }

  @Test
  @DisplayName("Should throw exception when API returns 500 error")
  void shouldThrowExceptionWhenApiReturns500() {
    // Arrange
    String isbn = "9999999999";

    wireMockServer.stubFor(
        get("/isbn/" + isbn)
            .willReturn(
                aResponse()
                    .withStatus(500)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody("{\"error\": \"Internal Server Error\"}")));

    // Act & Assert
    WebClientResponseException exception =
        assertThrows(WebClientResponseException.class, () -> cut.getBookByIsbn(isbn));

    assertThat(exception.getStatusCode().value()).isEqualTo(500);
  }

  @Test
  @DisplayName("Should throw exception when book not found")
  void shouldThrowExceptionWhenBookNotFound() {
    // Arrange
    String isbn = "9999999999";

    wireMockServer.stubFor(
        get("/isbn/" + isbn)
            .willReturn(
                aResponse()
                    .withStatus(404)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody("{\"error\": \"Not Found\"}")));

    // Act & Assert
    WebClientResponseException exception =
        assertThrows(WebClientResponseException.NotFound.class, () -> cut.getBookByIsbn(isbn));

    assertThat(exception.getStatusCode().value()).isEqualTo(404);
  }
}
