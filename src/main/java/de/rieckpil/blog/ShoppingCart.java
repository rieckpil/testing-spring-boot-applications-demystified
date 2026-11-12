package de.rieckpil.blog;

import java.util.HashMap;
import java.util.Map;

/** Simple shopping cart that looks up product prices via the {@link PricingService}. */
public class ShoppingCart {

  private final Map<String, CartItem> items = new HashMap<>();
  private final PricingService pricingService;

  public ShoppingCart(PricingService pricingService) {
    this.pricingService = pricingService;
  }

  public void addItem(String productId, int quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException("Quantity must be positive");
    }

    items.merge(
        productId,
        new CartItem(productId, quantity),
        (existing, newItem) -> new CartItem(productId, existing.quantity + quantity));
  }

  public double calculateTotal() {
    return items.values().stream()
        .mapToDouble(
            item -> {
              double price = pricingService.getPrice(item.productId);
              return price * item.quantity;
            })
        .sum();
  }

  public int getItemCount() {
    return items.values().stream().mapToInt(item -> item.quantity).sum();
  }

  private record CartItem(String productId, int quantity) {}
}
