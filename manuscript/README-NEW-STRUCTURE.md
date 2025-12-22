# Testing Spring Boot Applications Demystified - New Structure

## Overview

The book has been restructured to follow a "Testing Maze" hero's journey theme, matching the "Testing Spring Boot Applications Demystified" webinar presentation.

## Book Structure

The book is now organized as a progressive journey through the testing maze:

### Chapter 0: The Great Entrance (00-the-great-entrance.md)
**Theme:** Welcome to the Testing Maze

- Introduction to the testing challenge
- The Friday Afternoon Deployment Test (North Star)
- The testing Swiss Army knife (`spring-boot-starter-test`)
- Preview of 3 bosses and 3 quest items
- The testing pyramid mental model

**Estimated Pages:** ~15

---

### Chapter 1: Boss Fight #1 - Unit Testing Guardian (01-unit-testing-boss.md)
**Theme:** Testing without Spring

- Pure unit testing fundamentals
- JUnit 5, Mockito, AssertJ
- AAA (Arrange-Act-Assert) pattern
- Real examples: PriceCalculator, ShoppingCart
- Parameterized tests
- Test data builders
- What NOT to test

**Rewards:**
- ⚡ Lightning-fast feedback
- 🎯 Precise failure detection
- 🛡️ Solid foundation

**Estimated Pages:** ~18

---

### Chapter 2: Boss Fight #2 - Sliced Testing Hydra (02-sliced-testing-hydra.md)
**Theme:** Testing Spring components with focused contexts

- Why unit tests fail for Spring components (6 things you miss)
- `@WebMvcTest` for controllers with MockMvc
- `@DataJpaTest` for repositories with TestEntityManager
- Security testing with `@WithMockUser`
- Testcontainers for real databases
- Choosing the right slice

**Quest Item Unlocked:**
- ⚡ **Lightning Shield** - Fast feedback from test slices

**Estimated Pages:** ~24

---

### Chapter 3: Boss Fight #3 - Integration Testing Final Boss (03-integration-testing-final-boss.md)
**Theme:** Testing the complete system

- `@SpringBootTest` for full context testing
- `TestRestTemplate` for real HTTP requests
- Testcontainers for production-parity databases
- WireMock for external HTTP service mocking
- End-to-end security verification
- `BaseIntegrationTest` pattern

**Quest Item Unlocked:**
- 🔍 **Scroll of Truth** - Integration confidence

**Estimated Pages:** ~22

---

### Chapter 4: Collecting Quest Items (04-quest-items.md)
**Theme:** Performance optimization and best practices

**Quest Item #1: 🧿 The Caching Amulet**
- Spring Test context caching deep dive
- The Friday afternoon story (26 minutes → 12 minutes)
- BaseIntegrationTest pattern
- Spring Test Profiler
- Spring Boot 4's context pausing

**Quest Item #2: ⚡ Lightning Shield (Enhanced)**
- JVM forking parallelization
- Thread-based parallelization
- Tag-based selective execution
- Optimal test organization

**Quest Item #3: 🔍 Scroll of Truth (Enhanced)**
- Mutation testing with PIT
- Finding weak assertions
- Verifying tests actually test

**Anti-Patterns:**
- Over-mocking
- Testing implementation details
- Slow test suites
- Flaky tests

**Estimated Pages:** ~20

---

### Chapter 5: Exiting the Maze (05-exiting-the-maze.md)
**Theme:** Complete testing strategy and next steps

- Journey reflection and achievements
- Complete testing mental map
- The Friday Afternoon Test revisited
- Testing strategy template
- Performance optimization checklist
- Common pitfalls and solutions
- 4-week action plan
- Beyond the maze (E2E, performance, contract testing)
- Masterclass, workshops, newsletter (soft pitch)
- Quick reference appendix

**Estimated Pages:** ~22

---

## Total Estimated Length

**~100 pages** of high-density content

## Key Features

✅ **Hero's Journey Narrative**
- Maze metaphor throughout
- 3 boss fights with progressive difficulty
- 3 quest items to collect (Caching Amulet, Lightning Shield, Scroll of Truth)
- Victory rewards after each boss

✅ **High Information Density**
- 2-space code indentation
- 5-10 line code snippets
- Step-by-step explanations after each code block
- Text sandwich pattern (explanation → code → explanation)

✅ **Real Code Examples**
- All examples from Shelfie application
- `BookController`, `BookRepository`, `BookService`
- `PriceCalculator`, `ShoppingCart` for unit testing
- Complete workflows demonstrated

✅ **Webinar Alignment**
- Follows the presentation flow
- References the Friday afternoon story
- Includes context caching emphasis
- Spring Boot 4 features mentioned

✅ **Soft Teaching & Selling**
- Multiple mentions of Testing Spring Boot Applications Masterclass
- Workshop references
- Newsletter subscription prompts
- Community connection opportunities

## Building the Book

### Prerequisites

- Pandoc
- XeLaTeX
- Ghostscript (for PDF compression)

### Build Commands

```bash
# Step 1: Convert markdown to pandoc format
./convert-to-pandoc.sh

# Step 2: Generate PDF
./create-pdf-book.sh

# Or run both in sequence
./convert-to-pandoc.sh && ./create-pdf-book.sh
```

### Output

- `main-content.pdf` - Book content without cover
- `testing-spring-boot-applications-demystified.pdf` - Final book with cover

## File Organization

```
manuscript/
├── 00-the-great-entrance.md          # Chapter 0
├── 01-unit-testing-boss.md            # Chapter 1
├── 02-sliced-testing-hydra.md         # Chapter 2
├── 03-integration-testing-final-boss.md # Chapter 3
├── 04-quest-items.md                  # Chapter 4
├── 05-exiting-the-maze.md             # Chapter 5
├── changelog.md                        # Version history
├── Book.txt                            # DEPRECATED (see note inside)
└── resources/                          # Images (to be created)
    ├── testing-maze-entrance.png
    ├── unit-testing-boss.png
    ├── sliced-testing-hydra.png
    ├── integration-testing-boss.png
    ├── quest-items-chamber.png
    └── maze-exit.png
```

## Images to Create

The following images are referenced in the chapters and should be created:

1. **testing-maze-entrance.png** - The maze entrance scene
2. **unit-testing-boss.png** - The Unit Testing Guardian boss
3. **sliced-testing-hydra.png** - The multi-headed Hydra
4. **integration-testing-boss.png** - The Final Boss
5. **quest-items-chamber.png** - The artifacts chamber
6. **maze-exit.png** - The hero exiting victoriously

**Suggested Creation Method:**
- AI image generation (DALL-E, Midjourney, Stable Diffusion)
- Pixel art style or fantasy RPG aesthetic
- Consistent visual theme across all images

**Example Prompts:**
- "pixel art RPG game boss sprite, unit testing guardian, fantasy style"
- "glowing magical amulet with cache symbols, quest item, fantasy game art"
- "multi-headed hydra boss, each head different color, pixel art style"

## Content Highlights

### The North Star: Friday Afternoon Deployment Test

> "It's Friday at 6 PM. You see a pull request upgrading to Spring Boot 4. All tests are green. How confident are you clicking 'Merge' and deploying to production?"

This question frames the entire book's goal: achieving deployment confidence through comprehensive testing.

### The Testing Pyramid

```
       /\
      /  \  E2E Tests (Few)
     /----\
    /      \ Integration Tests (Some)
   /--------\
  /          \ Slice Tests (More)
 /------------\
/              \ Unit Tests (Many)
/______________\
```

### The Three Bosses

1. **Unit Testing Guardian** - Tests business logic in isolation
2. **Sliced Testing Hydra** - Tests Spring components with focused contexts
3. **Integration Testing Final Boss** - Tests the complete system

### The Three Quest Items

1. **🧿 Caching Amulet** - Context caching for 50%+ speed improvement
2. **⚡ Lightning Shield** - Fast feedback through slicing and parallelization
3. **🔍 Scroll of Truth** - Integration confidence and mutation testing

## Soft Teaching Strategy

The book includes natural, non-pushy mentions of:

- **Testing Spring Boot Applications Masterclass** - For deeper learning
- **Workshops** - For team training
- **Newsletter** - For weekly testing tips
- **Community** - For ongoing support

These are woven into the narrative at appropriate points (end of chapters, when discussing advanced topics, in the conclusion).

## Next Steps

1. ✅ Review the restructured chapters
2. ⬜ Create the 6 chapter images
3. ⬜ Build the PDF to verify formatting
4. ⬜ Adjust page count if needed (target: 80-100 pages)
5. ⬜ Final polish and editing
6. ⬜ Publish as lead magnet

## Questions or Issues?

If you encounter any issues with the build process or structure, check:

1. All markdown files are in `manuscript/` directory
2. Image placeholders exist (even if empty)
3. `convert-to-pandoc.sh` runs without errors
4. `create-pdf-book.sh` references correct file names

---

**Happy Testing! 🧪**
