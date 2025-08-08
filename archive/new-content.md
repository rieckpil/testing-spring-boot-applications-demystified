

# Why Testing Matters

In the world of software development, testing plays a crucial role in ensuring the reliability, stability, and functionality of your code.

For many teams, however, testing is often an afterthought, hastily addressed right before submitting a pull request for a feature merge into production. This rushed approach typically results in minimal testing efforts, with developers adding just enough tests to meet the minimum requirements.

## Common Misconceptions About Testing

For some teams, the primary motivation for testing is to meet a predetermined code coverage threshold, such as 80%.

While achieving high code coverage is beneficial, it shouldn’t be the sole focus.

Both of these approaches - rushed testing before a merge and testing solely to meet coverage metrics - are flawed and do not harness the true potential of a robust testing strategy.

## The True Value of Testing

Our main goal with testing is to build a solid foundation that allows us to make changes confidently. Well-written tests and a comprehensive test suite provide numerous benefits:

- Increased Confidence: Tests give us the confidence to refactor code fearlessly, knowing that any issues introduced will be caught early.
- Improved Documentation: Tests serve as a living documentation for the codebase, making it easier for developers to understand the intended behavior and purpose of different code areas.
- Enhanced Productivity: A reliable test suite can significantly boost productivity by catching bugs early in the development process, reducing the time spent on debugging and fixing issues later.

By focusing on these aspects, we can shift our perspective on testing from a mere formality to a critical component of the development process that enhances the quality and reliability of our applications.


Every changing industry, more software released every day, testing, especially automated testing, is crucial but for many only an afterthought
That's not good, good tests provide stability, maintainability allow for fearless refactoring, and are a safety net for the future
Our future self and team mates will thank us for writing good tests
The cost of not testing is high, the cost of testing is low especially with Java & Spring Boot, once you know the HOW
unfortunately, many developers don't know the HOW, they don't know how to test, they don't know how to write good tests
Test practices are not taught in school, they are not part of the curriculum, they are not part of the job description
bad test practices are inherited over decades in teams, companies, and the industry
Developers adopt to the existing testing practices, not challenging them, not improving them
Ending up in brittle test code that almost 1:1 mirrors the production code, making it hard to change the production code
Only knowing JUnit and Mockito, using brute force to keep the test coverage metric up
Wondering that they still can't move fast and have regressions from time to time

# What This Book Will Teach You

Java is a mature language, the testing ecosytem is huge, many libraries and frameworks have been developed over the years
There's almost for all testing scenarios a well-maintained library
Even more, the Spring Framework and Spring Boot provide a lot of testing support out of the box
Many of those testing support is hidden in the docs
In the end, you only need a hand-full of testing recipes to cover 99% of your testing needs
Don't use solely Mockito and JUnit for everything, unit testing is one part, but well written and covering integration and end-to-end tests will set you apart

# Conventions

Testing pyramid, testing honeycomb, testing trophies, testing box, etc. you name it. A lot of names for our testing strategy
No relgious advocate for any of those, I rather optimize for the central goal: Getting confidence and feedback as fast as possible.

A highly algorithmitc librariy might achieve this with 99% unit test, a simple CRUD application with a React frontend and a checkout integration with payPal may not follow this percentage and add more integration and end-to-end tests to it

What's important is that we have a common understanding of what we're writing and the testing terms we are using. component testing, black box testing ,white box testing, integral  testing, you name it
Using too much terms can be confusing, but not using any terms can be confusing as well, we need a clear definition for at least the team we're working in

I stick with three simple test types: unit, integration, and end-to-end tests

unit: fast, isolated, no external dependencies, no Spring context (usually using Mockito and JUnit only)
integration test: a bit slower than unit tests, usually involve some kind of infrastructure( database, messaging queue), in the context of Spring, they work with a Spring Context that usually contains multiple beans
end-to-end tests: slowest, usually involve the whole application, including the frontend, and external dependencies like databases, messaging queues, etc. Verifying the whole system works as expected, covering the happy paths

# How This Book Is Organized

We'll start with the basic for testing Spring Boot applications with Java and Maven, Things are quite similar for Gradle, however some commands for running the tests differ. We'll mention those differences when necessary

Go through the different test types, unit, integration, and end-to-end tests, and discuss the best practices for each of them, using hands-on examples

At the end, some general tips and tricks for testing spring boot applications: context caching, test parallelization, and more.

TDD with Spring Boot: master the recipes and become even more productive when using TDD with Spring Boot

# Who This Book Is For

Developers working with Spring Boot and Java for some time, newcomers are also fine, however we won't cover Java language features or discuss basic features of Spring and Spring Boot

Developers who try to break out of the bad testing patterns and learn what Spring Boot and Java have to offer in terms of testing
Expand their knowledge and learn new testing libraries and frameworks
Become more productive and confident in their daily work

Agenda:

1. Introduction
2. Testing with Spring Boot Fundamentals
3. Testing with a Sliced Application Context
4. Testing with @SpringBootTest
5. Testing Pitfalls & Best Practices
6. Outlook

# Basics For Testing Spring Boot

New project at start.spring.io, add the web dependency and some persitence layer
The Spring Boot Starter Test is also part by default although we didn't add it explicitly
That's already a nice default of Spring Boot, adding testing support right from day one, no need to argue within the team about which testing libraries to use, it's already there
Let's look at what's in there, run maven dependency tree (gralde equivalent) and see what's in there
Many transisitve dependencies, they are all in the test scope, so they don't    end up in the production code and don't make our final jar file bigger
Spring team made a pragmatic decision about what to include in the test scope, they included the most common libraries and frameworks for testing Spring Boot applications
They maintain the library versions, ensuring they work well together, as well as integration with our build tool, Maven or Gradle
Let's look at each included testing library step by step:

- JUnit 5: The de-facto standard for writing unit tests in Java, it's the default in Spring Boot, and it's the default in the Java ecosystem
- Mockito: The most popular mocking library for Java, it's the default in Spring Boot, and it's the default in the Java ecosystem
- AssertJ: A fluent assertion library, it's the default in Spring Boot, and it's the default in the Java ecosystem
- Hamcrest: Another assertion library, it's the default in Spring Boot, and it's the default in the Java ecosystem
- JSONAssert: A library for comparing JSON, it's the default in Spring Boot, and it's the default in the Java ecosystem
- JsonPath: A library for querying JSON, it's the default in Spring Boot, and it's the default in the Java ecosystem
- Awaitility: A library for testing asynchronous code, it's the default in Spring Boot, and it's the default in the Java ecosystem
- Spring Test: The testing support provided by Spring, it's the default in Spring Boot, and it's the default in the Java ecosystem

Use code examples for each of those libraries, show how to use them, and what they are good for.

Spring Boot's dependency management defines the version, we can use it or back off, by overriding the property.
If use for example JUnit 4 for our tests, we can configure this (hint take a look at OpenRewrite for automatic migration support).

# Getting Started With Unit Testing

The most common test type, the most common test library, JUnit and Mockito
We will test usually a single method in isolation, no Spring context, no external dependencies, any dependent class is usually mocked to avoid side effects
We will test the behaviour of the method, not the implementation (mention books: Good Tests, Bad Tests), this book is not about writing good but rather on the tooling and libraries

Explain the Mockito extension: excourse to JUnit Jupiter extension, what they are used for
Simple CUT, class under test, mock the collaborators, verify the interactions, verify the return value, verify the exceptions
Fast to run inside the IDE
Naming convention for Maven and Gradle: `*Test`, with Maven, the Surefire plugin will run them when we exeucte `mvn test`, with Gradle, the `test` task will run them
Some further key points for unit test: should be easily parallisized, should be fast, should be isolated, should be independent of the environment, should be independent of the execution order

Should base our foundation of our testing setup.

- Bare bones: https://saile.it/stop-springifying-your-unit-tests/


# Getting Started with Integration Testing

Now more complex, we need a Spring context, we need to test the interaction of multiple beans, we need to test the interaction with the infrastructure, we need to test the interaction with the database, messaging queue, etc.
Spring context, you'll see in the logs when running the tests the banner
We can bootstrap our own context or used slice test context annotation, excellent concept of Spring Boot, bootstrapping a minimal applicaiton context with only necassry beans, e.g. for testing the web or database layer.

When testing a controller in isolation, we don't care about any database interactions and vice-versa.
Show example for three sliced context annotations
There are many, see this list
Some libraries also bring their own annotations, e.g. Spring Data JPA, Spring Data MongoDB, etc.

When need infrastrucutre, Testcontainers to the rescue, show example for a database, show example for a messaging queue

Naming convention: `*IT`, with Maven, the Failsafe plugin (need to add it) will run them when we execute `mvn verify`, with Gradle, the `integrationTest` task will run them. this lets us seperate the tests and fail first the build if there's an issue with the unit tests, allowing faster feedback

Write our own sliced context annotation, if needed and no support from Spring Boot is present

`@Mock` vs. `@MockBean`. Solution needed to provide beans that are not auto-configured for sliced tests, there we can provide the real, a fake or a mock bean

Overriding properties is also possible

Last integration test, start the entire Spring Context with `@SpringBootTest`. Two mode: with and withouth the real servlet environment. Understand the difference and when to use which

Now all beans start, all infrastructure needs to be present. Testcontainers to the resuce, WireMock for external calls.

Hit the application with an HTTPClient, TestRestTemplate or WebTestClient, RestAssured is also possible.


# End-to-End Testing

Start the entire application, preferably also the frontend, if it's bundled withing the backend, e.g. a server-side rendered application. We use Thymeleaf. Simple view with a form, submit the form, verify the result.

Interact with the browser: Selenium to the rescue, or even better Selenide which is a wrapper on top of the Selenium API, allowing even simpler interactions with the browser

Testcontaienrs to run the driver and browser to make it robust, e2e test tend to be flaky, we need to make them as robust as possible

Tackling flakiness: investigate, use Testcontainers, use a Maven property to re-run a faield test. This should be a good starting point, but always make sure to understand the underlying flakiness and solve it at the root cause

Check the Masterclass if you want to see how to write e2e for a bundled SPA (React, Angular, Vue) with Spring Boot

# Advanced Topics Finally

Spring Context Caching: Speed up the execution of our tests, some pitfalls

Why to use it, what's the context caching and how to make the most of it.

Consider my talk at the Spring I/O 2022, where I explain the concept in detail

Be aware of `@DirtiesContext` and what goes into the context caching definition

## Transactional Pitfalls

Cleaning up the database, by default with `@Transactional`, spring will rollback, in production it commits

Transactional pitfall to understand, strategy needed to work with Db, cleanup afterwards, dumb trash in there but make sure each test uses e.g. a prefix, own tenant

## Test Parallelization

Paralleize tests: first only unit test as they should be easily parallelizeable, if not you did something wrong. Afterwards, you can try to parallelize the integration tests. This takes more effort and may not be worth the hassle.
Also never rely on the test execution order, they should be independent of each other, to quote JUnit's documentation: the test are run in a deterministic but not easily predictable order

## TDD with Spring Boot

TDD: Need all the recipes, focus on the test first and let it drive the design and implementation of the production code. When working remotely, we can use remote ping-pong TDD. On dev writes the implementation and the other the test, switch after some minutes. requires a low latency connection, e.g. with a good video conferencing tool and an IDE that supports remote pair programming

## Further Tools

AI tools are transforming the way we write tests, e.g. Diffblue,

OpenRewrite to migrate old Mockito or JUnit versions.

Learn the shortcuts, I have a single key to run all tests and re-run the last tests, staying super productive and feedback close to zero

Check out Testcontainers Cloud

# Afterword

Testing Spring Boot applications is not that hard at all
There's so much support available, so many excellent testing tools & libraries out there
We just have to spend some time exploring them and taking testing seriously
I went on this journey almost five years ago and never looked back
In the end, we all want a good night's sleep and not worry about our applications breaking in production
Coding should remain a fun activity, testing is part of coding
Don't just hand over your code to a dedicated team of testers, hoping they will find the flaws in your code, take responsibility for your code and write good tests
Spread the news about this book with your friends and colleagues and let's try to change the way we look at automated testing


I hope this compact book was a good starting point for you to get into testing Spring Boot applications
There's much more to explore and learn
Many scenarios to cover, many libraries to try out
Stay curious, stay open-minded, and keep learning

A broad overview of the testing given, in the end it's all about recipes and understanding what can break and what needs to be tested. avoid testing the framework. Many great support from Spring Boot, we just need to know how to use it.

What's next? Get started in your project

Need more hands-on examples and explanations? Check out the Masterclass, on-demand videos, and code examples with 12h+ content and 130 lessons, up-to-date with Java 21 and Spring Boot 3.2
Joyful testing,
Philip

## Keeping in Touch

Best way to stay in touch is to follow me on X or LinkedIn.

For up-to-date news and further content visit my blog and subscribe to the newsletter.

For an in-depth coverage of the topic, check out the Masterclass, on-demand videos, and code examples with 12h+ content and 130 lessons, up-to-date with Java 21 and Spring Boot 3.2.
