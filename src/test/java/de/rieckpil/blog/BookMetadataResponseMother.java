package de.rieckpil.blog;

import java.util.List;
import java.util.Map;

/**
 * TestObjectMother for creating BookMetadataResponse instances in tests. Provides sensible defaults
 * and a fluent API for customization.
 */
public class BookMetadataResponseMother {

  private String key = "/books/OL7353617M";
  private String title = "Effective Java";
  private List<String> isbn13 = List.of("9780134685991");
  private List<String> isbn10 = List.of("0134685997");
  private String publishDate = "January 6, 2018";
  private List<String> publishers = List.of("Addison-Wesley Professional");
  private List<Map<String, String>> authorRefs = List.of(Map.of("key", "/authors/OL123456A"));
  private Integer numberOfPages = 416;
  private String physicalFormat = "Hardcover";
  private String description = "A comprehensive guide to Java best practices";
  private List<String> subjects = List.of("Java", "Programming", "Software Engineering");
  private Map<String, Integer> covers = Map.of("medium", 8739161);

  private BookMetadataResponseMother() {}

  public static BookMetadataResponseMother defaultBook() {
    return new BookMetadataResponseMother();
  }

  public BookMetadataResponseMother withKey(String key) {
    this.key = key;
    return this;
  }

  public BookMetadataResponseMother withTitle(String title) {
    this.title = title;
    return this;
  }

  public BookMetadataResponseMother withIsbn13(List<String> isbn13) {
    this.isbn13 = isbn13;
    return this;
  }

  public BookMetadataResponseMother withIsbn13(String... isbn13) {
    this.isbn13 = List.of(isbn13);
    return this;
  }

  public BookMetadataResponseMother withIsbn10(List<String> isbn10) {
    this.isbn10 = isbn10;
    return this;
  }

  public BookMetadataResponseMother withIsbn10(String... isbn10) {
    this.isbn10 = List.of(isbn10);
    return this;
  }

  public BookMetadataResponseMother withPublishDate(String publishDate) {
    this.publishDate = publishDate;
    return this;
  }

  public BookMetadataResponseMother withPublishers(List<String> publishers) {
    this.publishers = publishers;
    return this;
  }

  public BookMetadataResponseMother withPublishers(String... publishers) {
    this.publishers = List.of(publishers);
    return this;
  }

  public BookMetadataResponseMother withAuthorRefs(List<Map<String, String>> authorRefs) {
    this.authorRefs = authorRefs;
    return this;
  }

  public BookMetadataResponseMother withNumberOfPages(Integer numberOfPages) {
    this.numberOfPages = numberOfPages;
    return this;
  }

  public BookMetadataResponseMother withPhysicalFormat(String physicalFormat) {
    this.physicalFormat = physicalFormat;
    return this;
  }

  public BookMetadataResponseMother withDescription(String description) {
    this.description = description;
    return this;
  }

  public BookMetadataResponseMother withSubjects(List<String> subjects) {
    this.subjects = subjects;
    return this;
  }

  public BookMetadataResponseMother withSubjects(String... subjects) {
    this.subjects = List.of(subjects);
    return this;
  }

  public BookMetadataResponseMother withCovers(Map<String, Integer> covers) {
    this.covers = covers;
    return this;
  }

  public BookMetadataResponseMother withCoverId(Integer coverId) {
    this.covers = Map.of("medium", coverId);
    return this;
  }

  public BookMetadataResponseMother withoutCover() {
    this.covers = null;
    return this;
  }

  public BookMetadataResponseMother withoutIsbn() {
    this.isbn13 = null;
    this.isbn10 = null;
    return this;
  }

  public BookMetadataResponse build() {
    return new BookMetadataResponse(
        key,
        title,
        isbn13,
        isbn10,
        publishDate,
        publishers,
        authorRefs,
        numberOfPages,
        physicalFormat,
        description,
        subjects,
        covers);
  }
}
