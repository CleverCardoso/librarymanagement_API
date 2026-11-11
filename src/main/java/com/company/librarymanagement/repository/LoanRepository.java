package com.company.librarymanagement.repository;

import com.company.librarymanagement.enums.LoanStatus;
import com.company.librarymanagement.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByStatus(LoanStatus status);

    List<Loan> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    boolean existsByBookId(Long bookId);

    @Query("SELECT l FROM Loan l WHERE l.status = com.company.librarymanagement.enums.LoanStatus.ACTIVE AND l.dueDate < CURRENT_DATE")
    List<Loan> findOverdueLoans();

    @Query("SELECT l.book.id FROM Loan l GROUP BY l.book.id ORDER BY COUNT(l.id) DESC")
    List<Long> findTopBorrowedBookIds();
}
