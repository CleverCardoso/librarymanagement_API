package com.company.librarymanagement.service;


import com.company.librarymanagement.model.Book;
import com.company.librarymanagement.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book createBook(Book book) {
        normalizeBookFields(book);
        return bookRepository.save(book);
    }

    public List<Book> listBooks() {
        return bookRepository.findAll();
    }

    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
    }

    public Book updateBook(Long id, Book updatedBook) {
        Book existingBook = findById(id);

        existingBook.setTitle(TextUtils.cleanName(updatedBook.getTitle()));
        existingBook.setAuthor(TextUtils.cleanName(updatedBook.getAuthor()));
        existingBook.setCategory(TextUtils.cleanName(updatedBook.getCategory()));
        existingBook.setPublicationYear(TextUtils.cleanBasic(updatedBook.getPublicationYear()));
        existingBook.setTotalQuantity(updatedBook.getTotalQuantity());
        existingBook.setAvailableQuantity(updatedBook.getAvailableQuantity());
        existingBook.setRare(updatedBook.getRare());

        return bookRepository.save(existingBook);
    }

    public void deleteBook(Long id) {
        Book existingBook = findById(id);
        bookRepository.delete(existingBook);
    }

    private void normalizeBookFields(Book book) {
        book.setTitle(TextUtils.cleanName(book.getTitle()));
        book.setAuthor(TextUtils.cleanName(book.getAuthor()));
        book.setCategory(TextUtils.cleanName(book.getCategory()));
        book.setPublicationYear(TextUtils.cleanBasic(book.getPublicationYear()));
    }

    public static final class TextUtils {
        private TextUtils() {
        }

        public static String cleanBasic(String s) {
            if (s == null) return null;
            s = s.trim();
            s = s.replaceAll("\\s+", " ");
            return s.isEmpty() ? null : s;
        }

        public static String cleanName(String s) {
            s = cleanBasic(s);
            if (s == null) return null;

            s = s.toLowerCase();
            String[] parts = s.split(" ");
            StringBuilder out = new StringBuilder();

            for (int i = 0; i < parts.length; i++) {
                String p = parts[i];
                if (p.isEmpty()) continue;

                out.append(Character.toUpperCase(p.charAt(0)));
                if (p.length() > 1) {
                    out.append(p.substring(1));
                }

                if (i < parts.length - 1) {
                    out.append(" ");
                }
            }

            return out.toString();
        }
    }
}
