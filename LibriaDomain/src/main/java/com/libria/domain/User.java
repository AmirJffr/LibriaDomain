package com.libria.domain;
import com.libria.exception.*;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class User {
    private String userId;
    private String name;
    private String email;
    private String password;
    private List<Book> downloadedBooks;


    public User() {}
    public User(String userId, String name, String email, String password) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.downloadedBooks = new ArrayList<>();
    }

    public abstract String getRole();

    public boolean login(String passwordInput) throws LoginException {
        if (passwordInput == null || passwordInput.isBlank()) {
            throw new LoginException("Le mot de passe ne peut pas être vide.");
        }
        if (!passwordInput.equals(this.password)) {
            throw new LoginException("Mot de passe incorrect.");
        }
        return true;
    }

    public void downloadBook(Book book) throws BookAlreadyExistException {
        if (downloadedBooks.contains(book)) {
            throw new BookAlreadyExistException("Book déja existente.");
        }
        downloadedBooks.add(book);
    }

    public void removeBook(Book book) throws BookNotFoundException {
        if(!downloadedBooks.contains(book)) {
            throw new BookNotFoundException("Ce livre n'est pas dans votre liste");
        }
        downloadedBooks.remove(book);
    }

    public List<Book> listDownloadedBooks() {
        return new ArrayList<>(downloadedBooks);
    }

    public boolean hasBook(Book book) {
        return downloadedBooks.contains(book);
    }

    public void setUserId(String userId) { this.userId = userId; }
    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public String getPassword() {
        return password;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }


    @Override
    public String toString() {
        return getRole() + "{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", downloadedBooks=" + downloadedBooks.size() +
                '}';
    }
}