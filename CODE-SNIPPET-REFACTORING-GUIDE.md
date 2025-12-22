# Code Snippet Refactoring Guide

## Goal
Maximum 12-15 lines per code block with step-by-step explanations between snippets.

## Strategy

### Before (Bad Example - 27 lines)
```java
public class PriceCalculator {
  private static final double TAX_RATE = 0.08;
  private static final double DISCOUNT_THRESHOLD = 100.0;
  private static final double DISCOUNT_RATE = 0.10;

  public double calculateFinalPrice(double basePrice, int quantity) {
    if (basePrice <= 0 || quantity <= 0) {
      throw new IllegalArgumentException(
        "Price and quantity must be positive");
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
```

### After (Good Example - Split into digestible pieces)

**Part 1: Class Setup (7 lines)**
```java
public class PriceCalculator {
  private static final double TAX_RATE = 0.08;
  private static final double DISCOUNT_THRESHOLD = 100.0;
  private static final double DISCOUNT_RATE = 0.10;

  // Methods follow...
}
```

**Explanation:** We define our business rules as constants: 8% tax rate, $100 discount threshold, and 10% discount rate. These constants make our code self-documenting.

**Part 2: Main Calculation Method (10 lines)**
```java
public double calculateFinalPrice(double basePrice, int quantity) {
  if (basePrice <= 0 || quantity <= 0) {
    throw new IllegalArgumentException(
      "Price and quantity must be positive");
  }

  double subtotal = basePrice * quantity;
  double discountedPrice = applyDiscount(subtotal);
  return applyTax(discountedPrice);
}
```

**Explanation:** The main method validates inputs first, then calculates the subtotal. It applies the discount first, then tax on the discounted price. This order matters for correct calculations.

**Part 3: Helper Methods (10 lines)**
```java
private double applyDiscount(double price) {
  if (price > DISCOUNT_THRESHOLD) {
    return price * (1 - DISCOUNT_RATE);
  }
  return price;
}

private double applyTax(double price) {
  return price * (1 + TAX_RATE);
}
```

**Explanation:** Two private helpers keep our code clean. `applyDiscount` only applies the 10% discount for purchases over $100. `applyTax` always applies the 8% tax rate.

## Patterns to Apply

### Pattern 1: Split by Logical Sections
- Class declaration + constants
- Constructor + fields
- Main methods
- Helper methods

### Pattern 2: Progressive Build-Up
Show code building up step-by-step:
1. Empty test method structure
2. Add arrange section
3. Add act section
4. Add assert section

### Pattern 3: Focus on What's New
Don't repeat code. Use comments like:
```java
class BookServiceTest {
  // Same setup as before...

  @Test
  void newTestCase() {
    // Only show the new test
  }
}
```

### Pattern 4: Extract Common Setup
```java
// Setup (shown once)
@BeforeEach
void setUp() {
  // Common initialization
}

// Then in tests, reference it
@Test
void testCase() {
  // setUp() already ran, use initialized objects
}
```

## Examples of Good Splits

### Testing Example (Split into 3 parts)

**Part 1: Test Class Setup**
```java
class PriceCalculatorTest {
  private PriceCalculator calculator = new PriceCalculator();

  // Tests follow...
}
```

**Part 2: First Test Case**
```java
@Test
void shouldCalculatePriceWithoutDiscount() {
  // Given: Purchase under discount threshold
  double basePrice = 20.0;
  int quantity = 4;

  // When
  double result = calculator.calculateFinalPrice(basePrice, quantity);

  // Then: Only tax applied (80 * 1.08 = 86.40)
  assertEquals(86.40, result, 0.01);
}
```

**Part 3: Second Test Case**
```java
@Test
void shouldApplyDiscountForLargePurchases() {
  // Given: Purchase over $100 threshold
  double basePrice = 50.0;
  int quantity = 3;

  // When
  double result = calculator.calculateFinalPrice(basePrice, quantity);

  // Then: Discount + tax (150 * 0.9 * 1.08 = 145.80)
  assertEquals(145.80, result, 0.01);
}
```

## Chapters Requiring Updates

Based on initial review:

1. **Chapter 1** (01-unit-testing-boss.md)
   - PriceCalculator class (27 lines → split into 3)
   - ShoppingCart class (likely 25+ lines → split into 4-5)
   - Test classes with multiple methods (split each test separately)

2. **Chapter 2** (02-sliced-testing-hydra.md)
   - BookController examples (split by method)
   - Repository tests (split setup from tests)
   - Security configuration examples

3. **Chapter 3** (03-integration-testing-final-boss.md)
   - BaseIntegrationTest class (split into logical sections)
   - WireMock setup (split configuration from test)
   - Multi-step integration tests

4. **Chapter 4** (04-quest-items.md)
   - Context caching examples
   - Parallelization configuration
   - Test data builder examples

5. **Chapter 5** (05-exiting-the-maze.md)
   - Strategy templates (already well-structured)
   - Quick reference code (can stay as-is in appendix)

## Action Plan

For each chapter:
1. Identify code blocks > 15 lines
2. Split into logical 10-12 line chunks
3. Add explanation between each chunk
4. Ensure each chunk is complete and runnable (or clearly marked as partial)
5. Use progressive disclosure (show what's new, reference what's known)

## Benefits

✅ Better learning progression
✅ Easier to understand complex code
✅ More opportunities for explanations
✅ Higher information density
✅ Better for PDF readability
✅ Matches "step-by-step" teaching approach
