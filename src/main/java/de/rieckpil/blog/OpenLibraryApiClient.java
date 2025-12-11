package de.rieckpil.blog;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/** Client for interacting with the OpenLibrary API. */
@Component
public class OpenLibraryApiClient {

  private final WebClient webClient;

  public OpenLibraryApiClient(WebClient openLibraryWebClient) {
    this.webClient = openLibraryWebClient;
  }

  public BookMetadataResponse getBookByIsbn(String isbn) {
    return webClient
        .get()
        .uri("/isbn/{isbn}", isbn)
        .retrieve()
        .bodyToMono(BookMetadataResponse.class)
        .block();
  }
}
