package de.rieckpil.blog.examples.chapter4;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;

class DatabaseTest {

  @Test
  @ResourceLock(value = "database", mode = ResourceAccessMode.READ)
  void readOnlyTest() {
    // Multiple tests can hold READ locks simultaneously
  }

  @Test
  @ResourceLock(value = "database", mode = ResourceAccessMode.READ_WRITE)
  void modifyingTest() {
    // Exclusive access - no other test runs
  }
}
