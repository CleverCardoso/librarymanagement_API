package com.company.librarymanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    private String title;
    private String author;
    private String category;
    private String publicationYear;
    private String totalQuantity;
    private String availableQuantity;
    private String rare;

    public Book() {
    }


}
