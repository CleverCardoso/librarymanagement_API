package com.company.librarymanagement.dto.response;

import com.company.librarymanagement.enums.LoanStatus;
import com.company.librarymanagement.model.Loan;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanResponse {
    private Long id;
    private Long userId;
    private Long bookId;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private LoanStatus status;
    private BigDecimal fineAmount;

    public static LoanResponse from(Loan loan) {
        LoanResponse response = new LoanResponse();
        response.setId(loan.getId());
        response.setUserId(loan.getUser().getId());
        response.setBookId(loan.getBook().getId());
        response.setLoanDate(loan.getLoanDate());
        response.setDueDate(loan.getDueDate());
        response.setReturnDate(loan.getReturnDate());
        response.setStatus(currentStatus(loan));
        response.setFineAmount(loan.getFineAmount());
        return response;
    }

    // Um empréstimo ainda não devolvido e com o prazo vencido aparece como atrasado.
    private static LoanStatus currentStatus(Loan loan) {
        boolean pastDueDate = loan.getDueDate().isBefore(LocalDate.now());
        if (loan.getStatus() == LoanStatus.ACTIVE && pastDueDate) {
            return LoanStatus.OVERDUE;
        }
        return loan.getStatus();
    }
}
