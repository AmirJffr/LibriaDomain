package com.libria.model;

import com.libria.exception.AccessDeniedException;
import com.libria.exception.BookAlreadyExistException;
import com.libria.exception.BookNotFoundException;

public class Admin extends User {
    public Admin(String userId, String name, String email, String password) {
        super(userId, name, email, password);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }

    public void addBookToLibrary(Library lib, Book book) throws BookAlreadyExistException, AccessDeniedException {
        if (lib == null || book == null) {
            throw new IllegalArgumentException("Library ou Book ne peut pas être null.");
        }
        if (!"ADMIN".equals(this.getRole())) {
            throw new AccessDeniedException("Action réservée aux administrateurs.");
        }
        if (lib.containsBook(book.getIsbn())) {
            throw new BookAlreadyExistException("Livre déjà existant !");
        }
        lib.addBook(book);
    }

    public void removeBookFromLibrary(Library lib, String isbn) throws BookNotFoundException, AccessDeniedException {
        if (lib == null || isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("Library ou ISBN invalide.");
        }
        if (!"ADMIN".equals(this.getRole())) {
            throw new AccessDeniedException("Action réservée aux administrateurs.");
        }
        if (!lib.containsBook(isbn)) {
            throw new BookNotFoundException("Livre introuvable !");
        }
        lib.removeBook(isbn);
    }
}