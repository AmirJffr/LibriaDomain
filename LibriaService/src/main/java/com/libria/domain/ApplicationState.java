package com.libria.domain;

import com.libria.exception.*;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import javax.security.auth.login.LoginException;

@ApplicationScoped
public class ApplicationState {

    private Library library;

    public Library getLibrary() {
        return library;
    }

    @PostConstruct
    public void init() {
        library = new Library();
        seedData();
    }

    public void addBook(String userId, Book book) throws AccessDeniedException, BookAlreadyExistException {
        User user = library.getUser(userId);
        if (!(user instanceof Admin)) throw new AccessDeniedException("Seul un administrateur peut ajouter des livres.");
        ((Admin) user).addBookToLibrary(library, book);
    }

    public void removeBook(String userId, String isbn) throws AccessDeniedException, BookNotFoundException {
        User user = library.getUser(userId);
        if (!(user instanceof Admin)) throw new AccessDeniedException("Seul un administrateur peut supprimer des livres.");
        ((Admin) user).removeBookFromLibrary(library, isbn);
    }

    public void updateBook(String userId, String isbn, Book updated) throws AccessDeniedException, BookNotFoundException {
        User user = library.getUser(userId);
        if (!(user instanceof Admin)) throw new AccessDeniedException("Seul un administrateur peut modifier des livres.");
        ((Admin) user).updateBookInLibrary(library, isbn, updated);
    }

    public User authenticate(String email, String password)
            throws UserNotFoundException, LoginException, IllegalArgumentException {

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Email et mot de passe requis.");
        }


        User u = library.listUsers().stream()
                .filter(user -> email.equalsIgnoreCase(user.getEmail()))
                .findFirst()
                .orElse(null);

        if (u == null) {
            throw new UserNotFoundException("Utilisateur introuvable pour cet email.");
        }


        try {
            u.login(password); // peut lever LoginException de ton modèle
            return u;
        } catch (LoginException e) {
            throw new LoginException("Mot de passe incorrect.");
        }
    }

    // --- Seed complet  ---
    private void seedData() {
        Admin admin = new Admin("AD01", "Super Admin", "admin@libria.com", "libria123");

        try {
            String PUBLIC_BASE = "http://localhost:8081/LibriaService/api/files";

            ((Admin) admin).addBookToLibrary(library, new Book(
                    "9780132350884", "Clean Code", "Robert C. Martin", 2008,
                    "Programming", true,
                    PUBLIC_BASE + "/cover/cover-exemple.png",
                    PUBLIC_BASE + "/pdf/pdf-exemple.pdf"
            ));
            ((Admin) admin).addBookToLibrary(library, new Book(
                    "9780134685991", "Effective Java", "Joshua Bloch", 2018,
                    "Programming", true,
                    PUBLIC_BASE + "/cover/cover-exemple.png",
                    PUBLIC_BASE + "/pdf/pdf-exemple.pdf"
            ));
            ((Admin) admin).addBookToLibrary(library, new Book(
                    "9780201616224", "The Pragmatic Programmer", "Andrew Hunt", 1999,
                    "Programming", true,
                    PUBLIC_BASE + "/cover/cover-exemple.png",
                    PUBLIC_BASE + "/pdf/pdf-exemple.pdf"
            ));
            ((Admin) admin).addBookToLibrary(library, new Book(
                    "9781492078005", "Designing Data-Intensive Applications", "Martin Kleppmann", 2017,
                    "Architecture", true,
                    PUBLIC_BASE + "/cover/cover-exemple.png",
                    PUBLIC_BASE + "/pdf/pdf-exemple.pdf"
            ));
            ((Admin) admin).addBookToLibrary(library, new Book(
                    "9780134494166", "Refactoring", "Martin Fowler", 2018,
                    "Software Engineering", true,
                    PUBLIC_BASE + "/cover/cover-exemple.png",
                    PUBLIC_BASE + "/pdf/pdf-exemple.pdf"
            ));
            ((Admin) admin).addBookToLibrary(library, new Book(
                    "9782070464090", "L'Étranger", "Albert Camus", 1942,
                    "Roman", true,
                    PUBLIC_BASE + "/cover/cover-exemple.png",
                    PUBLIC_BASE + "/pdf/pdf-exemple.pdf"
            ));
            ((Admin) admin).addBookToLibrary(library, new Book(
                    "9782253004226", "Le Petit Prince", "Antoine de Saint-Exupéry", 1943,
                    "Conte", true,
                    PUBLIC_BASE + "/cover/cover-exemple.png",
                    PUBLIC_BASE + "/pdf/pdf-exemple.pdf"
            ));
            ((Admin) admin).addBookToLibrary(library, new Book(
                    "9782070368220", "Les Misérables", "Victor Hugo", 1862,
                    "Roman", true,
                    PUBLIC_BASE + "/cover/cover-exemple.png",
                    PUBLIC_BASE + "/pdf/pdf-exemple.pdf"
            ));
            ((Admin) admin).addBookToLibrary(library, new Book(
                    "9780553283686", "Dune", "Frank Herbert", 1965,
                    "Science Fiction", true,
                    PUBLIC_BASE + "/cover/cover-exemple.png",
                    PUBLIC_BASE + "/pdf/pdf-exemple.pdf"
            ));
            ((Admin) admin).addBookToLibrary(library, new Book(
                    "9780345339706", "The Hobbit", "J.R.R. Tolkien", 1937,
                    "Fantasy", true,
                    PUBLIC_BASE + "/cover/cover-exemple.png",
                    PUBLIC_BASE + "/pdf/pdf-exemple.pdf"
            ));
            ((Admin) admin).addBookToLibrary(library, new Book(
                    "9780553103540", "A Game of Thrones", "George R.R. Martin", 1996,
                    "Fantasy", true,
                    PUBLIC_BASE + "/cover/cover-exemple.png",
                    PUBLIC_BASE + "/pdf/pdf-exemple.pdf"
            ));
            ((Admin) admin).addBookToLibrary(library, new Book(
                    "9780451524935", "1984", "George Orwell", 1949,
                    "Dystopie", true,
                    PUBLIC_BASE + "/cover/cover-exemple.png",
                    PUBLIC_BASE + "/pdf/pdf-exemple.pdf"
            ));
            ((Admin) admin).addBookToLibrary(library, new Book(
                    "9780060850524", "Fahrenheit 451", "Ray Bradbury", 1953,
                    "Dystopie", true,
                    PUBLIC_BASE + "/cover/cover-exemple.png",
                    PUBLIC_BASE + "/pdf/pdf-exemple.pdf"
            ));

            Member zakaria = new Member("MB01", "Zakaria Charouite", "zakaria@libria.com", "zack123");
            Member ismael  = new Member("MB02", "Ismael Benali",    "ismael@libria.com",  "ism123");
            Member aurelie = new Member("MB03", "Aurélie Dupont",   "aurelie@libria.com", "aury123");
            Member karen   = new Member("MB04", "Karen Lemoine",    "karen@libria.com",   "karen321");
            Member julien  = new Member("MB05", "Julien Mercier",   "julien@libria.com",  "jmerc");

            library.registerUser(zakaria);
            library.registerUser(ismael);
            library.registerUser(aurelie);
            library.registerUser(karen);
            library.registerUser(julien);
            library.registerUser(admin);

            System.out.println("LibriaService - ApplicationState initialisé !");
            System.out.println("   → " + library.listBooks().size() + " livres");
            System.out.println("   → " + library.listUsers().size() + " utilisateurs");

        } catch (UserAlreadyExistException | BookAlreadyExistException | AccessDeniedException e) {
            System.err.println("Erreur lors du seed : " + e.getMessage());
        }
    }
}