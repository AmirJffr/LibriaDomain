package com.libria.model;

import com.libria.exception.BookAlreadyExistException;
import com.libria.exception.BookNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MemberTest {

    private Member member;
    private Book book1;
    private Book book2;

    @BeforeEach
    void setUp() {
        member = new Member("M001", "Amir", "amir@libria.com", "abcd");
        book1 = new Book("ISBN-1", "Harry Potter", "J.K. Rowling", 1997, "Fantasy", true);
        book2 = new Book("ISBN-2", "The Lord of the Rings", "J.R.R. Tolkien", 1954, "Fantasy", true);
    }

    @Test
    void getRole_shouldReturnMEMBER() {
        assertEquals("MEMBER", member.getRole());
    }

    @Test
    void downloadBook_shouldAddBookToDownloadedList() throws Exception {
        member.downloadBook(book1);
        assertTrue(member.listDownloadedBooks().contains(book1));
    }

    @Test
    void downloadBook_shouldThrowIfBookAlreadyExists() throws Exception {
        member.downloadBook(book1);
        assertThrows(BookAlreadyExistException.class, () -> member.downloadBook(book1));
    }

    @Test
    void removeBook_shouldRemoveDownloadedBook() throws Exception {
        member.downloadBook(book1);
        member.removeBook(book1);
        assertFalse(member.listDownloadedBooks().contains(book1));
    }

    @Test
    void removeBook_shouldThrowIfBookNotDownloaded() {
        assertThrows(BookNotFoundException.class, () -> member.removeBook(book2));
    }
}