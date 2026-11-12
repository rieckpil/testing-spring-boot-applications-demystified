package de.rieckpil.blog.examples.chapter1;

import de.rieckpil.blog.PricingService;
import de.rieckpil.blog.ShoppingCart;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShoppingCartTest {

  @Mock private PricingService pricingService;

  @InjectMocks private ShoppingCart cart;

  @Nested
  class AddingItems {

    @Test
    void shouldAddSingleItem() {
      cart.addItem("PROD-001", 2);

      assertThat(cart.getItemCount()).isEqualTo(2);
    }

    @Test
    void shouldAccumulateQuantitiesForSameProduct() {
      cart.addItem("PROD-001", 2);
      cart.addItem("PROD-001", 3);

      assertThat(cart.getItemCount()).isEqualTo(5);
    }

    @Test
    void shouldRejectNonPositiveQuantities() {
      assertThatThrownBy(() -> cart.addItem("PROD-001", 0))
          .isInstanceOf(IllegalArgumentException.class);
      assertThatThrownBy(() -> cart.addItem("PROD-001", -1))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  class CalculatingTotals {

    @Test
    void shouldCalculateTotalForSingleItem() {
      when(pricingService.getPrice("PROD-001")).thenReturn(10.0);

      cart.addItem("PROD-001", 3);
      double total = cart.calculateTotal();

      assertThat(total).isEqualTo(30.0);
      verify(pricingService).getPrice("PROD-001");
    }

    @Test
    void shouldCalculateTotalForMultipleItems() {
      when(pricingService.getPrice("PROD-001")).thenReturn(10.0);
      when(pricingService.getPrice("PROD-002")).thenReturn(25.0);

      cart.addItem("PROD-001", 2);
      cart.addItem("PROD-002", 1);
      double total = cart.calculateTotal();

      assertThat(total).isEqualTo(45.0); // (2x10) + (1x25)
    }

    @Test
    void shouldReturnZeroForEmptyCart() {
      double total = cart.calculateTotal();

      assertThat(total).isEqualTo(0.0);
      verifyNoInteractions(pricingService);
    }
  }
}
