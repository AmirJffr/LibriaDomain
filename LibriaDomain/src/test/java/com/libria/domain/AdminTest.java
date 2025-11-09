package com.libria.domain;

import com.libria.exception.*;
import com.libria.domain.Admin;
import com.libria.domain.Book;
import com.libria.domain.Library;
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
        book1 = new Book("978-0-7475-3269-9", "Harry Potter", "J.K. Rowling", 1997, "Fantasy", true,"xx","xx");
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
    void updateBookInLibrary_shouldUpdateOnlyProvidedFields() throws Exception {
        admin.addBookToLibrary(library, book1);
        Book updated = new Book(book1.getIsbn(), "Harry Potter Updated", "New Author", 2000, "Adventure", true, "newCover", "newPdf");

        admin.updateBookInLibrary(library, book1.getIsbn(), updated);

        Book found = library.getBook(book1.getIsbn());
        assertEquals("Harry Potter Updated", found.getTitle());
        assertEquals("New Author", found.getAuthor());
        assertEquals("Adventure", found.getGenre());
        assertEquals(2000, found.getYear());
        assertEquals("newCover", found.getCoverImage());
        assertEquals("newPdf", found.getPdf());
    }

    @Test
    void updateBookInLibrary_shouldThrowIfBookNotFound() {
        assertThrows(BookNotFoundException.class, () -> admin.updateBookInLibrary(library, "INVALID_ISBN", book1));
    }
    @Test
    void updateBookInLibrary_shouldDenyWhenNotAdmin() throws Exception {
        Admin fake = new FakeAdmin("F003", "Fake3", "fake3@libria.com", "pwd");
        admin.addBookToLibrary(library, book1);
        Book updated = new Book(book1.getIsbn(), "HP", "X", 2000, "New", true, "xx", "xx");

        assertThrows(AccessDeniedException.class,
                () -> fake.updateBookInLibrary(library, book1.getIsbn(), updated));
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
    void setBookAvailability_shouldSetUnavailable() throws Exception {
        admin.addBookToLibrary(library, book1);
        admin.setBookAvailability(library, book1.getIsbn(), false);

        assertFalse(library.getBook(book1.getIsbn()).isAvailable());
    }

    @Test
    void setBookAvailability_shouldSetAvailable() throws Exception {
        admin.addBookToLibrary(library, book1);
        admin.setBookAvailability(library, book1.getIsbn(), true);

        assertTrue(library.getBook(book1.getIsbn()).isAvailable());
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