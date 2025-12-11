package de.rieckpil.blog.examples.chapter1;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;

class JUnitExtensionsTest {

  @Target({ElementType.TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @ExtendWith(ConfigurableSlowTestDetector.class)
  @interface SlowTestThreshold {
    long value() default 100;
  }

  static class SlowTestDetector implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private final long thresholdMs;

    SlowTestDetector() {
      this(100);
    }

    SlowTestDetector(long thresholdMs) {
      this.thresholdMs = thresholdMs;
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
      getStore(context).put("start", System.currentTimeMillis());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
      long start = getStore(context).remove("start", Long.class);
      long duration = System.currentTimeMillis() - start;

      if (duration > thresholdMs) {
        System.out.println("\n⚠️ SLOW TEST DETECTED ⚠️");
        System.out.println("Test: " + context.getDisplayName());
        System.out.println("Duration: " + duration + "ms");
        System.out.println("Threshold: " + thresholdMs + "ms\n");
      }
    }

    private ExtensionContext.Store getStore(ExtensionContext context) {
      return context.getStore(
          ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod()));
    }
  }

  static class ConfigurableSlowTestDetector
      implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    @Override
    public void beforeTestExecution(ExtensionContext context) {
      getStore(context).put("start", System.currentTimeMillis());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
      long start = getStore(context).remove("start", Long.class);
      long duration = System.currentTimeMillis() - start;

      long threshold = getThreshold(context);

      if (duration > threshold) {
        System.out.println("\n⚠️ SLOW TEST DETECTED ⚠️");
        System.out.println("Test: " + context.getDisplayName());
        System.out.println("Duration: " + duration + "ms");
        System.out.println("Threshold: " + threshold + "ms\n");
      }
    }

    private long getThreshold(ExtensionContext context) {
      SlowTestThreshold annotation =
          context.getRequiredTestMethod().getAnnotation(SlowTestThreshold.class);

      if (annotation == null) {
        annotation = context.getRequiredTestClass().getAnnotation(SlowTestThreshold.class);
      }

      return annotation != null ? annotation.value() : 100;
    }

    private ExtensionContext.Store getStore(ExtensionContext context) {
      return context.getStore(
          ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod()));
    }
  }

  @Nested
  @ExtendWith(SlowTestDetector.class)
  @DisplayName("Tests with class-level extension")
  class ClassLevelExtensionTests {

    @Test
    @DisplayName("Fast test that should not trigger the slow test warning")
    void shouldExecuteQuicklyWithoutWarning() {
      int result = 1 + 1;
      assertThat(result).isEqualTo(2);
    }

    @Test
    @DisplayName("Slow test that should trigger the slow test warning")
    void shouldTriggerSlowTestWarning() throws InterruptedException {
      Thread.sleep(150);
      int result = 1 + 1;
      assertThat(result).isEqualTo(2);
    }
  }

  @Nested
  class MethodLevelExtensionTests {

    @Test
    @DisplayName("Fast test without extension")
    void shouldRunWithoutExtension() {
      int result = 1 + 1;
      assertThat(result).isEqualTo(2);
    }

    @Test
    @ExtendWith(SlowTestDetector.class)
    @DisplayName("Slow test with method-level extension")
    void shouldDetectSlowTestWithMethodExtension() throws InterruptedException {
      Thread.sleep(150);
      int result = 1 + 1;
      assertThat(result).isEqualTo(2);
    }
  }

  @Nested
  class RegisterExtensionTests {

    @RegisterExtension static SlowTestDetector customDetector = new SlowTestDetector(200);

    @Test
    @DisplayName("Test with default threshold (100ms)")
    void shouldNotTriggerWithHigherThreshold() throws InterruptedException {
      Thread.sleep(150);
      int result = 1 + 1;
      assertThat(result).isEqualTo(2);
    }

    @Test
    @DisplayName("Test with custom threshold (200ms)")
    void shouldRespectCustomThreshold() throws InterruptedException {
      Thread.sleep(150);
      int result = 1 + 1;
      assertThat(result).isEqualTo(2);
    }
  }

  @Nested
  class AnnotationConfiguredTests {

    @Test
    @SlowTestThreshold
    @DisplayName("Test with default annotation threshold (100ms)")
    void shouldUseDefaultAnnotationThreshold() throws InterruptedException {
      Thread.sleep(150);
      int result = 1 + 1;
      assertThat(result).isEqualTo(2);
    }

    @Test
    @SlowTestThreshold(300)
    @DisplayName("Test with custom annotation threshold (300ms)")
    void shouldUseCustomAnnotationThreshold() throws InterruptedException {
      Thread.sleep(150);
      int result = 1 + 1;
      assertThat(result).isEqualTo(2);
    }
  }
}
