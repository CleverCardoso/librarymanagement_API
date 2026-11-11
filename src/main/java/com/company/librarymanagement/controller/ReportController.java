package com.company.librarymanagement.controller;

import com.company.librarymanagement.dto.response.BookResponse;
import com.company.librarymanagement.dto.response.UserResponse;
import com.company.librarymanagement.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/books/available")
    public List<BookResponse> getAvailableBooks() {
        return reportService.getAvailableBooks();
    }

    @GetMapping("/books/borrowed")
    public List<BookResponse> getBorrowedBooks() {
        return reportService.getBorrowedBooks();
    }

    @GetMapping("/users/overdue")
    public List<UserResponse> getUsersWithOverdueLoans() {
        return reportService.getUsersWithOverdueLoans();
    }

    @GetMapping("/books/ranking")
    public List<BookResponse> getBooksRanking() {
        return reportService.getBooksRanking();
    }
}
