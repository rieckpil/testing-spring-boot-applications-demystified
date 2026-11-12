package de.rieckpil.blog;

import java.util.regex.Pattern;

/** Validates the format of an ISBN-13 with prefix, e.g. 978-0132350884. */
public class IsbnValidator {

  private static final Pattern ISBN_PATTERN = Pattern.compile("^97[89]-\\d{10}$");

  public boolean isValid(String isbn) {
    return isbn != null && ISBN_PATTERN.matcher(isbn).matches();
  }
}
