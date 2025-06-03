package de.rieckpil.blog;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(LocalDevTestcontainerConfig.class)
class ApplicationIT {

  @Test
  void contextLoads() {}
}
