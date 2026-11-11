package com.company.librarymanagement.dto.request.User;

import com.company.librarymanagement.enums.UserType;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateRequest {

    private String name;

    @Email(message = "Formato de e-mail inválido")
    private String email;

    private String phone;

    private UserType userType;
}
