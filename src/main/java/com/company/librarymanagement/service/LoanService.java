package com.company.librarymanagement.service;

import com.company.librarymanagement.config.LibraryProperties;
import com.company.librarymanagement.dto.request.Loan.LoanCreateRequest;
import com.company.librarymanagement.dto.response.LoanResponse;
import com.company.librarymanagement.enums.LoanStatus;
import com.company.librarymanagement.exception.BusinessRuleException;
import com.company.librarymanagement.exception.ResourceNotFoundException;
import com.company.librarymanagement.model.Book;
import com.company.librarymanagement.model.Loan;
import com.company.librarymanagement.model.User;
import com.company.librarymanagement.repository.BookRepository;
import com.company.librarymanagement.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserService userService;
    private final BookService bookService;
    private final LibraryProperties libraryProperties;

    @Transactional
    public LoanResponse createLoan(LoanCreateRequest request) {
        User user = userService.findUserEntityById(request.getUserId());
        Book book = bookService.findBookEntityById(request.getBookId());

        if (book.getAvailableQuantity() <= 0) {
            throw new BusinessRuleException("Não existem cópias disponíveis deste livro.");
        }

        // Livros raros têm um prazo de empréstimo menor
        int loanDays = Boolean.TRUE.equals(book.getRare())
                ? libraryProperties.rareLoanDays()
                : libraryProperties.loanDays();

        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        bookRepository.save(book);

        LocalDate today = LocalDate.now();

        Loan loan = new Loan();
        loan.setUser(user);
        loan.setBook(book);
        loan.setLoanDate(today);
        loan.setDueDate(today.plusDays(loanDays));
        loan.setStatus(LoanStatus.ACTIVE);

        return LoanResponse.from(loanRepository.save(loan));
    }

    @Transactional
    public LoanResponse returnLoan(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empréstimo não encontrado."));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new BusinessRuleException("Este empréstimo já foi devolvido.");
        }

        Book book = loan.getBook();
        book.setAvailableQuantity(book.getAvailableQuantity() + 1);
        bookRepository.save(book);

        LocalDate today = LocalDate.now();

        loan.setReturnDate(today);
        loan.setStatus(LoanStatus.RETURNED);
        loan.setFineAmount(calculateFine(loan.getDueDate(), today));

        return LoanResponse.from(loanRepository.save(loan));
    }

    public List<LoanResponse> listAllLoans() {
        return loanRepository.findAll().stream()
                .map(LoanResponse::from)
                .toList();
    }

    // Empréstimos ainda não devolvidos, incluindo os atrasados
    public List<LoanResponse> listActiveLoans() {
        return loanRepository.findByStatus(LoanStatus.ACTIVE).stream()
                .map(LoanResponse::from)
                .toList();
    }

    public List<LoanResponse> listLoansByUser(Long userId) {
        userService.findUserEntityById(userId);

        return loanRepository.findByUserId(userId).stream()
                .map(LoanResponse::from)
                .toList();
    }

    // A multa é cobrada por dia de atraso na devolução
    private BigDecimal calculateFine(LocalDate dueDate, LocalDate returnDate) {
        long daysLate = ChronoUnit.DAYS.between(dueDate, returnDate);

        if (daysLate <= 0) {
            return BigDecimal.ZERO;
        }

        return libraryProperties.finePerDay().multiply(BigDecimal.valueOf(daysLate));
    }
}
