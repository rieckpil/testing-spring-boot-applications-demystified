package de.rieckpil.blog.examples.chapter4;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)
class SequentialTest {
  // Tests in this class run sequentially
}
