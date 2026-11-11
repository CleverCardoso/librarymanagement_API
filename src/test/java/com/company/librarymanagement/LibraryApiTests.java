package com.company.librarymanagement;

import com.company.librarymanagement.model.Loan;
import com.company.librarymanagement.repository.LoanRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LibraryApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoanRepository loanRepository;

    @Test
    void createsBookWithAllCopiesAvailableAndCleanText() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "  dom   casmurro ", "author": "machado de assis", "category": "romance",
                                 "publicationYear": 1899, "totalQuantity": 3}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Dom Casmurro"))
                .andExpect(jsonPath("$.author").value("Machado De Assis"))
                .andExpect(jsonPath("$.totalQuantity").value(3))
                .andExpect(jsonPath("$.availableQuantity").value(3))
                .andExpect(jsonPath("$.rare").value(false));
    }

    @Test
    void rejectsBookWithoutTitle() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"author": "Machado de Assis", "totalQuantity": 1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("título")));
    }

    @Test
    void returnsNotFoundForUnknownBook() throws Exception {
        mockMvc.perform(get("/books/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Livro não encontrado."));
    }

    @Test
    void rejectsDuplicatedEmail() throws Exception {
        createUser("Ana Souza", "ana@exemplo.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Outra Ana", "email": "ANA@exemplo.com", "userType": "TEACHER"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsInvalidUserType() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Ana", "email": "ana@exemplo.com", "userType": "DIRETOR"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatesOnlyTheFieldsThatWereSent() throws Exception {
        long userId = createUser("Ana Souza", "ana@exemplo.com");

        mockMvc.perform(put("/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"phone": "61 99999-0000", "userType": "TEACHER"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana Souza"))
                .andExpect(jsonPath("$.email").value("ana@exemplo.com"))
                .andExpect(jsonPath("$.phone").value("61 99999-0000"))
                .andExpect(jsonPath("$.userType").value("TEACHER"));
    }

    @Test
    void loanTakesOneCopyAndReturnGivesItBack() throws Exception {
        long userId = createUser("Ana Souza", "ana@exemplo.com");
        long bookId = createBook("Dom Casmurro", 1, false);

        long loanId = createLoan(userId, bookId);

        mockMvc.perform(get("/books/" + bookId))
                .andExpect(jsonPath("$.availableQuantity").value(0));

        // Sem cópias disponíveis, um segundo empréstimo é recusado
        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loanJson(userId, bookId)))
                .andExpect(status().isConflict());

        mockMvc.perform(patch("/loans/" + loanId + "/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"))
                .andExpect(jsonPath("$.returnDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.fineAmount").value(0));

        mockMvc.perform(get("/books/" + bookId))
                .andExpect(jsonPath("$.availableQuantity").value(1));

        // Não dá para devolver o mesmo empréstimo duas vezes
        mockMvc.perform(patch("/loans/" + loanId + "/return"))
                .andExpect(status().isConflict());
    }

    @Test
    void rareBookHasShorterLoanPeriod() throws Exception {
        long userId = createUser("Ana Souza", "ana@exemplo.com");
        long commonBookId = createBook("Dom Casmurro", 1, false);
        long rareBookId = createBook("Os Lusiadas", 1, true);

        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loanJson(userId, commonBookId)))
                .andExpect(jsonPath("$.dueDate").value(LocalDate.now().plusDays(14).toString()));

        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loanJson(userId, rareBookId)))
                .andExpect(jsonPath("$.dueDate").value(LocalDate.now().plusDays(7).toString()));
    }

    @Test
    void lateReturnShowsAsOverdueAndChargesFinePerDay() throws Exception {
        long userId = createUser("Ana Souza", "ana@exemplo.com");
        long bookId = createBook("Dom Casmurro", 1, false);
        long loanId = createLoan(userId, bookId);

        // Simula um empréstimo que venceu há 3 dias
        Loan loan = loanRepository.findById(loanId).orElseThrow();
        loan.setDueDate(LocalDate.now().minusDays(3));
        loanRepository.saveAndFlush(loan);

        mockMvc.perform(get("/loans/active"))
                .andExpect(jsonPath("$[0].status").value("OVERDUE"));

        mockMvc.perform(get("/reports/users/overdue"))
                .andExpect(jsonPath("$[*].email", hasItem("ana@exemplo.com")));

        mockMvc.perform(patch("/loans/" + loanId + "/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"))
                .andExpect(jsonPath("$.fineAmount").value(3.0));

        mockMvc.perform(get("/reports/users/overdue"))
                .andExpect(jsonPath("$[*].email", not(hasItem("ana@exemplo.com"))));
    }

    @Test
    void cannotDeleteUserOrBookWithLoans() throws Exception {
        long userId = createUser("Ana Souza", "ana@exemplo.com");
        long bookId = createBook("Dom Casmurro", 2, false);
        createLoan(userId, bookId);

        mockMvc.perform(delete("/users/" + userId)).andExpect(status().isConflict());
        mockMvc.perform(delete("/books/" + bookId)).andExpect(status().isConflict());
    }

    @Test
    void totalQuantityCannotGoBelowBorrowedCopies() throws Exception {
        long userId = createUser("Ana Souza", "ana@exemplo.com");
        long bookId = createBook("Dom Casmurro", 2, false);
        createLoan(userId, bookId);

        mockMvc.perform(put("/books/" + bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"totalQuantity": 0}
                                """))
                .andExpect(status().isConflict());

        mockMvc.perform(put("/books/" + bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"totalQuantity": 5}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuantity").value(5))
                .andExpect(jsonPath("$.availableQuantity").value(4));
    }

    @Test
    void rankingPutsMostBorrowedBookFirst() throws Exception {
        long userId = createUser("Ana Souza", "ana@exemplo.com");
        long lessBorrowedId = createBook("Dom Casmurro", 5, false);
        long mostBorrowedId = createBook("Vidas Secas", 5, false);

        createLoan(userId, lessBorrowedId);
        createLoan(userId, mostBorrowedId);
        createLoan(userId, mostBorrowedId);

        mockMvc.perform(get("/reports/books/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(mostBorrowedId))
                .andExpect(jsonPath("$[1].id").value(lessBorrowedId));
    }

    private long createUser(String name, String email) throws Exception {
        String body = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "%s", "email": "%s", "userType": "STUDENT"}
                                """.formatted(name, email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return readId(body);
    }

    private long createBook(String title, int totalQuantity, boolean rare) throws Exception {
        String body = mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "%s", "author": "Autor Teste", "totalQuantity": %d, "rare": %b}
                                """.formatted(title, totalQuantity, rare)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return readId(body);
    }

    private long createLoan(long userId, long bookId) throws Exception {
        String body = mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loanJson(userId, bookId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return readId(body);
    }

    private String loanJson(long userId, long bookId) {
        return """
                {"userId": %d, "bookId": %d}
                """.formatted(userId, bookId);
    }

    private long readId(String json) {
        Number id = JsonPath.read(json, "$.id");
        return id.longValue();
    }
}
