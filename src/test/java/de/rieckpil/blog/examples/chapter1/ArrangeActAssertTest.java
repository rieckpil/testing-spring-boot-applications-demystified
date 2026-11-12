package de.rieckpil.blog.examples.chapter1;

import de.rieckpil.blog.Calculator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArrangeActAssertTest {

  @Test
  void shouldCalculateTotalPrice() {
    // Arrange - Set up test data and preconditions
    Calculator calculator = new Calculator();
    int basePrice = 100;
    int quantity = 3;

    // Act - Execute the method under test
    int total = calculator.multiply(basePrice, quantity);

    // Assert - Verify the result
    assertEquals(300, total);
  }

  @Test
  void shouldCalculateTotalPriceWithGivenWhenThen() {
    // Given a calculator and price details
    Calculator calculator = new Calculator();
    int basePrice = 100;
    int quantity = 3;

    // When we calculate the total
    int total = calculator.multiply(basePrice, quantity);

    // Then we get the correct result
    assertEquals(300, total);
  }
}
