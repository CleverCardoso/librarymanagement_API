package com.company.librarymanagement.dto.request.User;

import com.company.librarymanagement.enums.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserCreateRequest {

    @NotBlank(message = "O nome é obrigatório")
    private String name;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    private String email;

    private String phone;

    @NotNull(message = "O tipo de usuário é obrigatório (STUDENT, TEACHER ou VISITOR)")
    private UserType userType;
}
