package de.rieckpil.blog.examples.chapter1;

import de.rieckpil.blog.PriceCalculator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PriceCalculatorTest {

  private PriceCalculator calculator = new PriceCalculator();

  @Test
  void shouldCalculatePriceWithoutDiscount() {
    // Given: Purchase under discount threshold
    double basePrice = 20.0;
    int quantity = 4; // Total: $80

    // When: Calculate final price
    double finalPrice = calculator.calculateFinalPrice(basePrice, quantity);

    // Then: Only tax is applied (80 * 1.08 = 86.40)
    assertEquals(86.40, finalPrice, 0.01);
  }

  @Test
  void shouldApplyDiscountForLargePurchases() {
    // Given: Purchase over discount threshold
    double basePrice = 50.0;
    int quantity = 3; // Total: $150

    // When: Calculate final price
    double finalPrice = calculator.calculateFinalPrice(basePrice, quantity);

    // Then: Discount and tax (150 * 0.9 * 1.08 = 145.80)
    assertEquals(145.80, finalPrice, 0.01);
  }

  @Test
  void shouldThrowExceptionForNegativePrice() {
    assertThrows(IllegalArgumentException.class, () -> calculator.calculateFinalPrice(-10, 5));
  }

  @Test
  void shouldThrowExceptionForZeroQuantity() {
    assertThrows(IllegalArgumentException.class, () -> calculator.calculateFinalPrice(10, 0));
  }
}
