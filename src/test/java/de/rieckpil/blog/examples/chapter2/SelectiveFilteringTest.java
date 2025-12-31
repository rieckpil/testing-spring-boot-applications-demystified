package de.rieckpil.blog.examples.chapter2;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Example of selective test filtering using tags.
 * <br>
 * Run with ./mvnw test -Dgroups="fast"
 */
class SelectiveFilteringTest {

  @Tag("fast")
  @Test
  void shouldValidateIsbn() {
    // Fast unit test (milliseconds)
    System.out.println("Running fast test: shouldValidateIsbn");
  }

  @Tag("slow")
  @Tag("nightly")
  @Test
  void shouldProcessCompleteBorrowingWorkflow() {
    // Slow integration test (seconds)
    System.out.println("Running slow test: shouldProcessCompleteBorrowingWorkflow");
  }
}
