package com.company.librarymanagement.controller;

import com.company.librarymanagement.dto.request.Loan.LoanCreateRequest;
import com.company.librarymanagement.dto.response.LoanResponse;
import com.company.librarymanagement.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoanResponse createLoan(@Valid @RequestBody LoanCreateRequest request) {
        return loanService.createLoan(request);
    }

    @PatchMapping("/{id}/return")
    public LoanResponse returnLoan(@PathVariable Long id) {
        return loanService.returnLoan(id);
    }

    @GetMapping
    public List<LoanResponse> listAllLoans() {
        return loanService.listAllLoans();
    }

    @GetMapping("/active")
    public List<LoanResponse> listActiveLoans() {
        return loanService.listActiveLoans();
    }

    @GetMapping("/user/{userId}")
    public List<LoanResponse> listLoansByUser(@PathVariable Long userId) {
        return loanService.listLoansByUser(userId);
    }
}
