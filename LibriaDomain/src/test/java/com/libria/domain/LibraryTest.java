package com.libria.domain;

import com.libria.exception.BookAlreadyExistException;
import com.libria.exception.BookNotFoundException;
import com.libria.exception.UserAlreadyExistException;
import com.libria.exception.UserNotFoundException;
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

        b1 = new Book(
                "ISBN-1",
                "Harry Potter",
                "J.K. Rowling",
                1997,
                "Fantasy",
                true,
                "cover/harry.png",
                "pdf/harry.pdf"
        );

        b2 = new Book(
                "ISBN-2",
                "The Lord of the Rings",
                "J.R.R. Tolkien",
                1954,
                "Fantasy",
                true,
                "cover/lotr.png",
                "pdf/lotr.pdf"
        );

        b3 = new Book(
                "ISBN-3",
                "To Kill a Mockingbird",
                "Harper Lee",
                1960,
                "Classic",
                true,
                "cover/mockingbird.png",
                "pdf/mockingbird.pdf"
        );

        admin = new Admin("A001", "Zak", "zak@libria.com", "1234");
        member = new Member("M001", "Amir", "amir@libria.com", "abcd");
    }

    @Test
    void registerUser_shouldAddUser_andPreventDuplicates() throws Exception {
        // 1. premier enregistrement OK
        library.registerUser(admin);
        assertEquals(admin, library.getUser("A001"));
        assertEquals(1, library.listUsers().size());

        // 2. même user -> UserAlreadyExistException
        assertThrows(UserAlreadyExistException.class, () -> library.registerUser(admin));
    }

    @Test
    void addBook_shouldAddOnce_andRejectDuplicates() {
        // cas invalide
        assertThrows(IllegalArgumentException.class, () -> library.addBook(null));

        // premier ajout OK
        library.addBook(b1);
        assertTrue(library.containsBook("ISBN-1"));
        assertEquals(b1, library.getBook("ISBN-1"));
        assertEquals(1, library.listBooks().size());

        // doublon interdit
        assertThrows(BookAlreadyExistException.class, () -> library.addBook(b1));
    }

    @Test
    void removeBook_shouldRemoveExisting_andFailOtherwise() {
        // paramètres invalides
        assertThrows(IllegalArgumentException.class, () -> library.removeBook(null));
        assertThrows(IllegalArgumentException.class, () -> library.removeBook("   "));

        // ISBN inconnu
        assertThrows(BookNotFoundException.class, () -> library.removeBook("UNKNOWN"));

        // ajout puis suppression
        library.addBook(b2);
        assertTrue(library.containsBook("ISBN-2"));

        library.removeBook("ISBN-2");
        assertFalse(library.containsBook("ISBN-2"));
        assertEquals(0, library.listBooks().size());
    }

    @Test
    void containsBook_shouldReturnTrueOnlyIfPresent() {
        assertFalse(library.containsBook("ISBN-1"));

        library.addBook(b1);
        assertTrue(library.containsBook("ISBN-1"));
        assertFalse(library.containsBook("NOPE"));
    }

    @Test
    void getBook_shouldReturnTheRightBookOrThrow() throws Exception {
        assertThrows(BookNotFoundException.class, () -> library.getBook("ISBN-1"));

        library.addBook(b1);
        Book found = library.getBook("ISBN-1");

        assertNotNull(found);
        assertEquals("Harry Potter", found.getTitle());
        assertEquals("cover/harry.png", found.getCoverImage());
        assertEquals("pdf/harry.pdf", found.getPdf());
    }

    @Test
    void searchByTitle_shouldBeCaseInsensitive_andPartial() {
        library.addBook(b1);
        library.addBook(b2);
        library.addBook(b3);

        // "harry" -> 1 résultat (Harry Potter)
        List<Book> result1 = library.searchByTitle("harry");
        assertEquals(1, result1.size());
        assertEquals("ISBN-1", result1.get(0).getIsbn());

        // "the" -> 1 résultat (The Lord of the Rings)
        List<Book> result2 = library.searchByTitle("the");
        assertEquals(1, result2.size());
        assertEquals("ISBN-2", result2.get(0).getIsbn());

        // recherche vide -> renvoie tous les livres (comportement actuel)
        List<Book> result3 = library.searchByTitle("");
        assertEquals(3, result3.size());
    }

    @Test
    void searchByGenre_shouldBeCaseInsensitive() {
        library.addBook(b1);
        library.addBook(b2);
        library.addBook(b3);

        List<Book> fantasy = library.searchByGenre("FANTASY");
        assertEquals(2, fantasy.size());
        assertTrue(fantasy.stream().anyMatch(b -> b.getIsbn().equals("ISBN-1")));
        assertTrue(fantasy.stream().anyMatch(b -> b.getIsbn().equals("ISBN-2")));

        List<Book> classic = library.searchByGenre("classic");
        assertEquals(1, classic.size());
        assertEquals("ISBN-3", classic.get(0).getIsbn());
    }

    @Test
    void listBooks_shouldReturnDefensiveCopy() {
        library.addBook(b1);
        library.addBook(b2);

        List<Book> copy = library.listBooks();
        assertEquals(2, copy.size());

        // On modifie la copie
        copy.clear();

        // La vraie liste interne ne doit pas bouger
        assertEquals(2, library.listBooks().size());
    }

    @Test
    void getUser_and_listUsers_shouldWorkAndBeDefensive() throws Exception, UserNotFoundException {
        library.registerUser(admin);
        library.registerUser(member);

        assertEquals(member, library.getUser("M001"));
        assertThrows(UserNotFoundException.class, () -> library.getUser("NOPE"));

        List<User> usersCopy = library.listUsers();
        assertEquals(2, usersCopy.size());

        // la copie est modifiable mais pas l'interne
        usersCopy.clear();
        assertEquals(2, library.listUsers().size());
    }

    @Test
    void toString_shouldExposeCounts() throws Exception {
        library.addBook(b1);
        library.addBook(b2);
        library.registerUser(admin);

        String s = library.toString();
        assertTrue(s.contains("books=2"));
        assertTrue(s.contains("users=1"));
    }
}