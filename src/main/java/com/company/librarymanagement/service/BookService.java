package com.company.librarymanagement.service;

import com.company.librarymanagement.dto.request.Book.BookCreateRequest;
import com.company.librarymanagement.dto.request.Book.BookUpdateRequest;
import com.company.librarymanagement.dto.response.BookResponse;
import com.company.librarymanagement.exception.BusinessRuleException;
import com.company.librarymanagement.exception.ResourceNotFoundException;
import com.company.librarymanagement.model.Book;
import com.company.librarymanagement.repository.BookRepository;
import com.company.librarymanagement.repository.LoanRepository;
import com.company.librarymanagement.util.TextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;

    public BookResponse createBook(BookCreateRequest request) {
        Book book = new Book();
        book.setTitle(TextUtils.cleanName(request.getTitle()));
        book.setAuthor(TextUtils.cleanName(request.getAuthor()));
        book.setCategory(TextUtils.cleanName(request.getCategory()));
        book.setPublicationYear(request.getPublicationYear());
        book.setTotalQuantity(request.getTotalQuantity());
        // Um livro novo começa com todas as cópias disponíveis
        book.setAvailableQuantity(request.getTotalQuantity());
        book.setRare(Boolean.TRUE.equals(request.getRare()));

        return BookResponse.from(bookRepository.save(book));
    }

    public List<BookResponse> listBooks() {
        return bookRepository.findAll().stream()
                .map(BookResponse::from)
                .toList();
    }

    public BookResponse getBookById(Long id) {
        return BookResponse.from(findBookEntityById(id));
    }

    @Transactional
    public BookResponse updateBook(Long id, BookUpdateRequest request) {
        Book book = findBookEntityById(id);

        if (request.getTitle() != null) {
            String title = TextUtils.cleanName(request.getTitle());
            if (title == null) {
                throw new BusinessRuleException("O título do livro não pode ficar vazio.");
            }
            book.setTitle(title);
        }

        if (request.getAuthor() != null) {
            String author = TextUtils.cleanName(request.getAuthor());
            if (author == null) {
                throw new BusinessRuleException("O autor do livro não pode ficar vazio.");
            }
            book.setAuthor(author);
        }

        if (request.getCategory() != null) {
            book.setCategory(TextUtils.cleanName(request.getCategory()));
        }

        if (request.getPublicationYear() != null) {
            book.setPublicationYear(request.getPublicationYear());
        }

        if (request.getTotalQuantity() != null) {
            updateTotalQuantity(book, request.getTotalQuantity());
        }

        if (request.getRare() != null) {
            book.setRare(request.getRare());
        }

        return BookResponse.from(bookRepository.save(book));
    }

    public void deleteBook(Long id) {
        Book book = findBookEntityById(id);

        if (loanRepository.existsByBookId(id)) {
            throw new BusinessRuleException("Não é possível excluir um livro que possui empréstimos registrados.");
        }

        bookRepository.delete(book);
    }

    public Book findBookEntityById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado."));
    }

    // Ao mudar o total de cópias, as disponíveis acompanham, sem contar as que estão emprestadas.
    private void updateTotalQuantity(Book book, Integer newTotal) {
        int borrowed = book.getTotalQuantity() - book.getAvailableQuantity();

        if (newTotal < borrowed) {
            throw new BusinessRuleException(
                    "A quantidade total não pode ser menor que o número de cópias emprestadas (" + borrowed + ").");
        }

        book.setTotalQuantity(newTotal);
        book.setAvailableQuantity(newTotal - borrowed);
    }
}
