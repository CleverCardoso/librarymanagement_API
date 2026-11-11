package com.company.librarymanagement.dto.request.Book;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookCreateRequest {

    @NotBlank(message = "O título do livro é obrigatório")
    private String title;

    @NotBlank(message = "O autor do livro é obrigatório")
    private String author;

    private String category;

    private Integer publicationYear;

    @NotNull(message = "A quantidade total é obrigatória")
    @Min(value = 0, message = "A quantidade total não pode ser negativa")
    private Integer totalQuantity;

    private Boolean rare;
}
