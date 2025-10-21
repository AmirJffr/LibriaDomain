package com.libria.model;

import com.libria.exception.UserAlreadyExistException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LibraryTest {

    private Library library;
    private Book b1, b2, b3;
    private Admin admin;
    private Member member;

    @BeforeEach
    void setUp() {
        library = new Library();
        b1 = new Book("ISBN-1", "Harry Potter", "J.K. Rowling", 1997, "Fantasy", true);
        b2 = new Book("ISBN-2", "The Lord of the Rings", "J.R.R. Tolkien", 1954, "Fantasy", true);
        b3 = new Book("ISBN-3", "To Kill a Mockingbird", "Harper Lee", 1960, "Classic", true);
        admin = new Admin("A001", "Zak", "zak@libria.com", "1234");
        member = new Member("M001", "Amir", "amir@libria.com", "abcd");
    }

    @Test
    void registerUser() throws Exception {
        library.registerUser(admin);
        assertEquals(admin, library.getUser("A001"));
        assertEquals(1, library.listUsers().size());

        //On refais linsertion pour verifier que lexception est bien levée
        assertThrows(UserAlreadyExistException.class, () -> library.registerUser(admin));
    }

    @Test
    void addBook() {
        // invalide
        assertThrows(IllegalArgumentException.class, () -> library.addBook(null));

        // premier ajout OK
        library.addBook(b1);
        assertTrue(library.containsBook("ISBN-1"));
        assertEquals(b1, library.getBook("ISBN-1"));
        assertEquals(1, library.listBooks().size());

        // doublon => exception
        assertThrows(com.libria.exception.BookAlreadyExistException.class, () -> library.addBook(b1));
    }

    @Test
    void removeBook() {
        // arguments invalides
        assertThrows(IllegalArgumentException.class, () -> library.removeBook(null));
        assertThrows(IllegalArgumentException.class, () -> library.removeBook("   "));
        // ISBN non connu
        assertThrows(com.libria.exception.BookNotFoundException.class, () -> library.removeBook("UNKNOWN"));
        // ajout puis suppression
        library.addBook(b2);
        assertTrue(library.containsBook("ISBN-2"));
        library.removeBook("ISBN-2");
        assertFalse(library.containsBook("ISBN-2"));
        assertEquals(0, library.listBooks().size());
    }

    @Test
    void containsBook() {
        assertFalse(library.containsBook("ISBN-1"));
        library.addBook(b1);
        assertTrue(library.containsBook("ISBN-1"));
        assertFalse(library.containsBook("NOPE"));
    }

    @Test
    void getBook() {
        assertNull(library.getBook("ISBN-1"));
        library.addBook(b1);
        assertEquals("Harry Potter", library.getBook("ISBN-1").getTitle());
    }

    @Test
    void searchByTitle() {
        library.addBook(b1);
        library.addBook(b2);
        library.addBook(b3);

        List<Book> result1 = library.searchByTitle("harry");
        assertEquals(1, result1.size());
        assertEquals("ISBN-1", result1.get(0).getIsbn());

        List<Book> result2 = library.searchByTitle("the");
        assertEquals(1, result2.size()); // "The Lord of the Rings"
    }

    @Test
    void searchByGenre() {
        library.addBook(b1);
        library.addBook(b2);
        library.addBook(b3);

        List<Book> fantasy = library.searchByGenre("FANTASY");
        assertEquals(2, fantasy.size());

        List<Book> classic = library.searchByGenre("classic");
        assertEquals(1, classic.size());
        assertEquals("ISBN-3", classic.get(0).getIsbn());
    }

    @Test
    void listBooks() {
        library.addBook(b1);
        library.addBook(b2);
        List<Book> copy = library.listBooks();
        assertEquals(2, copy.size());

        // verifier si la vrais liste de la lib change, parceque vaudrais pas que ca arrive. Quand on recup al liste
        // des livre, on recup un copie
        copy.clear();
        assertEquals(2, library.listBooks().size());
    }

    @Test
    void getUser_and_listUsers() throws Exception {
        library.registerUser(admin);
        library.registerUser(member);

        assertEquals(member, library.getUser("M001"));
        assertNull(library.getUser("NOPE"));

        List<User> usersCopy = library.listUsers();
        assertEquals(2, usersCopy.size());
        usersCopy.clear();
        assertEquals(2, library.listUsers().size()); // defensive copy
    }

    @Test
    void testToString() throws Exception {
        library.addBook(b1);
        library.addBook(b2);
        library.registerUser(admin);

        String s = library.toString();
        assertTrue(s.contains("books=2"));
        assertTrue(s.contains("users=1"));
    }
}