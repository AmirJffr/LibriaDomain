package com.libria.domain;

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


    public void updateBookInLibrary(Library lib, String isbn, Book updated)
            throws BookNotFoundException, AccessDeniedException {
        if (lib == null || isbn == null || isbn.isBlank() || updated == null) {
            throw new IllegalArgumentException("Paramètres invalides.");
        }
        if (!"ADMIN".equals(this.getRole())) {
            throw new AccessDeniedException("Action réservée aux administrateurs.");
        }

        Book existing = lib.getBook(isbn);
        if (existing == null) {
            throw new BookNotFoundException("Livre introuvable !");
        }

        // Mise à jour partielle : on ne modifie que les champs fournis
        if (updated.getTitle() != null && !updated.getTitle().isBlank()) {
            existing.setTitle(updated.getTitle());
        }
        if (updated.getAuthor() != null && !updated.getAuthor().isBlank()) {
            existing.setAuthor(updated.getAuthor());
        }
        if (updated.getGenre() != null && !updated.getGenre().isBlank()) {
            existing.setGenre(updated.getGenre());
        }
        if (updated.getYear() > 0) {
            existing.setYear(updated.getYear());
        }
        if (updated.getPdf() != null && !updated.getPdf().isBlank()) {
            existing.setPdf(updated.getPdf());
        }
        if (updated.getCoverImage() != null && !updated.getCoverImage().isBlank()) {
            existing.setCoverImage(updated.getCoverImage());
        }

        if (updated.isAvailable() != existing.isAvailable()) {
            if (updated.isAvailable()) {
                existing.markAvailable();
            } else {
                existing.markUnavailable();
            }
        }
    }

    public void setBookAvailability(Library lib, String isbn, boolean available)
            throws BookNotFoundException, AccessDeniedException {
        if (lib == null || isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("Paramètres invalides.");
        }
        if (!"ADMIN".equals(this.getRole())) {
            throw new AccessDeniedException("Action réservée aux administrateurs.");
        }

        Book existing = lib.getBook(isbn);
        if (existing == null) {
            throw new BookNotFoundException("Livre introuvable !");
        }

        if (available) existing.markAvailable();
        else existing.markUnavailable();
    }
}