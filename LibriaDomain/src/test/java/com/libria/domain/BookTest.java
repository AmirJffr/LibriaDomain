package com.libria.domain;

import com.libria.domain.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookTest {

    private Book book;

    @BeforeEach
    void setUp() {
        book = new Book("978-0-7475-3269-9", "Harry Potter", "J.K. Rowling", 1997, "Fantasy", true);
    }

    @Test
    void isAvailable_shouldReturnTrueInitially() {
        assertTrue(book.isAvailable());
    }

    @Test
    void markAvailable_shouldSetBookAvailable() {
        book.markUnavailable();
        book.markAvailable();
        assertTrue(book.isAvailable());
    }

    @Test
    void markUnavailable_shouldSetBookUnavailable() {
        book.markUnavailable();
        assertFalse(book.isAvailable());
    }

    @Test
    void getTitle_shouldReturnCorrectTitle() {
        assertEquals("Harry Potter", book.getTitle());
    }

    @Test
    void getGenre_shouldReturnCorrectGenre() {
        assertEquals("Fantasy", book.getGenre());
    }

    @Test
    void getIsbn_shouldReturnCorrectIsbn() {
        assertEquals("978-0-7475-3269-9", book.getIsbn());
    }

    @Test
    void getAuthor_shouldReturnCorrectAuthor() {
        assertEquals("J.K. Rowling", book.getAuthor());
    }

    @Test
    void getYear_shouldReturnCorrectYear() {
        assertEquals(1997, book.getYear());
    }

    @Test
    void setTitle_shouldUpdateTitle() {
        book.setTitle("Harry Potter and the Chamber of Secrets");
        assertEquals("Harry Potter and the Chamber of Secrets", book.getTitle());
    }

    @Test
    void setAuthor_shouldUpdateAuthor() {
        book.setAuthor("Rowling");
        assertEquals("Rowling", book.getAuthor());
    }

    @Test
    void setYear_shouldUpdateYear() {
        book.setYear(2000);
        assertEquals(2000, book.getYear());
    }

    @Test
    void setGenre_shouldUpdateGenre() {
        book.setGenre("Adventure");
        assertEquals("Adventure", book.getGenre());
    }

    @Test
    void testEquals_shouldReturnTrueForSameIsbn() {
        Book same = new Book("978-0-7475-3269-9", "Other Title", "Someone", 2001, "Drama", true);
        assertEquals(book, same);
    }

    @Test
    void testEquals_shouldReturnFalseForDifferentIsbn() {
        Book different = new Book("111-111", "Other", "Other", 2000, "Other", true);
        assertNotEquals(book, different);
    }

    @Test
    void testHashCode_shouldBeEqualForSameIsbn() {
        Book same = new Book("978-0-7475-3269-9", "Other Title", "Someone", 2001, "Drama", true);
        assertEquals(book.hashCode(), same.hashCode());
    }

    @Test
    void testToString_shouldContainTitleAndAuthor() {
        String result = book.toString();
        assertTrue(result.contains("Harry Potter"));
        assertTrue(result.contains("J.K. Rowling"));
    }
}