package com.libria.domain;
import com.libria.exception.*;

import java.util.*;


public class Library {
    private Map<String, Book> catalogByIsbn;
    private Map<String, User> usersById;

    public Library() {
        this.catalogByIsbn = new HashMap<>();
        this.usersById = new HashMap<>();
    }


    public String root() {
        return "Bienvenue chez Libria ;)";
    }

    public void registerUser(User user) throws UserAlreadyExistException {
        if (usersById.containsKey(user.getUserId())) {
            throw new UserAlreadyExistException("Cet utilisateur existe déja !");
        }
        usersById.put(user.getUserId(), user);
    }

    void addBook(Book book) {
        if (book == null) throw new IllegalArgumentException("Book null");
        if (catalogByIsbn.containsKey(book.getIsbn()))
            throw new BookAlreadyExistException("Livre déjà existant !");
        catalogByIsbn.put(book.getIsbn(), book);
    }

    void removeBook(String isbn) {
        if (isbn == null || isbn.isBlank()) throw new IllegalArgumentException("ISBN invalide");
        if (!catalogByIsbn.containsKey(isbn))
            throw new BookNotFoundException("Livre introuvable !");
        catalogByIsbn.remove(isbn);
    }

    public void removeUser(String userId) throws UserNotFoundException {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("ID utilisateur invalide.");
        }
        // Vérifie si l'utilisateur existe
        if (!usersById.containsKey(userId)) {
            throw new UserNotFoundException("Utilisateur introuvable : " + userId);
        }
        // Supprime l'utilisateur de la collection
        usersById.remove(userId);
    }

    boolean containsBook(String isbn) {
        return catalogByIsbn.containsKey(isbn);
    }

    public Book getBook(String isbn) throws BookNotFoundException {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("ISBN invalide.");
        }

        Book book = catalogByIsbn.get(isbn);

        if (book == null) {
            throw new BookNotFoundException("Aucun livre trouvé avec l’ISBN : " + isbn);
        }

        return book;
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

    public List<Book> searchByAuthor(String author) {
        List<Book> result = new ArrayList<>();
        for (Book book : catalogByIsbn.values()) {
            if (book.getAuthor().equalsIgnoreCase(author)) {
                result.add(book);
            }
        }
        return result;
    }


    public List<Book> listBooks() {
        return new ArrayList<>(catalogByIsbn.values());
    }

    public User getUser(String userId) throws UserNotFoundException {
        User user = usersById.get(userId);
        if (user == null) {
            throw new UserNotFoundException("Utilisateur avec l'ID " + userId + " introuvable.");
        }
        return user;
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