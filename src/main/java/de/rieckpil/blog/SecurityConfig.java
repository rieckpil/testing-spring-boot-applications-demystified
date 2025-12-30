package de.rieckpil.blog;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
      .authorizeHttpRequests(
        auth ->
          auth.requestMatchers(HttpMethod.GET, "/api/books/{id}")
            .hasRole("USER")
            .requestMatchers(HttpMethod.DELETE, "/api/books/{id}")
            .hasRole("ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/books").permitAll()
            .anyRequest()
            .authenticated())
      .httpBasic(Customizer.withDefaults());

    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    UserDetails admin =
      User.builder()
        .username("admin")
        .password(passwordEncoder.encode("admin"))
        .roles("ADMIN")
        .build();

    UserDetails librarian =
      User.builder()
        .username("librarian")
        .password(passwordEncoder.encode("librarian"))
        .roles("LIBRARIAN")
        .build();

    UserDetails user =
      User.builder()
        .username("user")
        .password(passwordEncoder.encode("user"))
        .roles("USER")
        .build();

    return new InMemoryUserDetailsManager(admin, librarian, user);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
