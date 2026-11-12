package de.rieckpil.blog;

/** Calculates the final price of a purchase: 10% discount above $100, then 8% tax. */
public class PriceCalculator {

  private static final double TAX_RATE = 0.08;
  private static final double DISCOUNT_THRESHOLD = 100.0;
  private static final double DISCOUNT_RATE = 0.10;

  public double calculateFinalPrice(double basePrice, int quantity) {
    if (basePrice <= 0 || quantity <= 0) {
      throw new IllegalArgumentException("Price and quantity must be positive");
    }

    double subtotal = basePrice * quantity;
    double discountedPrice = applyDiscount(subtotal);
    return applyTax(discountedPrice);
  }

  private double applyDiscount(double price) {
    if (price > DISCOUNT_THRESHOLD) {
      return price * (1 - DISCOUNT_RATE);
    }
    return price;
  }

  private double applyTax(double price) {
    return price * (1 + TAX_RATE);
  }
}
