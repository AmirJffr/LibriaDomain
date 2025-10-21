package com.libria.model;

import com.libria.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdminTest {

    private Library library;
    private Admin admin;
    private Book book1;

    // Faux admin qui n'a pas le rôle ADMIN
    static class FakeAdmin extends Admin {
        public FakeAdmin(String id, String name, String email, String pwd) {
            super(id, name, email, pwd);
        }
        @Override public String getRole() { return "MEMBER"; }
    }

    @BeforeEach
    void setUp() {
        library = new Library();
        admin = new Admin("A001", "Zakaria", "zakaria@libria.com", "1234");
        book1 = new Book("978-0-7475-3269-9", "Harry Potter", "J.K. Rowling", 1997, "Fantasy", true);
    }

    @Test
    void getRole_shouldReturnADMIN() {
        assertEquals("ADMIN", admin.getRole());
    }

    @Test
    void addBookToLibrary_shouldAddBookSuccessfully() throws Exception {
        admin.addBookToLibrary(library, book1);
        assertTrue(library.listBooks().contains(book1));
    }

    @Test
    void addBookToLibrary_shouldThrowIfBookAlreadyExists() throws Exception {
        admin.addBookToLibrary(library, book1);
        assertThrows(BookAlreadyExistException.class, () -> admin.addBookToLibrary(library, book1));
    }

    @Test
    void addBookToLibrary_shouldDenyWhenNotAdmin() {
        Admin fake = new FakeAdmin("F001", "Fake", "fake@libria.com", "pwd");
        assertThrows(AccessDeniedException.class, () -> fake.addBookToLibrary(library, book1));
    }

    @Test
    void addBookToLibrary_shouldValidateArguments() {
        assertThrows(IllegalArgumentException.class, () -> admin.addBookToLibrary(null, book1));
        assertThrows(IllegalArgumentException.class, () -> admin.addBookToLibrary(library, null));
    }

    @Test
    void removeBookFromLibrary_shouldRemoveExistingBook() throws Exception {
        admin.addBookToLibrary(library, book1);
        admin.removeBookFromLibrary(library, book1.getIsbn());
        assertFalse(library.containsBook(book1.getIsbn()));
    }

    @Test
    void removeBookFromLibrary_shouldThrowIfBookNotFound() {
        assertThrows(BookNotFoundException.class, () -> admin.removeBookFromLibrary(library, "1234"));
    }

    @Test
    void removeBookFromLibrary_shouldDenyWhenNotAdmin() {
        Admin fake = new FakeAdmin("F002", "Fake2", "fake2@libria.com", "pwd");
        assertThrows(AccessDeniedException.class, () -> fake.removeBookFromLibrary(library, book1.getIsbn()));
    }

    @Test
    void removeBookFromLibrary_shouldValidateArguments() {
        assertThrows(IllegalArgumentException.class, () -> admin.removeBookFromLibrary(null, "x"));
        assertThrows(IllegalArgumentException.class, () -> admin.removeBookFromLibrary(library, null));
        assertThrows(IllegalArgumentException.class, () -> admin.removeBookFromLibrary(library, "  "));
    }
}