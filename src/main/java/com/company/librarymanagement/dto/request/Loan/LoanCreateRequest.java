package com.company.librarymanagement.dto.request.Loan;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoanCreateRequest {

    @NotNull(message = "O ID do usuário é obrigatório")
    private Long userId;

    @NotNull(message = "O ID do livro é obrigatório")
    private Long bookId;
}
