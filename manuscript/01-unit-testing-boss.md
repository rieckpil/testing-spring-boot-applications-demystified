# Chapter 1: Boss Fight #1 - The Unit Testing Guardian

![Unit Testing Guardian Boss](resources/unit-testing-boss.png)

## Confronting the Guardian

At the first chamber of the maze, you encounter the Unit Testing Guardian. This boss blocks your path with a fundamental question:

> **"Can you test your code without Spring? Without databases? Without any external dependencies?"**

Many Spring Boot developers skip this boss entirely, jumping straight to `@SpringBootTest` for everything. But that's a mistake. The Unit Testing Guardian teaches us the most important testing foundation: **isolation**.

Defeat this boss, and you'll gain:
- ⚡ Lightning-fast tests that run in milliseconds
- 🎯 Precise feedback when something breaks
- 🛡️ A solid foundation for all other testing

Skip this boss, and you'll struggle with slow test suites, unclear failures, and frustrated debugging sessions.

## What Makes a Test a Unit Test?

A unit test verifies a single unit of code in **complete isolation** from external dependencies. For Spring Boot applications, this means:

**What Unit Tests Do:**
- Test pure business logic
- Test algorithmic computations
- Test data validation
- Test utility functions
- Run in milliseconds
- Require no Spring context
- Use no databases, no files, no network

**What Unit Tests Don't Do:**
- Load Spring application context
- Connect to databases
- Make HTTP calls
- Read from file systems
- Start embedded servers

Unit tests are testing **your code**, not the Spring Framework. Spring has its own tests. We trust Spring works. Our unit tests verify our business logic.

## Understanding the Testing Pyramid Foundation

Remember our mental map? Unit tests form the base:

```
Unit Tests: Like testing individual LEGO bricks
  ↓
Integration Tests: Like testing how bricks connect
  ↓
E2E Tests: Like testing the completed LEGO castle
```

We want **many** unit tests because they're:
- **Fast**: Execute in milliseconds
- **Stable**: No external dependencies to fail
- **Precise**: Pinpoint exactly what broke
- **Cheap**: Easy to write and maintain

## Your Weapons for This Boss Fight

The Unit Testing Guardian can only be defeated with three weapons, all part of your testing Swiss Army knife:

**Weapon #1: JUnit 5 (Your Sword)**

JUnit 5 is your testing framework. It provides:
- Annotations to mark test methods (`@Test`)
- Lifecycle hooks (`@BeforeEach`, `@AfterEach`)
- Assertions (`assertEquals`, `assertTrue`)
- Test execution engine

**Weapon #2: Mockito (Your Shield)**

Mockito creates test doubles for dependencies:
- Create fake implementations of interfaces
- Define behavior: "when this method is called, return this value"
- Verify interactions: "was this method called with these parameters?"

**Weapon #3: AssertJ (Your Precision Tool)**

AssertJ provides fluent, readable assertions:
- `assertThat(value).isEqualTo(expected)`
- `assertThat(list).hasSize(3).contains("item")`
- `assertThat(string).startsWith("Hello").endsWith("World")`

Let's see these weapons in action.

## The AAA Battle Pattern

Every unit test follows the AAA (Arrange-Act-Assert) pattern:

```java
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
```

This pattern creates clear, readable tests:
1. **Arrange**: Prepare everything needed for the test
2. **Act**: Execute the single method being tested
3. **Assert**: Verify the outcome matches expectations

Some developers prefer **Given-When-Then** (GWT), which is essentially the same:

```java
@Test
void shouldCalculateTotalPrice() {
  // Given a calculator and price details
  Calculator calculator = new Calculator();
  int basePrice = 100;
  int quantity = 3;

  // When we calculate the total
  int total = calculator.multiply(basePrice, quantity);

  // Then we get the correct result
  assertEquals(300, total);
}
```

Use whichever pattern makes your tests more readable. We'll use both throughout this book.

## Boss Strategy #1: Testing Pure Business Logic

Let's test a realistic piece of business logic from our Shelfie application. Here's a price calculator that applies discounts and taxes:

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

This class contains business rules:
- 8% tax applies to all purchases
- 10% discount for orders over $100
- Discount applies before tax
- Invalid inputs throw exceptions

Let's test these rules comprehensively:

```java
class PriceCalculatorTest {
  private PriceCalculator calculator = new PriceCalculator();

  @Test
  void shouldCalculatePriceWithoutDiscount() {
    // Given: Purchase under discount threshold
    double basePrice = 20.0;
    int quantity = 4; // Total: $80

    // When: Calculate final price
    double finalPrice = calculator.calculateFinalPrice(
      basePrice, quantity);

    // Then: Only tax is applied (80 * 1.08 = 86.40)
    assertEquals(86.40, finalPrice, 0.01);
  }

  @Test
  void shouldApplyDiscountForLargePurchases() {
    // Given: Purchase over discount threshold
    double basePrice = 50.0;
    int quantity = 3; // Total: $150

    // When: Calculate final price
    double finalPrice = calculator.calculateFinalPrice(
      basePrice, quantity);

    // Then: Discount and tax applied (150 * 0.9 * 1.08 = 145.80)
    assertEquals(145.80, finalPrice, 0.01);
  }

  @Test
  void shouldThrowExceptionForNegativePrice() {
    // When/Then: Negative price throws exception
    assertThrows(IllegalArgumentException.class,
      () -> calculator.calculateFinalPrice(-10, 5));
  }

  @Test
  void shouldThrowExceptionForZeroQuantity() {
    // When/Then: Zero quantity throws exception
    assertThrows(IllegalArgumentException.class,
      () -> calculator.calculateFinalPrice(10, 0));
  }
}
```

Notice what makes these good unit tests:
- **No Spring annotations**: Just plain JUnit
- **No external dependencies**: Pure Java object creation
- **Fast execution**: Run in milliseconds
- **Clear intent**: Each test name describes the scenario
- **Complete coverage**: Happy path, edge cases, error cases

The third parameter in `assertEquals(86.40, finalPrice, 0.01)` is the delta for floating-point comparison. We allow 1 cent difference to handle floating-point precision.

## Boss Strategy #2: Using Mockito for Isolation

Real applications have dependencies. Here's a shopping cart service that depends on a pricing service:

```java
@Service
public class ShoppingCart {
  private final Map<String, CartItem> items = new HashMap<>();
  private final PricingService pricingService;

  public ShoppingCart(PricingService pricingService) {
    this.pricingService = pricingService;
  }

  public void addItem(String productId, int quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException(
        "Quantity must be positive");
    }

    items.merge(productId,
      new CartItem(productId, quantity),
      (existing, newItem) ->
        new CartItem(productId, existing.quantity + quantity)
    );
  }

  public double calculateTotal() {
    return items.values().stream()
      .mapToDouble(item -> {
        double price = pricingService.getPrice(item.productId);
        return price * item.quantity;
      })
      .sum();
  }

  public int getItemCount() {
    return items.values().stream()
      .mapToInt(item -> item.quantity)
      .sum();
  }

  private record CartItem(String productId, int quantity) {}
}
```

To unit test `ShoppingCart`, we need to isolate it from `PricingService`. We don't want our test to:
- Call a real database for prices
- Depend on external price data
- Break when price data changes

This is where Mockito saves us:

```java
@ExtendWith(MockitoExtension.class)
class ShoppingCartTest {

  @Mock
  private PricingService pricingService;

  @InjectMocks
  private ShoppingCart cart;

  @Nested
  @DisplayName("Adding items to cart")
  class AddingItems {

    @Test
    @DisplayName("should add single item successfully")
    void addSingleItem() {
      // When
      cart.addItem("PROD-001", 2);

      // Then
      assertThat(cart.getItemCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("should accumulate quantities for same product")
    void accumulateQuantities() {
      // When
      cart.addItem("PROD-001", 2);
      cart.addItem("PROD-001", 3);

      // Then
      assertThat(cart.getItemCount()).isEqualTo(5);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    @DisplayName("should reject non-positive quantities")
    void rejectInvalidQuantities(int quantity) {
      assertThrows(IllegalArgumentException.class,
        () -> cart.addItem("PROD-001", quantity));
    }
  }

  @Nested
  @DisplayName("Calculating totals")
  class CalculatingTotals {

    @Test
    @DisplayName("should calculate total for single item")
    void calculateSingleItemTotal() {
      // Given
      when(pricingService.getPrice("PROD-001"))
        .thenReturn(10.0);

      // When
      cart.addItem("PROD-001", 3);
      double total = cart.calculateTotal();

      // Then
      assertThat(total).isEqualTo(30.0);
      verify(pricingService).getPrice("PROD-001");
    }

    @Test
    @DisplayName("should calculate total for multiple items")
    void calculateMultipleItemsTotal() {
      // Given
      when(pricingService.getPrice("PROD-001"))
        .thenReturn(10.0);
      when(pricingService.getPrice("PROD-002"))
        .thenReturn(25.0);

      // When
      cart.addItem("PROD-001", 2);
      cart.addItem("PROD-002", 1);
      double total = cart.calculateTotal();

      // Then
      assertThat(total).isEqualTo(45.0); // (2×10) + (1×25)
    }

    @Test
    @DisplayName("should return zero for empty cart")
    void emptyCartTotal() {
      // When
      double total = cart.calculateTotal();

      // Then
      assertThat(total).isEqualTo(0.0);
      verifyNoInteractions(pricingService);
    }
  }
}
```

Let's break down the Mockito magic:

**`@ExtendWith(MockitoExtension.class)`**
- Activates Mockito for JUnit 5
- Enables Mockito annotations

**`@Mock private PricingService pricingService`**
- Creates a mock (fake) PricingService
- Has no real behavior until we configure it

**`@InjectMocks private ShoppingCart cart`**
- Creates a real ShoppingCart instance
- Automatically injects mocked dependencies into it

**`when(pricingService.getPrice("PROD-001")).thenReturn(10.0)`**
- Defines behavior: "When `getPrice()` is called with 'PROD-001', return 10.0"
- This is called "stubbing"

**`verify(pricingService).getPrice("PROD-001")`**
- Verifies the mock was called with specific arguments
- Ensures our code actually used the dependency

**`verifyNoInteractions(pricingService)`**
- Ensures the mock was never called
- Important for testing optimization paths

Notice how we use `@Nested` classes to group related tests. This creates readable test reports:

```
ShoppingCartTest
├── Adding items to cart
│   ├── should add single item successfully ✓
│   ├── should accumulate quantities for same product ✓
│   └── should reject non-positive quantities [3 tests] ✓
└── Calculating totals
    ├── should calculate total for single item ✓
    ├── should calculate total for multiple items ✓
    └── should return zero for empty cart ✓
```

## Boss Strategy #3: Parameterized Tests

Testing multiple similar scenarios leads to duplication:

```java
// Bad: Lots of duplication
@Test
void shouldValidateValidIsbn1() {
  assertTrue(validator.isValid("978-0132350884"));
}

@Test
void shouldValidateValidIsbn2() {
  assertTrue(validator.isValid("979-1234567890"));
}

@Test
void shouldValidateValidIsbn3() {
  assertTrue(validator.isValid("978-0134685991"));
}
```

JUnit 5's parameterized tests eliminate this duplication:

```java
@ParameterizedTest
@ValueSource(strings = {
  "978-0132350884",
  "979-1234567890",
  "978-0134685991"
})
void shouldAcceptValidIsbns(String isbn) {
  assertTrue(validator.isValid(isbn));
}
```

For testing multiple parameters together, use `@CsvSource`:

```java
@ParameterizedTest
@CsvSource({
  "978-0132350884, true",
  "invalid-isbn, false",
  "123, false",
  "978-0134685991, true"
})
void shouldValidateIsbnCorrectly(String isbn, boolean expected) {
  assertEquals(expected, validator.isValid(isbn));
}
```

Each line in `@CsvSource` provides one test case. The first value maps to `isbn`, the second to `expected`.

## Boss Strategy #4: Testing with AssertJ

While JUnit's assertions work, AssertJ makes tests more readable:

```java
// JUnit assertions - functional but plain
assertEquals(3, list.size());
assertTrue(result.contains("item"));
assertEquals("expected", actual);

// AssertJ - fluent and expressive
assertThat(list).hasSize(3);
assertThat(result).contains("item");
assertThat(actual).isEqualTo("expected");
```

AssertJ really shines with complex assertions:

```java
@Test
void shouldValidateBookDetails() {
  Book book = new Book("Clean Code", "Robert Martin",
                       "978-0132350884");
  book.setDescription("A handbook of agile software craftsmanship");
  book.setPageCount(464);

  assertThat(book)
    .satisfies(b -> {
      assertThat(b.getTitle()).isEqualTo("Clean Code");
      assertThat(b.getAuthor()).contains("Martin");
      assertThat(b.getIsbn()).startsWith("978");
      assertThat(b.getPageCount()).isGreaterThan(400);
      assertThat(b.getDescription()).isNotEmpty();
    });
}
```

For collections, AssertJ is even more powerful:

```java
@Test
void shouldFilterBooksByGenre() {
  List<Book> sciFiBooks = bookRepository.findByGenre("Science Fiction");

  assertThat(sciFiBooks)
    .hasSize(3)
    .extracting(Book::getTitle)
    .containsExactlyInAnyOrder(
      "Dune",
      "Foundation",
      "Neuromancer"
    );
}
```

The `extracting()` method pulls out a specific property, letting us assert on just titles without caring about the full Book objects.

## What NOT to Test

The Unit Testing Guardian has one final lesson: **Don't test everything.**

Some things aren't worth unit testing:

**Don't Test Getters and Setters:**

```java
// Bad: No value
@Test
void testGetName() {
  book.setName("Test Book");
  assertEquals("Test Book", book.getName());
}
```

This tests Java itself, not your logic. Getters and setters are too simple to break.

**Don't Test Framework Code:**

```java
// Bad: Testing Spring's @Autowired
@Test
void testServiceIsInjected() {
  assertNotNull(controller.getBookService());
}
```

Spring has its own tests. Trust that dependency injection works.

**Don't Test Private Methods:**

```java
// Bad: Using reflection to test private method
Method method = BookService.class
  .getDeclaredMethod("calculateDiscount");
method.setAccessible(true);
```

If a private method needs testing, it's probably doing too much. Either:
1. Test it through public methods that call it
2. Extract it to a separate class and test that

**Focus on Behavior, Not Implementation:**

```java
// Bad: Testing HOW something works
@Test
void shouldUseSpecificAlgorithm() {
  BookService spy = spy(bookService);
  spy.processBooks();
  verify(spy).internalSortMethod(); // Testing implementation
}

// Good: Testing WHAT it produces
@Test
void shouldReturnBooksSortedByTitle() {
  List<Book> books = bookService.processBooks();
  assertThat(books)
    .extracting(Book::getTitle)
    .isSortedAccordingTo(String::compareTo);
}
```

Test the outcome (sorted books), not the internal steps (which sort method was used).

## The Boss's Weakness: Test Data Builders

Creating test data gets repetitive:

```java
Book book1 = new Book();
book1.setTitle("Test Book 1");
book1.setAuthor("Test Author 1");
book1.setIsbn("978-1234567890");
book1.setPublishedDate(LocalDate.now());
book1.setStatus(BookStatus.AVAILABLE);

Book book2 = new Book();
book2.setTitle("Test Book 2");
// ... repeat for every test
```

The Test Data Builder pattern eliminates this duplication:

```java
class BookTestDataBuilder {
  private String title = "Default Title";
  private String author = "Default Author";
  private String isbn = "978-0000000000";
  private LocalDate publishedDate = LocalDate.now();
  private BookStatus status = BookStatus.AVAILABLE;

  public BookTestDataBuilder withTitle(String title) {
    this.title = title;
    return this;
  }

  public BookTestDataBuilder withAuthor(String author) {
    this.author = author;
    return this;
  }

  public BookTestDataBuilder withIsbn(String isbn) {
    this.isbn = isbn;
    return this;
  }

  public Book build() {
    Book book = new Book();
    book.setTitle(title);
    book.setAuthor(author);
    book.setIsbn(isbn);
    book.setPublishedDate(publishedDate);
    book.setStatus(status);
    return book;
  }
}
```

Now tests become concise:

```java
@Test
void shouldProcessAvailableBooks() {
  Book book = new BookTestDataBuilder()
    .withTitle("Clean Code")
    .withStatus(BookStatus.AVAILABLE)
    .build();

  // Test uses only the book
}
```

Only specify what matters for each test. Everything else uses sensible defaults.

## Boss Defeated: Victory Rewards

Congratulations! You've defeated the Unit Testing Guardian.

**Rewards Unlocked:**
- ⚡ **Lightning-fast feedback**: Unit tests run in milliseconds
- 🎯 **Precise failure detection**: Know exactly what broke
- 🛡️ **Solid foundation**: Base for all other testing
- 📚 **Living documentation**: Tests show how code should work

**Skills Acquired:**
- Writing isolated tests with JUnit 5
- Creating mocks with Mockito
- Fluent assertions with AssertJ
- Parameterized testing
- The AAA pattern
- Test data builders

## Moving Forward

Unit tests are your foundation, but they have limits:

**Unit Tests Can't Verify:**
- Database queries actually work
- HTTP endpoints are mapped correctly
- JSON serialization is configured properly
- Spring Security protects endpoints
- Components integrate correctly

For these challenges, we need a different approach.

The next chamber of the maze awaits, guarded by a more complex foe: **The Sliced Testing Hydra**.

This multi-headed beast requires understanding Spring Boot's test slice annotations: `@WebMvcTest`, `@DataJpaTest`, `@JsonTest`, and more. Each head demands a different strategy.

But you're ready. You have your weapons, you understand isolation, and you know how to write effective tests.

**Next up: Chapter 2 - Boss Fight #2: The Sliced Testing Hydra**
