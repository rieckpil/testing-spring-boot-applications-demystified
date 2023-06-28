# Content

## Spring Boot Testing Pitfalls


## Tips & Tricks

### Maven

### Use GitHub Actions

### Parallelizing Unit Tests with Maven and JUnit 5

The more our project and test suite grow, the longer the feedback loop becomes. Fortunately, there are techniques available to speed up our build time. One of such techniques is parallelizing our tests. Instead of running our tests in sequence, we can run them in parallel to save time. The parallelization may not work for all kinds of tests, and hence we'll learn with this article how to only parallelize our unit tests with JUnit 5 and Maven. The upcoming technique is framework independent, and we can apply it to any Java project (Spring Boot, Quarkus, Micronaut, Jakarta EE, etc.) that uses JUnit 5 (JUnit Jupiter, to be precise) and Maven.

#### Upfront Requirement: Separation of Tests

Before we jump right into the required configuration setup for parallelizing our unit tests, we first have to split our tests into at least two categories. The reason for this is to allow a separate parallelization strategy (or no parallelization at all) depending on the test category. While there are many different types of tests in the literature, one can endlessly discuss what's the correct name and category. Sticking to two basic test categories is usually sufficient: unit and integration tests. Where to draw the line between unit and integration tests is yet another discussion. In general, if a test meets the following criteria, we can usually refer to it as a unit test:

* A small unit (method or class) is tested in isolation
* The collaborators of the class under test are replaced with a fake/stub
* The test doesn't depend on infrastructure components like a database
* The test executes fast
* We can parallelize the test as there are no side effects from other tests

While this list of requirements is not exhaustive, it's a first good indicator of what a unit test is. Any test that doesn't fit in this category will be labeled an integration test. When it comes to labeling and separating our tests, we have multiple options when using JUnit and Maven. First, JUnit 5 lets us tag `@Tag("integration-test")` (former JUnit 4 categories) our test class to label them. However, when adding a new test to our project, we may forget to add these tags, and in general, it requires a little bit more maintenance effort on our end. A more pragmatic approach is to use the convenient defaults of two Maven plugins that are involved in our testing lifecycle: the Maven Surefire and Maven Failsafe Plugin. By default, the Maven Surefire plugin will run any test that has the postfix `*Test` (e.g., `CustomerServiceTest`). The Maven Failsafe Plugin, on the other hand, only executes tests with the postfix `*IT` (for integration test). We can even override these naming strategies and come up with our own postfix. Sticking to the defaults, if we add the postfix `*Test` only to our unit tests classes and `*IT` for our integration tests, we already have a separation. Both plugins run separately at a different build phase of the Maven default lifecycle. The Maven Surefire plugin executes our unit tests in the `test` phase while the Failsafe plugin gets active in the `verify` phase. For more information on both plugins and to understand how Maven is involved in testing Java applications, [head over to this article](https://rieckpil.de/maven-setup-for-testing-java-applications/).

#### Upfront Requirement: Independent Unit Tests

Another requirement we have to conform to is the independence of our unit tests. As soon as we've split up our test suite into unit and integration tests by naming them differently, we have to ensure our unit test can run in parallel. For this to work, there shouldn't be an implicit order that dictates the success or failure of our unit tests. Our unit tests should pass or fail independently of the order they were invoked. As our unit tests ran in sequence before, we may not have noticed any violation of this requirment. It's very likely that some tests fail this requirement, especially the longer our project exists. The parallelization acts as a litmus test for the independence of our unit test. If we see random test failures, we know there's something for us to work on before we can fully benefit from the test parallelization. As the independence of our tests is a best practice, it's we should revisit any test that fails to meet this requirement. Even if we decide not to parallelize them, fixing these tests is still worth the effort as they may fail randomly in the future. This makes our build less deterministic and results in frustrated developers that try to fix a critical bug while working under pressure.

#### Java JUnit 5 Test Example

For the upcoming unit test parallelization example with JUnit 5 and Maven, we're using the following sample unit test:

```java
class StringFormatterTest {

  private StringFormatter cut = new StringFormatter();

  @BeforeEach
  void artificialDelay() throws Exception {
    // delaying the test execution to see the parallelization efforts
    Thread.sleep(1000);
  }

  @Test
  void shouldUppercaseLowercaseString() {
    String input = "duke";

    String result = cut.format(input);

    assertEquals("DUKE", result);
  }

  // two more similar tests
}
```

The actual implementation that is being tested by this unit test is secondary. Our `StringFormatterTest` test class contains three unit tests that we artificially slow down with a `Thread.sleep()`. This will help us see an actual difference once we enable the parallelization of our unit tests. Running the three tests of this class in sequence takes three seconds:

```shell
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running de.rieckpil.blog.StringFormatterTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.056 s - in de.rieckpil.blog.StringFormatterTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

This is our benchmark to compare the result of running the tests in parallel. Next to this unit test, we have the following sample integration test. The test is a copy of the unit test with the postfix IT:

```java
class StringFormatterIT {

  private StringFormatter cut = new StringFormatter();

  @BeforeEach
  void artificialDelay() throws Exception {
    // delaying the test execution to see the parallelization efforts
    Thread.sleep(1000);
  }

  @Test
  void shouldUppercaseLowercaseString() {
    String input = "duke";

    String result = cut.format(input);

    assertEquals("DUKE", result);
  }

  // two more similar tests
}
```

While this is not a real integration test, it acts as a placeholder test. It helps us see that our upcoming configuration change only takes effect for the unit tests. Running all tests for this sample project takes six seconds as all tests are executed in sequence.

#### Parallelize Java Unit Tests with JUnit 5 and Maven

Now it's time to parallelize our Java unit tests with JUnit 5 and Maven. As JUnit runs our tests in sequence by default, we have to override this configuration only for our unit tests. A global JUnit 5 config file to define the parallelization won't do the job as this would also trigger parallelization for our integration tests. When using Maven and the Maven Surefire Plugin, we can add custom configurations (environment variables, system properties, etc.) specifically for the tests that are executed by the Surefire plugin. We can use this technique to pass the relevant JUnit 5 configuration parameters to start parallelizing our unit tests:

```xml
<plugin>
  <artifactId>maven-surefire-plugin</artifactId>
  <version>3.0.0-M7</version>
  <configuration>
    <properties>
      <configurationParameters>
        junit.jupiter.execution.parallel.enabled = true
        junit.jupiter.execution.parallel.mode.default = concurrent
      </configurationParameters>
    </properties>
  </configuration>
</plugin>
```

Using the configuration above to run all tests concurrently, we get the following result after running our unit tests with `./mvnw test`:

```shell
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running de.rieckpil.blog.StringFormatterTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.031 s - in de.rieckpil.blog.StringFormatterTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

The total test execution time (see the time elapsed) went down from three seconds to one second as now all three tests run in parallel. While this time improvement may seem negligible in this example, imagine the same 300% speed improvement for a bigger project. As we don't override the parallelization strategy, JUnit will fall back to the default `dynamic` parallization and parallelize by the number of available processors/cores. Per default, JUnit uses one thread per core. Hence, if we run the tests on a machine with only two cores, the build time will be two seconds as only two tests can run in parallel. That's why we might see build time differences when comparing our local build time with our build agent (e.g., GitHub Actions, Jenkins). We can override the degree to which parallelize by using either a custom, fixed, or dynamic strategy (factor x available cores):

```xml
<properties>
  <configurationParameters>
    junit.jupiter.execution.parallel.enabled = true
    junit.jupiter.execution.parallel.mode.default = concurrent
    junit.jupiter.execution.parallel.config.strategy = fixed
    junit.jupiter.execution.parallel.config.fixed.parallelism = 2
  </configurationParameters>
</properties>
```

Overriding the default parallelism configuration with the fixed configuration above, JUnit 5 will now use two threads to run our tests. This results in an overall test execution time of 2 seconds as we have three tests to execute. For more configuration options for the test parallelization, head over to the [JUnit 5 documentation](https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution).

#### Running Our Java Integration Tests In Sequence

The previous Maven Surefire Plugin configuration only propagates the JUnit 5 configuration for the unit tests. Hence for the Maven Failsafe Plugin that executes our integration tests, we can run the tests in sequence by not specifying any parallelism config:

```xml
<plugin>
  <artifactId>maven-failsafe-plugin</artifactId>
  <version>3.0.0-M7</version>
  <executions>
    <execution>
      <goals>
        <goal>integration-test</goal>
        <goal>verify</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

The `configurationParameters` from the Surefire Plugin are not shared, and hence we can isolate the configuration for both test executions. If our integration test suite allows for parallel test execution, we can even configure different parallelism and concurrent execution. We may want to only parallelize on an integration test class level and run the test methods in sequence. This can be achieved with the following configuration:

```
junit.jupiter.execution.parallel.enabled = true
junit.jupiter.execution.parallel.mode.default = same_thread
junit.jupiter.execution.parallel.mode.classes.default = concurrent
```

As soon as both our unit and integration tests share the same parallelism config, we may rather prefer a single  `junit-platform.properties` file to avoid the duplicated config for two Maven plugins. This file has to be at the root of our classpath, and hence we can store it within `src/test/resources`.

#### Conclusion of Parallelizing Java Unit Tests with JUnit 5 and Maven

Parallelizing our Java unit tests with JUnit 5 and Maven is a simple technique to speed up our Maven build. The parallelization feature of JUnit 5 allows for a fine-grained parallelization configuration. By separating our tests into two categories and by using two different Maven plugins, we can isolate the parallelization setup. Depending on how many existing unit tests we already have, parallelizing them may take some initial setup effort. Not all tests may have been written to be run in parallel in any order. Fixing them is worth the effort. A sample JUnit 5 Maven project with this parallelization setup is [available on GitHub](https://github.com/rieckpil/blog-tutorials/tree/master/maven-junit-paralellize-tests).

### Run Java Tests With Maven Silently (Only Log on Failure)

When running our Java tests with Maven they usually produce a lot of noise in the console. While this log output can help understand test failures, it's typically superfluous when our test suite is passing. Nobody will take a look at the entire output if the tests are green. It's only making the build logs more bloated. A better solution would be to run our tests with Maven silent with no log output and only dump the log output once the tests fail. This blog post demonstrates how to achieve this simple yet convenient technique to run Java tests with Maven silently for a compact and followable build log.

#### The Status Quo: Noisy Java Tests

Based on our logging configuration, our tests produce quite some log output. When running our entire test suite locally or on a CI server (e.g., GitHub Actions or Jenkins), analyzing a test failure is tedious when there's a lot of noise in the logs. We first have to find our way to the correct position by scrolling or using the search functionality of, e.g., our browser or the integrated terminal of our IDE. A demo output for a test that verifies email functionality using [GreenMail](https://rieckpil.de/use-greenmail-for-spring-mail-javamailsender-junit-5-integration-tests/) looks like the following:

```
06:52:17.982 [smtp:127.0.0.1:3025<-/127.0.0.1:53348] INFO  c.i.greenmail.user.UserManager - Created user login mike@java.io for address mike@java.io with password mike@java.io because it didn't exist before.
06:52:18.024 [smtp:127.0.0.1:3025<-/127.0.0.1:53350] INFO  c.i.greenmail.user.UserManager - Created user login mike@java.io for address mike@java.io with password mike@java.io because it didn't exist before.
```

There's usually a lot of default noise of frameworks and test libraries that add up to quite some log output when running an entire test suite:

```
[INFO] --- maven-surefire-plugin:3.0.0-M5:test (default-test) @ spring-boot-example ---
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running de.rieckpil.blog.greenmail.MailServiceTest
06:55:24.185 [smtp:127.0.0.1:3025<-/127.0.0.1:53406] INFO  c.i.greenmail.user.UserManager - Created user login mike@java.io for address mike@java.io with password mike@java.io because it didn't exist before.
06:55:24.225 [smtp:127.0.0.1:3025<-/127.0.0.1:53408] INFO  c.i.greenmail.user.UserManager - Created user login mike@java.io for address mike@java.io with password mike@java.io because it didn't exist before.
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.115 s - in de.rieckpil.blog.greenmail.MailServiceTest
[INFO] Running de.rieckpil.blog.junit5.RegistrationWebTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0 s - in de.rieckpil.blog.junit5.RegistrationWebTest
[INFO] Running de.rieckpil.blog.junit5.ExtensionExampleTest
Injected random UUID: 6aad64dd-0d22-4681-95ca-d254fa53c3ac
Injected random UUID: b3c44f26-7000-4998-9de8-c2b353098e72
Injected random UUID: b75d8d0e-7ac2-4f2c-936c-7d768b7db2cc
Injected random UUID: e721908f-a4ef-4938-883d-f2f614534b37
Injected random UUID: e4ead0b5-af7b-40c0-8c9a-459f4ebfcf15
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.009 s - in de.rieckpil.blog.junit5.ExtensionExampleTest
[INFO] Running de.rieckpil.blog.junit5.JUnit5ExampleTest
Will run only once before all tests of this class
Will run before each test
Will run before after each test
Will run before each test
Will run before after each test
Will run before each test
Will run before after each test
Will run before each test
Will run before after each test
Will run before each test
Will run before after each test
Will run only once after all tests of this class
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.022 s - in de.rieckpil.blog.junit5.JUnit5ExampleTest
[INFO] Running de.rieckpil.blog.wiremock.WireMockSetupTest
06:55:28.930 [main] INFO  org.eclipse.jetty.util.log - Logging initialized @6609ms to org.eclipse.jetty.util.log.Slf4jLog
06:55:28.996 [main] INFO  org.eclipse.jetty.server.Server - jetty-9.4.44.v20210927; built: 2021-09-27T23:02:44.612Z; git: 8da83308eeca865e495e53ef315a249d63ba9332; jvm 11.0.11+9-LTS
06:55:29.004 [main] INFO  o.e.j.server.handler.ContextHandler - Started o.e.j.s.ServletContextHandler@5df54296{/__admin,null,AVAILABLE}
06:55:29.005 [main] INFO  o.e.j.server.handler.ContextHandler - Started o.e.j.s.ServletContextHandler@64a7ad02{/,null,AVAILABLE}
06:55:29.010 [main] INFO  o.e.jetty.server.AbstractConnector - Started NetworkTrafficServerConnector@20960b51{HTTP/1.1, (http/1.1)}{0.0.0.0:53424}
06:55:29.011 [main] INFO  org.eclipse.jetty.server.Server - Started @6690ms
06:55:29.015 [main] INFO  o.e.jetty.server.AbstractConnector - Stopped NetworkTrafficServerConnector@20960b51{HTTP/1.1, (http/1.1)}{0.0.0.0:0}
06:55:29.015 [main] INFO  o.e.j.server.handler.ContextHandler - Stopped o.e.j.s.ServletContextHandler@64a7ad02{/,null,STOPPED}
06:55:29.015 [main] INFO  o.e.j.server.handler.ContextHandler - Stopped o.e.j.s.ServletContextHandler@5df54296{/__admin,null,STOPPED}
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.194 s - in de.rieckpil.blog.wiremock.WireMockSetupTest
[INFO] Running de.rieckpil.blog.wiremock.JavaHttpClientTest
06:55:29.019 [main] INFO  org.eclipse.jetty.server.Server - jetty-9.4.44.v20210927; built: 2021-09-27T23:02:44.612Z; git: 8da83308eeca865e495e53ef315a249d63ba9332; jvm 11.0.11+9-LTS
06:55:29.020 [main] INFO  o.e.j.server.handler.ContextHandler - Started o.e.j.s.ServletContextHandler@5ff29e8b{/__admin,null,AVAILABLE}
06:55:29.020 [main] INFO  o.e.j.server.handler.ContextHandler - Started o.e.j.s.ServletContextHandler@7acfcfc4{/,null,AVAILABLE}
06:55:29.021 [main] INFO  o.e.jetty.server.AbstractConnector - Started NetworkTrafficServerConnector@736f8837{HTTP/1.1, (http/1.1)}{0.0.0.0:53425}
06:55:29.021 [main] INFO  org.eclipse.jetty.server.Server - Started @6700ms
06:55:29.067 [qtp1974219375-82] INFO  o.e.j.s.handler.ContextHandler.ROOT - RequestHandlerClass from context returned com.github.tomakehurst.wiremock.http.StubRequestHandler. Normalized mapped under returned 'null'
06:55:29.087 [qtp1974219375-82] INFO  o.e.j.s.h.ContextHandler.__admin - RequestHandlerClass from context returned com.github.tomakehurst.wiremock.http.AdminRequestHandler. Normalized mapped under returned 'null'
06:55:31.189 [main] INFO  o.e.jetty.server.AbstractConnector - Stopped NetworkTrafficServerConnector@736f8837{HTTP/1.1, (http/1.1)}{0.0.0.0:0}
06:55:31.189 [main] INFO  o.e.j.server.handler.ContextHandler - Stopped o.e.j.s.ServletContextHandler@7acfcfc4{/,null,STOPPED}
06:55:31.189 [main] INFO  o.e.j.server.handler.ContextHandler - Stopped o.e.j.s.ServletContextHandler@5ff29e8b{/__admin,null,STOPPED}
```

While we could tweak our logger configuration and set the log level to `ERROR` for the framework and libraries logs, their `INFO` can still be quite relevant when analyzing a test failure. When scrolling through the log output of passing tests, we might also see stack traces and exceptions that are intended but might confuse newcomers as they wonder if something went wrong there. Having a clean build log without much noise would better help us follow the current build. The bigger our test suite, the more we have to scroll. If all tests pass, why pollute the console with log output from the tests? Our Maven build might also fail for different reasons than test failures, e.g., a [failing OWASP dependency check or a dependency convergence issue](https://rieckpil.de/top-3-maven-plugins-to-ensure-quality-and-security-for-your-project/). Getting fast to the root cause of the build failure is much simpler with a compact build log.

#### The Goal: Run Tests with Maven Silently

Our goal for this optimization is to have a compact Maven build log and only log the test output if it's really necessary (aka. tests are failing). Gradle is doing this already by default. When running tests with Gradle, we'll only see a test summary after running our tests. There's no intermediate noise inside our console. The goal is to achieve a somehow similar behavior as Gradle and run our tests silently. If they're passing, we're fine, and there's (usually) no need to investigate the log outcome of our tests. If one of our tests fails, report the build log to the console to analyze the test failure. In short, with our target solution, we have two scenarios:

* No log output for tests in the console when all tests pass
* Print the log output of our tests when a test fails

The second scenario should be (hopefully) less likely. Hence most of our Maven builds should result in a compact and clean build log. We're fine with the log noise if there's a failure, as it helps us understand what went wrong. Let's see how we can achieve this with the least amount of configuration.

#### The Solution: Customized Maven Setup

As a first step, we configure the desired log level for testing:

```xml
<configuration>
  <include resource="/org/springframework/boot/logging/logback/base.xml"/>
  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
      </pattern>
    </encoder>
  </appender>
  <root level="INFO">
    <appender-ref ref="STDOUT"/>
  </root>
</configuration>
```

We're using Logback (any logger works) and log any `INFO` (and above) statement to the console for the example above. We don't differentiate between our application's log and framework or test libraries. Next comes the important configuration that'll make our test silent. The [Maven Surefire (unit tests) and the Failsafe (integration tests)](https://rieckpil.de/maven-setup-for-testing-java-applications/) plugin allow redirecting the console output to a file. We won't see any test log output in the console with this configuration as it's stored within a file:

```xml
<plugin>
  <artifactId>maven-surefire-plugin</artifactId>
  <version>3.0.0-M5</version>
  <configuration>
    <redirectTestOutputToFile>true</redirectTestOutputToFile>
    <reportsDirectory>${project.build.directory}/test-reports</reportsDirectory>
  </configuration>
</plugin>
```

When activating this functionality (`redirectTestOutputToFile`), both plugins create an output file inside the target folder for each test class with the naming scheme `TestClassName-output.txt`. We can override the location of the output files using the `reportsDirectory` configuration option. Overriding this location helps us store the output of the Surefire and Failsafe plugin at the same place:

```xml
<plugin>
  <artifactId>maven-failsafe-plugin</artifactId>
  <version>3.0.0-M5</version>
  <executions>
    <execution>
      <goals>
        <goal>integration-test</goal>
        <goal>verify</goal>
      </goals>
    </execution>
  </executions>
  <configuration>
    <redirectTestOutputToFile>true</redirectTestOutputToFile>
    <reportsDirectory>${project.build.directory}/test-reports</reportsDirectory>
  </configuration>
</plugin>
```

This configuration for bot the Surefire and Failsafe plugin will mute our test runs, and Maven will only display a test execution summary for each test class:

```
[INFO] --- maven-surefire-plugin:3.0.0-M5:test (default-test) @ spring-boot-example ---
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running de.rieckpil.blog.assertj.AssertJTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.397 s - in de.rieckpil.blog.assertj.AssertJTest
[INFO] Running de.rieckpil.blog.hamcrest.HamcrestTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.149 s - in de.rieckpil.blog.hamcrest.HamcrestTest
[INFO] Running de.rieckpil.blog.citrus.CitrusDemoTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.583 s - in de.rieckpil.blog.citrus.CitrusDemoTest
[INFO] Running de.rieckpil.blog.xmlunit.XMLUnitTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.189 s - in de.rieckpil.blog.xmlunit.XMLUnitTest
[INFO] Running de.rieckpil.blog.MyFirstTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0 s - in de.rieckpil.blog.MyFirstTest
[INFO] Running de.rieckpil.blog.jsonpath.JsonPayloadTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.034 s - in de.rieckpil.blog.jsonpath.JsonPayloadTest
```

This compact build log makes it even fun to watch the test execution (assuming there are no flaky tests). After running our tests, we can take a look at the content of the `test-reports` folder:

```
target
`-- test-reports
    |-- de.rieckpil.blog.MyFirstTest.txt
    |-- de.rieckpil.blog.assertj.AssertJTest.txt
    |-- de.rieckpil.blog.citrus.CitrusDemoTest-output.txt
    |-- de.rieckpil.blog.citrus.CitrusDemoTest.txt
    |-- de.rieckpil.blog.greenmail.MailServiceTest-output.txt
    |-- de.rieckpil.blog.greenmail.MailServiceTest.txt
    |-- de.rieckpil.blog.hamcrest.HamcrestTest.txt
    |-- de.rieckpil.blog.jsonassert.JSONAssertTest.txt
    |-- de.rieckpil.blog.jsonpath.JsonPayloadTest.txt
    |-- de.rieckpil.blog.junit5.ExtensionExampleTest-output.txt
    |-- de.rieckpil.blog.junit5.ExtensionExampleTest.txt
    |-- de.rieckpil.blog.junit5.JUnit5ExampleTest-output.txt
```

For each test class, we'll find (at least) one text file that contains the test summary as we saw it in the build log. If the test prints output to the console, there'll be a `-output.txt` file with the content:

*   `de.rieckpil.blog.greenmail.MailServiceTest-output.txt`: All console output of the test
*   `de.rieckpil.blog.greenmail.MailServiceTest.txt`: The test summary, as seen in the build log

What's left is to extract the content of all our `*-output.txt` files if our build is failing. As long as our tests are all green, we can ignore the content of the output files. In case of a test failure, we must become active and dump the file contents to the console. For this purpose, we're using a combination of `find` and `tail`.For demonstration purposes, we'll use GitHub Actions. However, the present solution is portable to any other build server that provides functionality to detect a build failure and execute shell commands:

```yaml
name: Maven Build
on: [ push, pull_request ]
jobs:
  build-project:
    runs-on: ubuntu-20.04
    name: Build sample project
    steps:
      - name: Checkout code
        uses: actions/checkout@v2

      - name: Set up JDK 11
        uses: actions/setup-java@v2
        with:
          java-version: 11.0
          distribution: 'adopt'
          java-package: 'jdk'

      # ... more steps including running ./mvnw verify

      - name: Log test output on failure
        if: failure() || cancelled()
        run: find . -type f -path "*test-reports/*-output.txt" -exec tail -n +1 {} +
```

As part of the last step of our build workflow, we find all `*-output.txt` files and print their content. We only print the content of the test output files in case of a failure. With GitHub Actions, we can conditionally execute a step using a boolean expression: `if: failure() || cancelled()`. Both `failure()` and `cancelled()` are built-in functions of GitHub Actions. Every other CI server provides some similar functionality. We include `cancelled()` to the expression to cover the scenario when our test suite is stuck and we manually stop (aka. cancel) the build. If the build is passing, this last logging step is skipped, and no test log output is logged. By using `tail -n +1 {}` we print the file name before dumping its content to the console. This helps search for the failed test class to start the investigation:

```
==> ./target/test-reports/de.rieckpil.blog.greenmail.MailServiceTest-output.txt <==
06:35:17.474 [smtp:127.0.0.1:3025<-/127.0.0.1:64642] INFO  c.i.greenmail.user.UserManager - Created user login mike@java.io for address mike@java.io with password mike@java.io because it didn't exist before.
06:35:17.547 [smtp:127.0.0.1:3025<-/127.0.0.1:64644] INFO  c.i.greenmail.user.UserManager - Created user login mike@java.io for address mike@java.io with password mike@java.io because it didn't exist before.
```

#### Summary: Silent and Compact Build Logs

We'll get compact Maven build logs with this small tweak to the Maven Surefire and Failsafe plugin and the additional step inside our build server. No more noisy test runs. We won't lose any test log output as we temporarily park it inside files and inspect the files if a test fails. This configuration will only affect the way our tests are run with Maven. We can still see the console output when executing tests within our IDE. We'll capture any test console output with this mechanism, both from logging libraries and plain `System.out.println` calls. This technique also works when running our tests in parallel. However, if we parallelize the test methods, the console statements may be out of order inside the test output file. If you want to see this technique in action for a public repository, take a look at the [Java Testing Toolbox](https://rieckpil.de/testing-tools-and-libraries-every-java-developer-must-know/) repository [on GitHub](https://github.com/rieckpil/java-testing-ecosystem). As part of the [main GitHub Actions workflow](https://github.com/rieckpil/java-testing-ecosystem/actions) that builds the project(s) with Maven, you'll see the Java tests being run silently. If there's a build failure, you'll see the content of the test output files as one of the last jobs.

### Five Lesser-Known JUnit 5 Features

Writing your first test with JUnit 5 is straightforward. Annotate your test method with `@Test` and verify the result using assertions. Apart from the basic testing functionality of JUnit 5, there are some features you might not have heard about (yet). Discover five JUnit 5 features I found useful while working with JUnit 5: test execution order, nesting tests, parameter injection, parallelizing tests, and conditionally run tests.

#### Test Execution Ordering

The first feature we'll explore is influencing the test execution order. While JUnit 5 has the following default-test execution order:

> ..., test methods will be ordered using an algorithm that is deterministic but intentionally nonobvious. This ensures that subsequent runs of a test suite execute test methods in the same order, thereby allowing for repeatable builds.

you can configure a different ordering mechanism. Therefore, either implement your own `MethodOrderer` or use a built-in that orders: alphabetically, randomly, or numerically based on a specified value. Let's take a look at how to order some unit tests using the `OrderAnnotation` ordering mechanism:

```
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderedExecutionTest {

  @Test
  @Order(2)
  void testTwo() {
    System.out.println("Executing testTwo");
    assertEquals(4, 2 + 2);
  }

  @Test
  @Order(1)
  void testOne() {
    System.out.println("Executing testOne");
    assertEquals(4, 2 + 2);
  }

  @Test
  @Order(3)
  void testThree() {
    System.out.println("Executing testThree");
    assertEquals(4, 2 + 2);
  }
}
```

With `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)` you basically opt-out from the JUnit 5 default ordering. The actual order is then specified with `@Order`. A lower value implies a higher priority. Once you execute the tests above, the order is the following: `testOne`, `testTwo` and `testThree`. As a general best practice your test should not rely on the order they are executed. Nevertheless, there are scenarios where this feature helps to execute the tests in configurable order.

#### Nesting Tests With JUnit 5

Usually, you test different business requirements inside the same test class. With Java and JUnit 5, you write them one after the other and add new tests to the bottom of the class. While this is working for a small number of tests inside a class, this approach gets harder to manage for bigger test suites. Consider you want to adjust tests that verify a common scenario. As there is no defined order or grouping inside your test class you end up scrolling and searching them. The following JUnit 5 feature allows you to counteract this pain point of a growing test suite: nested tests. You can use this feature to group tests that verify common functionality. This does not only improves maintainability but also reduces the time to understand what the class under test is responsible for:

```java
class NestedTest {

  @Nested
  @DisplayName("Testing division functionality")
  class DivisionTests {

    @Test
    void shouldDivideByTwo() {
      assertEquals(4, 8 / 2);
    }

    @Test
    void shouldThrowExceptionForDivideByZero() {
      assertThrows(ArithmeticException.class, () -> {
        int result = 8 / 0;
      });
    }
  }

  @Nested
  @DisplayName("Testing addition functionality")
  class AdditionTests {

    @Test
    void shouldAddTwo() {
      assertEquals(4, 2 + 2);
    }

    @Test
    void shouldAddZero() {
      assertEquals(2, 2 + 0);
    }
  }

}
```

You might run into [issues](https://github.com/spring-projects/spring-framework/issues/19930) while using this feature in conjunction with some Spring Boot test features.

#### Parameter Injection With JUnit 5

JUnit 5 offers parameter injection for test constructor and method arguments. There are built-in parameter resolvers you can use to inject an instance of `TestReport`, `TestInfo`, or `RepetitionInfo` (in combination with a repeated test):

```java
@RepeatedTest(5)
void testMethodName(TestInfo testInfo, TestReporter testReporter, RepetitionInfo repetitionInfo) {
  System.out.println(testInfo.getTestMethod().get().getName());
  testReporter.publishEntry("secretMessage", "JUnit 5");
  System.out.println(repetitionInfo.getCurrentRepetition() + " from " + repetitionInfo.getTotalRepetitions());
}
```

Furthermore, you can implement your own `ParameterResolver` to resolve arguments of any type. We can use this mechanism to resolve a random `UUID` for our tests:

```java
public class RandomUUIDParameterResolver implements ParameterResolver {

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.PARAMETER)
  public @interface RandomUUID {
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return parameterContext.isAnnotated(RandomUUID.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return UUID.randomUUID().toString();
  }

}
```

With `supportsParameter` we indicate that this resolver is capable of resolving a requested parameter, as you can have multiple `ParameterResolver` . We're checking if the requested parameter is annotated with our custom annotation. In addition, you could also verify that the parameter is of type `String`. We can use this resolver for our tests once we register this extension with `@ExtendWith`:

```java
@ExtendWith(RandomUUIDParameterResolver.class)
public class ParameterInjectionTest {

  @RepeatedTest(5)
  public void testUUIDInjection(@RandomUUID String uuid) {
    System.out.println("Random UUID: " + uuid);
  }
}
```

#### Test Parallelization With JUnit 5

While you might have configured this in the past with the corresponding Maven or Gradle plugin, you can now configure this as an experimental feature with JUnit (since version 5.3). This gives you more fine-grain control on how to parallelize the tests. A basic configuration can look like the following:

```
junit.jupiter.execution.parallel.enabled = true
junit.jupiter.execution.parallel.mode.default = concurrent
```

This enables parallel execution for all your tests and set the execution mode to `concurrent`. Compared to `same_thread`,  `concurrent` does not enforce to execute the test in the same thread of the parent. For a per test class or method mode configuration, you can use the `@Execution` annotation. There are [multiple ways to set these configuration values](https://junit.org/junit5/docs/current/user-guide/#running-tests-config-params), one is to use the Maven Surefire plugin for it:

```xml
<plugin>
  <artifactId>maven-surefire-plugin</artifactId>
  <version>2.22.2</version>
  <configuration>
    <properties>
      <configurationParameters>
        junit.jupiter.execution.parallel.enabled = true
        junit.jupiter.execution.parallel.mode.default = concurrent
      </configurationParameters>
    </properties>
  </configuration>
</plugin>
```

or a `junit-platform.properties` file inside `src/test/resources` with the configuration values as content. You should benefit the most when using this feature for unit tests. Enabling parallelization for integration tests might not be possible or easy to achieve depending on your setup. Therefore, I can recommend executing your unit tests with the Maven Surefire plugin and configure parallelization for them. All your integration tests can then be executed with the Maven Failsafe plugin where you don't specify these JUnit 5 configuration parameters. For more fine-grain parallelism configuration, take a look at the official [JUnit 5 documentation](https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution).

#### Conditionally Disable Tests

The last feature is useful when you want to avoid tests being executed based on different conditions. There might be tests that don't run on different operating systems or require different environment variables to be present. JUnit 5 comes with some built-in conditions that you can use for your tests, e.g.:

```
@Test
@DisabledOnOs(OS.LINUX)
void disabledOnLinux() {
  assertEquals(42, 40 + 2);
}

@Test
@DisabledIfEnvironmentVariable(named = "FLAKY_TESTS", matches = "false")
void disableFlakyTest() {
  assertEquals(42, 40 + 2);
}
```

On the other side, writing a custom condition is pretty straightforward. Let's consider you don't want to execute a test around midnight:

```java
@Test
@DisabledOnMidnight
void disabledOnMidNight() {
  assertEquals(42, 40 + 2);
}
```

All you have to do is to implement `ExecutionCondition` and add your own condition:

```java
public class DisabledOnMidnightCondition implements ExecutionCondition {

  private static final ConditionEvaluationResult ENABLED_BY_DEFAULT =
    enabled("@DisabledOnMidnight is not present");

  private static final ConditionEvaluationResult ENABLED_DURING_DAYTIME =
    enabled("Test is enabled during daytime");

  private static final ConditionEvaluationResult DISABLED_ON_MIDNIGHT =
    disabled("Disabled as it is around midnight");

  @Documented
  @Target({ElementType.TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @ExtendWith(DisabledOnMidnightCondition.class)
  public @interface DisabledOnMidnight {
  }

  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
    Optional<DisabledOnMidnight> optional = findAnnotation(context.getElement(), DisabledOnMidnight.class);
    if (optional.isPresent()) {
      LocalDateTime now = LocalDateTime.now();
      if (now.getHour() == 23 || now.getHour() <= 1) {
        return DISABLED_ON_MIDNIGHT;
      } else {
        return ENABLED_DURING_DAYTIME;
      }
    }
    return ENABLED_BY_DEFAULT;
  }
}
```

There is a dedicated [testing category](https://rieckpil.de/category/other/testing/) available on my blog for more content about JUnit and topics like [Testcontainers](https://rieckpil.de/?s=testcontainers), testing Spring Boot or Jakarta EE applications, etc. You can find the [source code](https://github.com/rieckpil/blog-tutorials/tree/master/five-unknown-junit-5-features) for these five JUnit 5 features on GitHub.

## Recipes

### Testing the Web Layer

### Testing the Database Layer

### Testing HTTP Clients

### Writing End-to-End Tests
