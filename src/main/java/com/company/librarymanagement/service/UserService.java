package com.company.librarymanagement.service;

import com.company.librarymanagement.dto.request.User.UserCreateRequest;
import com.company.librarymanagement.dto.request.User.UserUpdateRequest;
import com.company.librarymanagement.dto.response.UserResponse;
import com.company.librarymanagement.exception.BusinessRuleException;
import com.company.librarymanagement.exception.ResourceNotFoundException;
import com.company.librarymanagement.model.User;
import com.company.librarymanagement.repository.LoanRepository;
import com.company.librarymanagement.repository.UserRepository;
import com.company.librarymanagement.util.TextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final LoanRepository loanRepository;

    public UserResponse createUser(UserCreateRequest request) {
        String email = TextUtils.cleanEmail(request.getEmail());

        if (userRepository.existsByEmail(email)) {
            throw new BusinessRuleException("Já existe um usuário cadastrado com este e-mail.");
        }

        User user = new User();
        user.setName(TextUtils.cleanName(request.getName()));
        user.setEmail(email);
        user.setPhone(TextUtils.cleanPhone(request.getPhone()));
        user.setUserType(request.getUserType());

        return UserResponse.from(userRepository.save(user));
    }

    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        return UserResponse.from(findUserEntityById(id));
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = findUserEntityById(id);

        if (request.getName() != null) {
            String name = TextUtils.cleanName(request.getName());
            if (name == null) {
                throw new BusinessRuleException("O nome não pode ficar vazio.");
            }
            user.setName(name);
        }

        if (request.getEmail() != null) {
            String email = TextUtils.cleanEmail(request.getEmail());

            if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
                throw new BusinessRuleException("Já existe outro usuário cadastrado com este e-mail.");
            }
            user.setEmail(email);
        }

        if (request.getPhone() != null) {
            user.setPhone(TextUtils.cleanPhone(request.getPhone()));
        }

        if (request.getUserType() != null) {
            user.setUserType(request.getUserType());
        }

        return UserResponse.from(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        User user = findUserEntityById(id);

        if (loanRepository.existsByUserId(id)) {
            throw new BusinessRuleException("Não é possível excluir um usuário que possui empréstimos registrados.");
        }

        userRepository.delete(user);
    }

    public User findUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    }
}
