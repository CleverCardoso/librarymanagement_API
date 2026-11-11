package com.company.librarymanagement.dto.request.Book;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class BookUpdateRequest {

    private String title;

    private String author;

    private String category;

    private Integer publicationYear;

    @Min(value = 0, message = "A quantidade total não pode ser negativa")
    private Integer totalQuantity;

    private Boolean rare;
}
