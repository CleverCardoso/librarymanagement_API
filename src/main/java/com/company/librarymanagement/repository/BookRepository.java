package com.company.librarymanagement.repository;

import com.company.librarymanagement.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByAvailableQuantityGreaterThan(Integer quantity);

    @Query("SELECT b FROM Book b WHERE b.availableQuantity < b.totalQuantity")
    List<Book> findBooksCurrentlyBorrowed();
}
