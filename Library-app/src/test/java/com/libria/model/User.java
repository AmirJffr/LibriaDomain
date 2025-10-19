package com.libria.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User {
    private String userId;
    private String name;
    private String email;
    private String password;
    private List<Book> downloadedBooks;

    public User(String userId, String name, String email, String password) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.downloadedBooks = new ArrayList<>();
    }

    public boolean login(String passwordInput) {
        return this.password.equals(passwordInput);
    }

    public boolean downloadBook(Book book) {
        if (book == null || hasBook(book)) return false;
        downloadedBooks.add(book);
        book.markUnavailable();
        return true;
    }

    public boolean removeBook(Book book) {
        if (book == null) return false;
        boolean removed = downloadedBooks.remove(book);
        if (removed) book.markAvailable();
        return removed;
    }

    public List<Book> listDownloadedBooks() {
        return new ArrayList<>(downloadedBooks);
    }

    public boolean hasBook(Book book) {
        return downloadedBooks.contains(book);
    }


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
        return "User{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", downloadedBooks=" + downloadedBooks.size() +
                '}';
    }
}