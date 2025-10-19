package com.libria.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Library {
    private Map<String, Book> catalogByIsbn;
    private Map<String, User> usersById;


    public Library() {
        this.catalogByIsbn = new HashMap<>();
        this.usersById = new HashMap<>();
    }


    public boolean registerUser(User user) {
        if (user == null || usersById.containsKey(user.getUserId())) {
            return false;
        }
        usersById.put(user.getUserId(), user);
        return true;
    }


    public boolean addBook(Book book) {
        if (book == null || catalogByIsbn.containsKey(book.getIsbn())) {
            return false;
        }
        catalogByIsbn.put(book.getIsbn(), book);
        return true;
    }

    public boolean removeBook(String isbn) {
        if (!catalogByIsbn.containsKey(isbn)) {
            return false;
        }
        catalogByIsbn.remove(isbn);
        return true;
    }

    public Book getBook(String isbn) {
        return catalogByIsbn.get(isbn);
    }


    public List<Book> searchByTitle(String title) {
        List<Book> result = new ArrayList<>();
        for (Book book : catalogByIsbn.values()) {
            if (book.getTitle().toLowerCase().contains(title.toLowerCase())) {
                result.add(book);
            }
        }
        return result;
    }

    public List<Book> searchByGenre(String genre) {
        List<Book> result = new ArrayList<>();
        for (Book book : catalogByIsbn.values()) {
            if (book.getGenre().equalsIgnoreCase(genre)) {
                result.add(book);
            }
        }
        return result;
    }

    public List<Book> listBooks() {
        return new ArrayList<>(catalogByIsbn.values());
    }


    public User getUser(String userId) {
        return usersById.get(userId);
    }

    public List<User> listUsers() {
        return new ArrayList<>(usersById.values());
    }


    @Override
    public String toString() {
        return "Library{" +
                "books=" + catalogByIsbn.size() +
                ", users=" + usersById.size() +
                '}';
    }
}