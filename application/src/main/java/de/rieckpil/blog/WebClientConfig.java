package de.rieckpil.blog;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

  @Bean
  public WebClient openLibraryWebClient(
      @Value("${book.metadata.api.url:https://openlibrary.org}") String baseUrl,
      @Value("${book.metadata.api.timeout:5}") int timeoutSeconds) {

    HttpClient httpClient =
        HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutSeconds * 1000)
            .responseTimeout(Duration.ofSeconds(timeoutSeconds))
            .doOnConnected(
                conn ->
                    conn.addHandlerLast(new ReadTimeoutHandler(timeoutSeconds, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeoutSeconds, TimeUnit.SECONDS)));

    return WebClient.builder()
        .baseUrl(baseUrl)
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .filter(logRequest())
        .codecs(
            configurer ->
                configurer
                    .defaultCodecs()
                    .maxInMemorySize(16 * 1024 * 1024)) // 16MB buffer for larger responses
        .build();
  }

  private ExchangeFilterFunction logRequest() {
    return ExchangeFilterFunction.ofRequestProcessor(
        clientRequest -> {
          System.out.println("Request: " + clientRequest.method() + " " + clientRequest.url());
          return Mono.just(clientRequest);
        });
  }
}
