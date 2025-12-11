package de.rieckpil.blog;

import java.time.LocalDate;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
class BookControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private BookService bookService;

  @Nested
  @DisplayName("DELETE /api/books/{id} endpoint tests")
  class DeleteBookTests {

    @Test
    @DisplayName("Should return 401 Unauthorized when no authentication is provided")
    void shouldReturnUnauthorizedWhenNoAuthentication() throws Exception {
      mockMvc.perform(delete("/api/books/1")).andExpect(status().isUnauthorized());

      verify(bookService, times(0)).deleteBook(any());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return 403 Forbidden when authenticated with insufficient privileges")
    void shouldReturnForbiddenWhenInsufficientPrivileges() throws Exception {
      mockMvc.perform(delete("/api/books/1")).andExpect(status().isForbidden());

      verify(bookService, times(0)).deleteBook(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 204 No Content when authenticated as admin and book exists")
    void shouldReturnNoContentWhenAdminAndBookExists() throws Exception {
      when(bookService.deleteBook(1L)).thenReturn(true);

      mockMvc.perform(delete("/api/books/1")).andExpect(status().isNoContent());

      verify(bookService, times(1)).deleteBook(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 Not Found when authenticated as admin but book doesn't exist")
    void shouldReturnNotFoundWhenAdminButBookDoesNotExist() throws Exception {
      when(bookService.deleteBook(999L)).thenReturn(false);

      mockMvc.perform(delete("/api/books/999")).andExpect(status().isNotFound());

      verify(bookService, times(1)).deleteBook(999L);
    }
  }

  @Nested
  @DisplayName("POST /api/books endpoint tests")
  class CreateBookTests {

    @Test
    @WithMockUser
    @DisplayName("Should return 201 Created when valid book data is provided")
    void shouldReturnCreatedWhenValidBookData() throws Exception {
      String validBookJson =
          """
        {
            "isbn": "978-1234567890",
            "title": "Test Book",
            "author": "Test Author",
            "publishedDate": "2023-01-01"
        }
        """;

      when(bookService.createBook(any())).thenReturn(1L);

      mockMvc
          .perform(
              post("/api/books").contentType(MediaType.APPLICATION_JSON).content(validBookJson))
          .andExpect(status().isCreated())
          .andExpect(header().exists("Location"))
          .andExpect(header().string("Location", Matchers.containsString("/api/books/1")));

      verify(bookService, times(1)).createBook(any());
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 Bad Request when invalid book data is provided")
    void shouldReturnBadRequestWhenInvalidBookData() throws Exception {
      String invalidBookJson =
          """
        {
            "isbn": "",
            "title": "",
            "author": "Test Author",
            "publishedDate": "2025-01-01"
        }
        """;

      mockMvc
          .perform(
              post("/api/books").contentType(MediaType.APPLICATION_JSON).content(invalidBookJson))
          .andExpect(status().isBadRequest());

      verify(bookService, times(0)).createBook(any());
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 Bad Request when ISBN is null")
    void shouldReturnBadRequestWhenIsbnIsNull() throws Exception {
      String nullIsbnJson =
          """
        {
            "isbn": null,
            "title": "Test Book",
            "author": "Test Author",
            "publishedDate": "2023-01-01"
        }
        """;

      mockMvc
          .perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(nullIsbnJson))
          .andExpect(status().isBadRequest());

      verify(bookService, times(0)).createBook(any());
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 Bad Request when publishedDate is in the future")
    void shouldReturnBadRequestWhenPublishedDateInFuture() throws Exception {
      LocalDate futureDate = LocalDate.now().plusDays(1);
      String futureDateJson =
          String.format(
              """
        {
            "isbn": "978-1234567890",
            "title": "Test Book",
            "author": "Test Author",
            "publishedDate": "%s"
        }
        """,
              futureDate);

      mockMvc
          .perform(
              post("/api/books").contentType(MediaType.APPLICATION_JSON).content(futureDateJson))
          .andExpect(status().isBadRequest());

      verify(bookService, times(0)).createBook(any());
    }
  }
}
