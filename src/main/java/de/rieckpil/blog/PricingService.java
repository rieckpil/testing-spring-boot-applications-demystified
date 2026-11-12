package de.rieckpil.blog;

/** Collaborator of the {@link ShoppingCart}, used to demonstrate mocking with Mockito. */
public interface PricingService {

  double getPrice(String productId);
}
