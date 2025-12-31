package de.rieckpil.blog.examples.chapter4;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.CONCURRENT)
class ParallelTest {
  // Tests in this class run in parallel
}
