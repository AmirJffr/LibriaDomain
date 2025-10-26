package com.libria.domain;

import com.libria.exception.BookAlreadyExistException;
import com.libria.exception.BookNotFoundException;
import com.libria.domain.Book;
import com.libria.domain.Member;
import com.libria.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.security.auth.login.LoginException;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;
    private Book book1, book2;

    @BeforeEach
    void setUp() {
        user = new Member("U001", "Zakaria", "zakaria@libria.com", "1234");
        book1 = new Book("ISBN-1", "Harry Potter", "J.K. Rowling", 1997, "Fantasy", true,"xx","xx");
        book2 = new Book("ISBN-2", "The Lord of the Rings", "J.R.R. Tolkien", 1954, "Fantasy", true,"xx","xx");
    }

    @Test
    void getRole_shouldReturnMEMBER() {
        assertEquals("MEMBER", user.getRole());
    }

    @Test
    void login_shouldReturnTrueForCorrectPassword() throws Exception {
        assertTrue(user.login("1234"));
    }

    @Test
    void login_shouldThrowForWrongPassword() {
        assertThrows(LoginException.class, () -> user.login("wrong"));
    }

    @Test
    void login_shouldThrowForEmptyPassword() {
        assertThrows(LoginException.class, () -> user.login(""));
        assertThrows(LoginException.class, () -> user.login(null));
    }

    @Test
    void downloadBook_shouldAddBook() throws Exception {
        user.downloadBook(book1);
        assertTrue(user.listDownloadedBooks().contains(book1));
    }

    @Test
    void downloadBook_shouldThrowIfAlreadyExists() throws Exception {
        user.downloadBook(book1);
        assertThrows(BookAlreadyExistException.class, () -> user.downloadBook(book1));
    }

    @Test
    void removeBook_shouldRemoveSuccessfully() throws Exception {
        user.downloadBook(book1);
        user.removeBook(book1);
        assertFalse(user.listDownloadedBooks().contains(book1));
    }

    @Test
    void removeBook_shouldThrowIfBookNotFound() {
        assertThrows(BookNotFoundException.class, () -> user.removeBook(book2));
    }

    @Test
    void listDownloadedBooks_shouldReturnCopy() throws Exception {
        user.downloadBook(book1);
        var list = user.listDownloadedBooks();
        list.clear(); // ne doit pas affecter la vraie liste
        assertEquals(1, user.listDownloadedBooks().size());
    }

    @Test
    void hasBook_shouldReturnTrueIfDownloaded() throws Exception {
        user.downloadBook(book1);
        assertTrue(user.hasBook(book1));
        assertFalse(user.hasBook(book2));
    }

    @Test
    void getUserId_shouldReturnCorrectValue() {
        assertEquals("U001", user.getUserId());
    }

    @Test
    void getNameAndSetName_shouldWork() {
        assertEquals("Zakaria", user.getName());
        user.setName("Amir");
        assertEquals("Amir", user.getName());
    }

    @Test
    void getEmailAndSetEmail_shouldWork() {
        assertEquals("zakaria@libria.com", user.getEmail());
        user.setEmail("new@libria.com");
        assertEquals("new@libria.com", user.getEmail());
    }

    @Test
    void changePassword_shouldUpdatePassword() throws Exception {
        user.changePassword("newpass");
        assertTrue(user.login("newpass"));
    }

    @Test
    void testEquals_shouldReturnTrueForSameUserId() {
        User same = new Member("U001", "Other", "other@libria.com", "0000");
        assertEquals(user, same);
    }

    @Test
    void testEquals_shouldReturnFalseForDifferentUserId() {
        User other = new Member("U999", "Other", "other@libria.com", "0000");
        assertNotEquals(user, other);
    }

    @Test
    void testHashCode_shouldBeSameForSameId() {
        User same = new Member("U001", "Other", "other@libria.com", "0000");
        assertEquals(user.hashCode(), same.hashCode());
    }

    @Test
    void testToString_shouldContainNameAndEmail() {
        String str = user.toString();
        assertTrue(str.contains("Zakaria"));
        assertTrue(str.contains("zakaria@libria.com"));
    }
}