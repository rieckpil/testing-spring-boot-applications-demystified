# Content

## Spring Boot Testing Pitfalls


## Tips & Tricks

### Correct Use Of Your Build Tool: Maven

Starting with a new programming language is always exciting. However, it can be overwhelming as we have to get comfortable with the language, the tools, conventions, and the general development workflow. This holds true for both developing and testing our applications. 

When testing Java applications with Maven, there are several concepts and conventions to understand: Maven lifecycles, build phases, plugins, etc. With this blog post, we'll cover the basic concepts for you to understand how testing Java applications with Maven _works_.

#### What Do We Need Maven For?

When writing applications with Java, we can't just pass our `.java` files to the JVM (Java Virtual Machine) to run our program. We first have to compile our Java source code to bytecode (`.class` files) using the Java Compiler (`javac`). 

Next, we pass this bytecode to the JVM (`java` binary on our machines) which then interprets our program and/or compiles parts of it even further to native machine code. Given this two-step process, someone has to compile our Java classes and package our application accordingly. Manually calling `javac` and passing the correct classpath is a cumbersome task. 

A build tool automates this process. As developers, we then only have to execute one command, and everything get's build automatically. The two most adopted build tools for the Java ecosystem are [Maven](https://maven.apache.org/) and [Gradle](https://gradle.org/). _Ancient devs_ might still prefer [Ant](https://ant.apache.org/), while _latest-greatest devs_ might advocate for [Bazel](https://bazel.build/) as a build tool for their Java applications. We're going to focus on Maven with this article. To build and test our Java applications, we need a [JDK](https://adoptopenjdk.net/) (Java Development Kit) installed on our machine and Maven. 

We can either [install Maven as a command-line tool](https://maven.apache.org/install.html) (i.e., place the Maven binary on our system's `PATH`) or use the portable Maven Wrapper. The Maven Wrapper is a convenient way to work with Maven without having to install it locally. It allows us to conveniently build Java projects with Maven without having to install and configure Maven as a CLI tool on our machine When creating a new Spring Boot project, for example, you might have already wondered what the `mvnw` and `mvnw.cmd` files inside the root of the project are used for. That's the Maven Wrapper (the idea is borrowed from Gradle).

#### Creating a New Maven Project

There are several ways to bootstrap a new Maven project. Most of the popular Java application frameworks offer a project bootstrapping wizard-like interface. Good examples are the [Spring Initializr](https://start.spring.io/) for new Spring Boot applications, [Quarkus](https://code.quarkus.io/), [MicroProfile](https://start.microprofile.io/). 

If we want to create a new Maven project without any framework support, we can use a [Maven Archetype](https://maven.apache.org/guides/introduction/introduction-to-archetypes.html) to create new projects. These archetypes are a project templating toolkit to generate a new Maven project conveniently. 

Maven provides a set of [default Archetypes artifacts](https://maven.apache.org/archetypes/index.html) for several purposes like a new web app, a new Maven plugin project, or a simple quickstart project. We bootstrap a new Java project from one of these Archetypes using the `mvn` command-line tool:

```
mvn archetype:generate \
    -DarchetypeGroupId=org.apache.maven.archetypes  \
    -DarchetypeArtifactId=maven-archetype-quickstart \
    -DarchetypeVersion=1.4 \
    -DgroupId=com.mycompany \
    -DartifactId=order-service
```

The skeleton projects we create with the official Maven Archetypes are a good place to start. However, some of these archetypes generate projects with outdated dependency versions like JUnit 4.11.

While it's not a big effort to manually bump the dependency version after the project initialization, having an up-to-date Maven Archetype in the first place is even better.

#### Minimal Maven Project For Testing Java Applications

As part of my [Custom Maven Archetype](https://github.com/rieckpil/custom-maven-archetypes) open-source project on GitHub, I've published a collection of useful Maven Archetypes. One of them is the `java-testing-toolkit` to create a Java Maven project with basic testing capabilities. [Creating our own Maven Archetype](https://rieckpil.de/create-your-own-maven-archetype-in-5-simple-steps/) is almost no effort. We can create a new testing playground project using this custom Maven Archetype with the following Maven command (for Linux & Mac):

```
mvn archetype:generate \
    -DarchetypeGroupId=de.rieckpil.archetypes  \
    -DarchetypeArtifactId=testing-toolkit \
    -DarchetypeVersion=1.0.0 \
    -DgroupId=com.mycompany \
    -DartifactId=order-service
```

For Windows (both PowerShell and CMD), we can use the following command to bootstrap a new project from this template:

```
mvn archetype:generate "-DarchetypeGroupId=de.rieckpil.archetypes" "-DarchetypeArtifactId=testing-toolkit" "-DarchetypeVersion=1.0.0" "-DgroupId=com.mycompany" "-DartifactId=order-service" "-DinteractiveMode=false"
```

We can adjust both `-DgroupId` and `-DartifactId` to our project's or company's preference. The generated project comes with a basic set of the [most central Java testing libraries](https://rieckpil.de/testing-tools-and-libraries-every-java-developer-must-know/). We can use it as a blueprint for our next project or explore testing Java applications with this playground. In summary, the following default configuration and libraries are part of this project shell:

* Java 11 
* JUnit Jupiter, Mockito, and Testcontainers dependencies 
* A basic unit test 
* Maven Surefire and Failsafe Plugin configuration 
* A basic `.gitignore`
* Maven Wrapper

Next, we have to ensure a JDK 11 (or higher) is on our `PATH` and also `JAVA_HOME` points to the installation folder:

```
$ java -version
openjdk version "11.0.10" 2021-01-19
OpenJDK Runtime Environment AdoptOpenJDK (build 11.0.10+9)
OpenJDK 64-Bit Server VM AdoptOpenJDK (build 11.0.10+9, mixed mode)

# Windows
$ echo %JAVA_HOME%
C:\Program Files\AdoptOpenJDK\jdk-11.0.10.9-hotspot

# Mac and Linux
$ echo $JAVA_HOME
/usr/lib/jvm/adoptopenjdk-11.0.10.9-hotspot
```

As a final verification step, we can now build and test this project with Maven:

```
$ mvn archetype:generate ... // generate the project
$ cd order-service // navigate into the folder
$ ./mvnw package // mvnw.cmd package for Windows

....

[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  3.326 s
[INFO] Finished at: 2021-06-03T08:31:11+02:00
[INFO] ------------------------------------------------------------------------
```

We can now open and import the project to our editor or IDE ([IntelliJ IDEA](https://www.jetbrains.com/idea/), [Eclipse](https://www.eclipse.org/downloads/), [NetBeans](https://netbeans.apache.org/download/index.html), [Visual Code](https://code.visualstudio.com/), etc.) to inspect the generated project in more detail.

#### Important Testing Folders and Files

First, let's take a look at the folders and files that are relevant for testing Java applications with Maven:

* `src/test/java`

This folder is the main place to add our Java test classes (`.java` files). As a general recommendation, we should try to mirror the package structure of our production code (`src/main/java`). Especially if there's a direct relationship between the test and a source class. 

The corresponding `CustomerServiceTest` for a `CustomerService` class inside the package `com.company.customer` should be placed in the same package within `src/test/java`. This improves the likelihood that our colleagues (and our future us) locate the corresponding test for a particular Java class without too many facepalms. Most of the IDEs and editors provide further support to jump to a test class. 

IntelliJ IDEA, for example, provides a shortcut (Ctrl+ Shift + T) to navigate from a source file to its test classes(s) and vice-versa.

* `src/test/resources`

As part of this folder, we store static files that are only relevant for our test. This might be a CSV file to import test customers for an integration test, a dummy JSON response for [testing our HTTP clients](https://rieckpil.de/how-to-test-java-http-client-usages-e-g-okhttp-apache-httpclient/), or a configuration file.

* `target/test-classes`

At this location, Maven places our compiled test classes (`.class` files) and test resources whenever the Maven compiler compiles our test sources. We can explicitly trigger this with `mvn test-compile` and add a `clean` if we want to remove the existing content of the entire `target` folder first. Usually, there's no need to perform any manual operations inside this folder as it contains build artifacts. Nevertheless, it's helpful to investigate the content for this folder whenever we face test failures because we, e.g., can't read a file from the classpath. Taking a look at this folder (after the Maven compiler did its work), can help understanding where a resources file ended up on the classpath.

* `pom.xml`

This is the heart of our Maven project. The abbreviation stands for **P**roject **O**bject **M**odel. Within this file, we define metadata about our project (e.g., description, artifactId, developers, etc.), which dependencies we require, and the configuration of our plugins.

#### Maven and Java Testing Naming Conventions

Next, let's take a look at the naming conventions for our test classes. We can separate our tests into two (or even more) basic categories: unit and integration test. To distinguish the tests for both of these two categories, we use different naming conventions with Maven. The Maven Surefire Plugin, more about this plugin later, is designed to run our unit tests. The following patterns are the defaults so that the plugin will detect a class as a test:

* `**/Test*.java`
* `**/*Test.java`
* `**/*Tests.java`
* `**/*TestCase.java`

So what's actually a unit test? Several smart people came up with a definition for this term. One of such smart people is [Michael Feathers](https://www.artima.com/weblogs/viewpost.jsp?thread=126923). He's turning the definition around and defines what a **unit test is not**:

> A test is not a unit test if it ...
>
> *   talks to the database
> *   communicates across the network
> *   touches the file system
> *   can't run at the same time as any of your other unit tests
> *   or you have to do special things to your environment (such as editing config files) to run it.

Kevlin Henney is also a great source of inspiration for a [definition of the term unit test](https://www.theregister.com/2007/07/28/what_are_your_units/?page=3). Nevertheless, our own definition or the definition of our coworkers might be entirely different. In the end, the actual definition is secondary as long as we're sharing the same definition within our team and talking about the same thing when referring to the term unit test. The Maven Failsafe Plugin, designed to run our integration tests, detects our integration tests by the following default patterns:

* `**/IT*.java`
* `**/*IT.java`
* `**/*ITCase.java`

We can also override the default patterns for both plugins and come up with a different naming convention. However, sticking to the defaults is recommended.

#### When Are Our Java Tests Executed?

Maven is built around the concept of build lifecycles. There are three built-in lifecycles:

* `default`: handling project building and deployment
* `clean`: project cleaning
* `site`: the creation of our project's (documentation) site

Each of the three built-in lifecycles has a list of build phases. For our testing example, the `default` lifecycle is important. The `default` lifecycle compromises a set of build phases to handle building, testing, and deploying our Java project. Each phase represents a stage in the build lifecycle with a central responsibility:

<div style="text-align: center;">![](https://rieckpil.de/wp-content/uploads/2021/06/maven-default-lifecycle-build-phases.png)</div>

In short, the several phases have the following responsibilities:

* `validate`: validate that our project setup is correct (e.g., we have the correct Maven folder structure)
* `compile`: compile our source code with `javac`
* `test`: run our unit tests
* `package`: build our project in its distributable format (e.g., JAR or WAR)
* `verify`: run our integration tests and further checks (e.g., [the OWASP dependency check](https://rieckpil.de/top-3-maven-plugins-to-ensure-quality-and-security-for-your-project/))
* `install`: install the distributable format into our local repository (`~/.m2` folder)
* `deploy`: deploy the project to a remote repository (e.g., Maven Central or a company hosted Nexus Repository/Artifactory)

These build phases represent the central phases of the `default` lifecycle. There are actually more phases. For a complete list, please refer to the [Lifecycle Reference](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html#Lifecycle_Reference) of the official Maven documentation. Whenever we execute a build phase, our project will go through all build phases and sequentially until the build phase we specified. To phrase it differently, when we run `mvn package`, for example, Maven will execute the default lifecycle phases up to `package` in order:

```
validate -> compile -> test -> package
```

If one of the build phases in the chain fails, the entire build process will terminate. Imagine our Java source code has a missing semicolon, the `compile` phase would detect this and terminate the process. As with a corrupt source file, there'll be no compiled `.class` file to test. When it comes to testing our Java project, both the `test` and `verify` build phases are of importance. As part of the `test` phase, we're running our unit tests with the [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/), and with `verify` our integration tests are executed by the [Maven Failsafe Plugin](https://maven.apache.org/surefire/maven-failsafe-plugin/index.html). Let's take a look at these two plugins.

#### Running Unit Tests With the Maven Surefire Plugin

The Maven Surefire is responsible for running our unit tests. We must either follow the default naming convention of our test classes, as discussed above, or [configure a different pattern](https://maven.apache.org/surefire/maven-surefire-plugin/examples/inclusion-exclusion.html) that matches our custom naming convention. In both cases, we have to place our tests inside `src/test/java` folder for the plugin to pick them up. For the upcoming examples, we're using a basic `format` method

```
public class Main {

  public String format(String input) {
    return input.toUpperCase();
  }
}
```

... and its corresponding test as a unit test blueprint:

```
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MainTest {

  private Main cut;

  @BeforeEach
  void setUp() {
    this.cut = new Main();
  }

  @Test
  void shouldReturnFormattedUppercase() {
    String input = "duke";

    String result = cut.format(input);

    assertEquals("DUKE", result);
  }
}
```

Depending on the Maven version and distribution format of our application (e.g., JAR or WAR), Maven defines [default versions for the core plugins](https://maven.apache.org/ref/3.8.1/maven-core/default-bindings.html). Besides the Maven Compiler Plugin, the Maven Resource Plugin, and other plugins, the Maven Surefire Plugin is such a core plugin. When packaging our application as a JAR file and using Maven 3.8.1, for example, Maven picks the Maven Surefire Plugin with version 2.12.4 by default unless we override it. As the default versions are sometimes a little bit behind the latest plugin versions, it's worth updating the plugin versions and manually specifying the plugin version inside our `pom.xml`:

```xml
<project>
  <!-- dependencies -->

  <build>
    <plugins>
      <plugin>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.0.0-M5</version>
      </plugin>
    </plugins>
  </build>
 </project>
```

As part of the `test` phase of the default lifecycle, we'll now see the Maven Surefire Plugin executing our tests:

```
$ mvn test

[INFO] --- maven-surefire-plugin:3.0.0-M5:test (default-test) @ testing-example ---
[INFO]
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running de.rieckpil.blog.MainTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.04 s - in de.rieckpil.blog.MainTest
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
```

For the example above, we're running one unit test with JUnit 5 (testing provider). There's no need to configure the testing provider anywhere, as with recent Surefire versions, the plugin will pick up the correct test provider by itself. The Maven Surefire Plugin [integrates both JUnit and TestNG as testing providers](https://maven.apache.org/surefire/maven-surefire-plugin/usage.html) out-of-the-box. If we don't want to execute all build phases before running our tests, we can also explicitly execute the `test` goal of the Surefire plugin:

```
mvn surefire:test
```

But keep in mind that we have to ensure that the test classes have been compiled first (e.g., by a previous build). We can further tweak and configure the Maven Surefire Plugin to, e.g., parallelize the execution of our unit tests. This is only relevant for JUnit 4, as JUnit 5 (JUnit Jupiter to be precise) [supports parallelization on the test framework level](https://junit.org/junit5/docs/snapshot/user-guide/index.html#writing-tests-parallel-execution). Whenever we want to skip our unit tests when building our project, we can use an additional parameter:

```
mvn package -DskipTests
```

We can also explicitly [run only one or multiple tests](https://maven.apache.org/surefire/maven-surefire-plugin/examples/single-test.html):


```
mvn test -Dtest=MainTest

mvn test -Dtest=MainTest#testMethod

mvn surefire:test -Dtest=MainTest
```

#### Running Integration Tests With the Maven Failsafe Plugin

Unlike the Maven Surefire Plugin, the Maven Failsafe Plugin is not a core plugin and hence won't be part of our project unless we manually include it. As already outlined, the Maven Failsafe plugin is used to run our integration test. In contrast to our unit tests, the integration tests usually take more time, more setup effort (e.g., [start Docker containers for external infrastructure with Testcontainers](https://rieckpil.de/howto-write-spring-boot-integration-tests-with-a-real-database/)), and test multiple components of our application together. We integrate the Maven Failsafe Plugin by adding it to the `build` section of our `pom.xml`:

```xml
<project>

  <!-- other dependencies -->

  <build>
      <!-- further plugins -->
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
      </plugin>
    </plugins>
  </build>
</project>
```

As part of the `executions` configuration, we specify [the goals of the Maven Failsafe plugin](https://maven.apache.org/surefire/maven-failsafe-plugin/plugin-info.html) we want to execute as part of our build process. A common pitfall is to only execute the `integration-test` goal. Without the `verify` goal, the plugin will run our integration tests but won't fail the build if there are test failures. The Maven Failsafe Plugin is invoked as part of the `verify` build phase of the default lifecycle. That's right after the `package` build phase where we build our distributable artifact (e.g., JAR):

```
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ testing-example ---
[INFO] Building jar: C:\Users\phili\Desktop\junk\testing-example\target\testing-example.jar
[INFO]
[INFO] --- maven-failsafe-plugin:3.0.0-M5:integration-test (default) @ testing-example ---
[INFO]
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running de.rieckpil.blog.MainIT
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.039 s - in de.rieckpil.blog.MainIT
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO]
[INFO] --- maven-failsafe-plugin:3.0.0-M5:verify (default) @ testing-example ---
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

If we want to run our integration tests manually, we can do so with the following command:

```
mvn failsafe:integration-test failsafe:verify
```

For scenarios where we don't want to run our integration test (but still our unit tests), we can add `-DskipITs` to our Maven execution:

```
mvn verify -DskipITs
```

Similar to the Maven Surefire Plugin, we can also run a subset of our integration tests:

```
mvn -Dit.test=MainIT failsafe:integration-test failsafe:verify

mvn -Dit.test=MainIT#firstTest failsafe:integration-test failsafe:verify
```

When using the command above, make sure the test classes have been compiled previously, as otherwise, there won't be any test execution. There's also a property available to entirely skip the compilation of test classes and avoid running any tests when building our project (not recommended):

```
mvn verify -Dmaven.test.skip=true
```

#### Summary of Testing Java Applications With Maven

Maven is a powerful, mature, and well-adopted build tool for Java projects. As a newcomer or when coming from a different programming language, the basics of the Maven build lifecycle and how and when different Maven Plugins interact is something to understand first. With the help of Maven Archetypes or using a framework initializer, we can easily bootstrap new Maven projects. There's no need to install Maven as a CLI tool for our machine as we can instead use the portable Maven Wrapper. Furthermore, keep this in mind when testing your Java applications and use Maven as the build tool:

* With Maven, we can separate the unit and integration test execution 
* The Maven Surefire Plugin runs our unit tests 
* The Maven Failsafe Plugin runs our integration tests 
* By following the default naming conventions for both plugins, we can easily separate our tests 
* The Maven default lifecycle consists of several build phases that are executed in order and sequentially 
* Use the [Java Testing Toolkit Maven archetype](https://github.com/rieckpil/custom-maven-archetypes) for your next testing adventure

### Use GitHub Actions

I was recently wasting time and energy to get the CI pipelines for my two main GitHub repositories working with Travis CI. Even though the documentation provides examples for Maven-based Java projects, it took me still some time to find the correct setup. 

While working on these pipelines, I remembered that GitHub now also provides its own CI/CD solution: [GitHub Actions](https://github.com/features/actions). As I wasn't quite satisfied with Travis CI, I gave it a try and got everything up- and running in less than one hour.

#### Introduction to GitHub Actions

GitHub [markets](https://github.com/features/actions) its GitHub Actions product as the following:

> GitHub Actions makes it easy to automate all your software workflows, now with world-class CI/CD. Build, test, and deploy your code right from GitHub. Make code reviews, branch management, and issue triaging work the way you want.

We can enter this feature on every (even private) GitHub repository with the **Actions** tab: [![GitHub Actions Tab Panel](https://rieckpil.de/wp-content/uploads/2019/12/gitHubActionsTabPanel.png)](https://rieckpil.de/wp-content/uploads/2019/12/gitHubActionsTabPanel.png) Within this tab, we get an overview of your latest builds and their logs like our might know it from Jenkins, Travis CI, Circle CI, etc. : [![GitHub Actions Overview](https://rieckpil.de/wp-content/uploads/2019/12/gitHubActionsOverview.png)](https://rieckpil.de/wp-content/uploads/2019/12/gitHubActionsOverview.png)

We configure your different pipeline steps as code and include them in your repository. The pipeline YAML definitions are then placed in the `.github/workflows` folder. Among other things, GitHub actions offers the following features:

* hosted runners for every OS (Windows, macOS, Linux)
* matrix builds to, e.g., test your library for different OS and programming language versions
* access to Docker to, e.g., use Testcontainers or a docker-compose.yml file for integration tests
* rich-feature marketplace next to pre-defined Actions provided by GitHub
* free for public repositories and limited contingent (minutes per month) for private repositories
* great integration for [events of your GitHub repository](https://help.github.com/en/actions/automating-your-workflow-with-github-actions/events-that-trigger-workflows) (e.g., pull request, issue creation, etc.)

Let's use a Maven-based Java project to demonstrate how to use GitHub Actions...

#### Sample Workflow for a Maven-Based Java Application

As an example, we'll use a [typical Java 11 Spring Boot Maven project](https://github.com/rieckpil/blog-tutorials/tree/master/github-actions-java-maven) to demonstrate the use of GitHub Actions.

The project uses Testcontainers to access a PostgreSQL database during integration tests. The deployment target is Kubernetes, and the application is packaged inside a Docker container. This should reflect 80% of the requirements for a standard CI/CD pipeline these days. In short, we want to achieve the following with our CI/CD pipeline using GitHub Actions:

* use different Java versions to compile the project (useful for library developers)
* cache the content of `.m2/repository` (or any other folder) for accelerated builds
* use Maven to build the project
* stash build artifacts between different jobs
* access secrets (to, e.g., login to a container registry )
* make use of Docker

The workflow uses three jobs: `compile`, `build` and `deploy`. Don't reflect on the meaningfulness of the following setup. We intentionally create a bloated pipeline to showcase as many features of GitHub Actions as possible. Let's start with the `compile` job:

```
name: Build sample Java Maven project

on: [push, pull_request]

jobs:
  compile:
    runs-on: ubuntu-20.04
    strategy:
      matrix:
        java: [ 11, 12, 13 ]
    name: Java ${{ matrix.java }} compile
    steps:
      - name: Checkout Source Code
        uses: actions/checkout@v2
      - name: Setup Java
        uses: actions/setup-java@v2
        with:
          distribution: 'adopt'
          java-package: jdk
          java-version: ${{ matrix.java }}
      - name: Compile the Project
        working-directory: github-actions-java-maven
        run: mvn -B compile
```

We configure the job to run on a hosted ubuntu-20.04 runner. GitHub let's use choose between Ubuntu, Windows, and Mac as GitHub-hosted runners. Those runners already come with a decent amount of binaries and tools installed (e.g. AWS CLI, Maven, etc.). For a complete list of installed software, see the [documentation on supported software](https://docs.github.com/en/actions/using-github-hosted-runners/about-github-hosted-runners#supported-software). 

Next, we define a matrix strategy to run the same job multiple times (in parallel) with different Java versions. The source code for the repository is not checked out on the runner by default. 

We use the `checkout@v2` action for this purpose. The `setup-java@v2` action is used to set up the specific Java version on the runner and as part of the last step, we're compiling the Java project with Maven. With `working-directory` we define in which folder we want to execute the commands. This way we don't have to explicitly perform any `cd` operation. Next comes the `build` job:

```
name: Build sample Java Maven project

on: [push, pull_request]

jobs:
  compile:
    # compile job

  build:
    runs-on: ubuntu-20.04
    needs: compile
    name: Build the Maven Project
    steps:
    - uses: actions/checkout@v2
    - uses: actions/cache@v2
      with:
        path: ~/.m2/repository
        key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
        restore-keys: |
          ${{ runner.os }}-maven-
    - name: Set up JDK 11
      uses: actions/setup-java@v2
      with:
          distribution: 'adopt'
          java-version: '11'
          java-package: jdk
    - name: Build and test project
      working-directory: github-actions-java-maven
      run: mvn -B verify
    - name: Upload Maven build artifact
      uses: actions/upload-artifact@v2
      with:
        name: artifact.jar
        path: github-actions-java-maven/target/github-actions-java-maven.jar
```

By default, GitHub executes all jobs in parallel unless we specify that the job depends on the outcome of a previous job. This way we can ensure a sequential order. Each GitHub Actions job starts with a fresh GitHub runner and hence we have to perform the VCS checkout and Java setup again.

As an alternative, we can (and should have) performed the Maven build as part of the previous job. Similar to the previous job, we're using Maven to now build the `.jar` file. We'll now cache the contents of the `.m2` folder to speed up subsequent builds as they don't have to download our dependencies over and over. 

After we've successfully built our project, we want to share the build artifact with an upcoming job. As the jobs don't share the same filesystem we have to upload it. Once we've uploaded an artifact (this might also be a screenshot from a failing web test), another job can download it. Furthermore, we can also manually download any build artifacts ourselves: ![GitHub Actions Download Artifact](https://rieckpil.de/wp-content/uploads/2019/12/github-actions-download-build-artifact-e1623407894167.png) And finally, we're (artificially) deploying the project:

```
name: Build sample Java Maven project

on: [push, pull_request]

jobs:
  # existing jobs ..

  deploy:
    runs-on: ubuntu-20.04
    needs: build
    name: Build Docker Container and Deploy to Kubernetes
    steps:
    - uses: actions/checkout@v2
    - name: Download Maven build artifact
      uses: actions/download-artifact@v2
      with:
        name: artifact.jar
        path: github-actions-java-maven/target
    - name: Build Docker container
      working-directory: github-actions-java-maven
      run: |
        docker build -t de.rieckpil.blog/github-actions-sample .
    - name: Access secrets
      env:
        SUPER_SECRET: ${{ secrets.SUPER_SECRET }}
      run: echo "Content of secret - $SUPER_SECRET"
    - name: Push Docker images
      run: echo "Pushing Docker image to Container Registry (e.g. ECR)"
    - name: Deploy application
      run: echo "Deploying application (e.g. Kubernetes)"
```

We first download the Maven build artifact as we need it to build our Docker image. Right after building the Docker image, we could now log in to our private Docker Registry to push our image. As this usually requires access to secrets (username and password) we demonstrate how to map secrets to environment variables. We can securely store those secrets as part of our GitHub repository (Settings -> Secrets). What's left is to deploy the new Docker Image to our target environment (e.g. Kubeternes).

#### Blueprints for Real-World Workflow With GitHub Actions

Over the past month, I've migrated most of my GitHub projects to GitHub Actions and never looked back. For further inspiration on how to use GitHub Actions, take a look at the following examples:

* Building and testing all blog post code examples as part of my [blog-tutorials repository](https://github.com/rieckpil/blog-tutorials/tree/master/.github/workflows) (Gradle, Maven, multiple Java versions, caching)
* Building and testing the source code for the [Getting Started With Eclipse MicroProfile Course](https://github.com/rieckpil/getting-started-with-microprofile/tree/master/.github/workflows) (Integration tests with MicroShed Testing, Docker, multiple projects, Open Liberty)
* For [Stratospheric](https://stratospheric.dev/) (a book about Spring Boot and AWS that I'm co-authoring), we're using GitHub actions for the entire CI/CD pipeline. We're building the Spring Boot backend, push the Docker image to ECR and create/sync our entire AWS infrastructure with the AWS CDK. Take a look at [the various workflows](https://github.com/stratospheric-dev/stratospheric/tree/main/.github/workflows) for some inspiration and [get a copy of this book](https://leanpub.com/stratospheric) if you want detailed information about this setup.
* If you're maintaining a Java library that you're publishing to Maven Central, consider [this GitHub Actions workflow blueprint](https://github.com/stratospheric-dev/cdk-constructs/tree/main/.github/workflows) for building and deploying libraries.
* Aggregating Selenide screenshots to download in case of test failures for the [Spring Boot Applications Masterclass](https://github.com/rieckpil/testing-spring-boot-applications-masterclass/blob/master/.github/workflows/maven.yml)
* Checking for [rotten links in markdown files](https://github.com/rieckpil/blog-tutorials/blob/master/.github/workflows/broken-links.yml) (must-have when writing eBooks)

To summarize, I can highly recommend GitHub Actions for Maven-based Java projects. The configuration is simple and you are ready in minutes. Whether you are building a Java library or an application in a private repository, GitHub Actions allows you to easily set up CI/CD. Give it a try! 

The [Spring Boot application](https://github.com/rieckpil/blog-tutorials/tree/master/github-actions-java-maven) and the [workflow](https://github.com/rieckpil/blog-tutorials/blob/master/.github/workflows/sampleJavaMavenProject.yml) definition is available on GitHub. 

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
