package com.libria.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookTest {

    private Book book;

    @BeforeEach
    void setUp() {
        book = new Book(
                "978-0-7475-3269-9",
                "Harry Potter",
                "J.K. Rowling",
                1997,
                "Fantasy",
                true,
                "/covers/hp1.jpg",
                "/pdf/hp1.pdf"
        );
    }

    @Test
    void isAvailable_shouldReturnTrueInitially() {
        assertTrue(book.isAvailable());
    }

    @Test
    void setAvailable_shouldSetBookAvailable() {
        book.setUnavailable(false);
        book.setAvailable(true);
        assertTrue(book.isAvailable());
    }

    @Test
    void setUnavailable_shouldSetBookUnavailable() {
        book.setUnavailable(false);
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
    void coverImage_shouldBeStoredAndMutable() {
        assertEquals("/covers/hp1.jpg", book.getCoverImage());
        book.setCoverImage("/covers/new.jpg");
        assertEquals("/covers/new.jpg", book.getCoverImage());
    }

    @Test
    void pdf_shouldBeStoredAndMutable() {
        assertEquals("/pdf/hp1.pdf", book.getPdf());
        book.setPdf("/pdf/new.pdf");
        assertEquals("/pdf/new.pdf", book.getPdf());
    }

    @Test
    void testEquals_shouldReturnTrueForSameIsbn() {
        Book same = new Book(
                "978-0-7475-3269-9",
                "Some other title",
                "Whoever",
                2001,
                "Drama",
                false,
                "/covers/other.jpg",
                "/pdf/other.pdf"
        );
        assertEquals(book, same);
    }

    @Test
    void testEquals_shouldReturnFalseForDifferentIsbn() {
        Book different = new Book(
                "111-111",
                "Other",
                "Other",
                2000,
                "Other",
                true,
                "/covers/x.jpg",
                "/pdf/x.pdf"
        );
        assertNotEquals(book, different);
    }

    @Test
    void testHashCode_shouldBeEqualForSameIsbn() {
        Book same = new Book(
                "978-0-7475-3269-9",
                "Another title",
                "Another Author",
                2010,
                "Something",
                true,
                "/covers/y.jpg",
                "/pdf/y.pdf"
        );
        assertEquals(book.hashCode(), same.hashCode());
    }

    @Test
    void testToString_shouldContainTitleAndAuthor() {
        String result = book.toString();
        assertTrue(result.contains("Harry Potter"));
        assertTrue(result.contains("J.K. Rowling"));
        assertTrue(result.contains("available=true"));
    }
}