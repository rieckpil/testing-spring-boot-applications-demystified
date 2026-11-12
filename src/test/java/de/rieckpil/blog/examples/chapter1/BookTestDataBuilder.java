package de.rieckpil.blog.examples.chapter1;

import java.time.LocalDate;

import de.rieckpil.blog.Book;
import de.rieckpil.blog.BookStatus;

class BookTestDataBuilder {

  private String title = "Default Title";
  private String author = "Default Author";
  private String isbn = "978-0000000000";
  private LocalDate publishedDate = LocalDate.now();
  private BookStatus status = BookStatus.AVAILABLE;

  public BookTestDataBuilder withTitle(String title) {
    this.title = title;
    return this;
  }

  public BookTestDataBuilder withAuthor(String author) {
    this.author = author;
    return this;
  }

  public BookTestDataBuilder withIsbn(String isbn) {
    this.isbn = isbn;
    return this;
  }

  public BookTestDataBuilder withPublishedDate(LocalDate publishedDate) {
    this.publishedDate = publishedDate;
    return this;
  }

  public BookTestDataBuilder withStatus(BookStatus status) {
    this.status = status;
    return this;
  }

  public Book build() {
    Book book = new Book();
    book.setTitle(title);
    book.setAuthor(author);
    book.setIsbn(isbn);
    book.setPublishedDate(publishedDate);
    book.setStatus(status);
    return book;
  }
}
