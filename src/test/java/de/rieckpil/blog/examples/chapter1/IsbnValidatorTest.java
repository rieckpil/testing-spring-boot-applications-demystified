package de.rieckpil.blog.examples.chapter1;

import de.rieckpil.blog.IsbnValidator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IsbnValidatorTest {

  private final IsbnValidator validator = new IsbnValidator();

  @ParameterizedTest
  @ValueSource(strings = {"978-0132350884", "979-1234567890", "978-0134685991"})
  void shouldAcceptValidIsbns(String isbn) {
    assertTrue(validator.isValid(isbn));
  }

  @ParameterizedTest
  @CsvSource({"978-0132350884, true", "invalid-isbn, false", "123, false", "978-0134685991, true"})
  void shouldValidateIsbnCorrectly(String isbn, boolean expected) {
    assertEquals(expected, validator.isValid(isbn));
  }
}
