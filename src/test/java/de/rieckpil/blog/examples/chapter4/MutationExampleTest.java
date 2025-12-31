package de.rieckpil.blog.examples.chapter4;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MutationExampleTest {

  private class PriceCalculator {
    public double calculateDiscount(double price) {
      if (price > 100) {
        return price * 0.9; // 10% discount
      }
      return price;
    }
  }

  PriceCalculator calc = new PriceCalculator();

  @Test
  void testCalculateDiscount() {
    PriceCalculator calc = new PriceCalculator();

    double result = calc.calculateDiscount(150);
    // No assertion! But 100% coverage achieved.
  }

  @Test
  void shouldNotApplyDiscountAtBoundary() {
    double result = calc.calculateDiscount(100);

    assertEquals(100.0, result, 0.01);
  }

  @Test
  void shouldApplyDiscountAboveThreshold() {
    double result = calc.calculateDiscount(150);

    assertEquals(135.0, result, 0.01); // 150 * 0.9 = 135
  }

  @Test
  void shouldNotApplyDiscountBelowThreshold() {
    double result = calc.calculateDiscount(50);

    assertEquals(50.0, result, 0.01);
  }
}
