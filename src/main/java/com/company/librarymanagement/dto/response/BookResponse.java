package com.company.librarymanagement.dto.response;

import com.company.librarymanagement.model.Book;
import lombok.Data;

@Data
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private String category;
    private Integer publicationYear;
    private Integer totalQuantity;
    private Integer availableQuantity;
    private Boolean rare;

    public static BookResponse from(Book book) {
        BookResponse response = new BookResponse();
        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setCategory(book.getCategory());
        response.setPublicationYear(book.getPublicationYear());
        response.setTotalQuantity(book.getTotalQuantity());
        response.setAvailableQuantity(book.getAvailableQuantity());
        response.setRare(book.getRare());
        return response;
    }
}
