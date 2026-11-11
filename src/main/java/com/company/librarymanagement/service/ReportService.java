package com.company.librarymanagement.service;

import com.company.librarymanagement.dto.response.BookResponse;
import com.company.librarymanagement.dto.response.UserResponse;
import com.company.librarymanagement.model.Loan;
import com.company.librarymanagement.repository.BookRepository;
import com.company.librarymanagement.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;

    public List<BookResponse> getAvailableBooks() {
        return bookRepository.findByAvailableQuantityGreaterThan(0).stream()
                .map(BookResponse::from)
                .toList();
    }

    public List<BookResponse> getBorrowedBooks() {
        return bookRepository.findBooksCurrentlyBorrowed().stream()
                .map(BookResponse::from)
                .toList();
    }

    // Usuários com pelo menos um empréstimo atrasado, sem repetir
    public List<UserResponse> getUsersWithOverdueLoans() {
        return loanRepository.findOverdueLoans().stream()
                .map(Loan::getUser)
                .distinct()
                .map(UserResponse::from)
                .toList();
    }

    // Livros ordenados do mais emprestado para o menos emprestado
    public List<BookResponse> getBooksRanking() {
        return loanRepository.findTopBorrowedBookIds().stream()
                .map(bookRepository::findById)
                .flatMap(Optional::stream)
                .map(BookResponse::from)
                .toList();
    }
}
