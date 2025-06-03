package de.rieckpil.blog;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

/** DTO for book creation requests using Java Record */
public record BookCreationRequest(
    @NotBlank(message = "ISBN is required")
        @Pattern(regexp = "^\\d{3}-\\d{10}$", message = "ISBN must be in format 123-1234567890")
        String isbn,
    @NotBlank(message = "Title is required") String title,
    @NotBlank(message = "Author is required") String author,
    @NotNull(message = "Published date is required")
        @Past(message = "Published date must be in the past")
        LocalDate publishedDate) {}
