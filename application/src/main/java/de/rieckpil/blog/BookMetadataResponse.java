package de.rieckpil.blog;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BookMetadataResponse(
    // Core book info
    String key,
    String title,

    // ISBN identifiers
    @JsonProperty("isbn_13") List<String> isbn13,
    @JsonProperty("isbn_10") List<String> isbn10,

    // Publication info
    @JsonProperty("publish_date") String publishDate,
    List<String> publishers,

    // Author references - need to be fetched separately
    @JsonProperty("authors") List<Map<String, String>> authorRefs,

    // Physical details
    @JsonProperty("number_of_pages") Integer numberOfPages,
    @JsonProperty("physical_format") String physicalFormat,

    // Additional metadata
    String description,
    @JsonProperty("subjects") List<String> subjects,
    @JsonProperty("cover") Map<String, Integer> covers) {
  // Convenience methods
  public String getMainIsbn() {
    if (isbn13 != null && !isbn13.isEmpty()) {
      return isbn13.get(0);
    } else if (isbn10 != null && !isbn10.isEmpty()) {
      return isbn10.get(0);
    }
    return null;
  }

  public String getPublisher() {
    if (publishers != null && !publishers.isEmpty()) {
      return publishers.get(0);
    }
    return null;
  }

  public Integer getCoverId() {
    if (covers != null && covers.containsKey("medium")) {
      return covers.get("medium");
    }
    return null;
  }

  public String getCoverUrl() {
    Integer coverId = getCoverId();
    if (coverId != null) {
      return "https://covers.openlibrary.org/b/id/" + coverId + "-M.jpg";
    }
    return null;
  }
}
