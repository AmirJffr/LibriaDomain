package com.libria.model;

import com.libria.exception.BookAlreadyExistException;
import com.libria.exception.BookNotInDownloadedBooks;

import javax.security.auth.login.LoginException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws BookAlreadyExistException, BookNotInDownloadedBooks, LoginException {
        // --- Créer la bibliothèque ---
        Library library = new Library();
        System.out.println("== Démarrage Libria ==");

        // --- Créer quelques livres ---
        Book b1 = new Book("978-0-7475-3269-9", "Harry Potter and the Philosopher's Stone", "J.K. Rowling", 1997, "Fantasy", true);
        Book b2 = new Book("978-0-261-10236-9", "The Lord of the Rings", "J.R.R. Tolkien", 1954, "Fantasy", true);
        Book b3 = new Book("978-0-06-112008-4", "To Kill a Mockingbird", "Harper Lee", 1960, "Classic", true);
        Book b4 = new Book("978-0-201-03801-9", "Design Patterns", "Erich Gamma", 1994, "Software", true);

        // --- Ajouter les livres au catalogue ---
        library.addBook(b1);
        library.addBook(b2);
        library.addBook(b3);
        library.addBook(b4);

        System.out.println("Livres dans le catalogue: " + library.listBooks().size());
        for (Book b : library.listBooks()) {
            System.out.println(" - " + b.getIsbn() + " | " + b.getTitle() + " | " + b.getGenre());
        }

        // --- Créer des utilisateurs ---
        User u1 = new User("U001", "Zakaria", "zakaria@libria.com", "1234");
        User u2 = new User("U002", "Amir", "amir@libria.com", "abcd");

        // --- Enregistrer les utilisateurs ---
        library.registerUser(u1);
        library.registerUser(u2);
        System.out.println("\nUtilisateurs enregistrés: " + library.listUsers().size());

        // --- Authentification ---
        System.out.println("\n[Auth] Zakaria avec '1234' => " + u1.login("1234"));
        System.out.println("[Auth] Amir avec 'wrong' => " + u2.login("wrong"));

        // --- Recherche par titre ---
        System.out.println("\nRecherche titre contient 'Harry':");
        List<Book> searchTitle = library.searchByTitle("Harry");
        for (Book b : searchTitle) System.out.println(" - " + b.getTitle());

        // --- Recherche par genre ---
        System.out.println("\nRecherche genre = 'Fantasy':");
        List<Book> searchGenre = library.searchByGenre("Fantasy");
        for (Book b : searchGenre) System.out.println(" - " + b.getTitle());

        // --- Télécharger un livre ---
        System.out.println("\n== Téléchargement ==");
        Book hp = library.getBook("978-0-7475-3269-9");
        System.out.println("Disponibilité avant: " + hp.isAvailable());
        boolean dl1 = u1.downloadBook(hp);
        System.out.println("Zakaria télécharge HP => " + dl1);
        System.out.println("Disponibilité après: " + hp.isAvailable());

        // Tentative de retéléchargement du même livre (doit être false car déjà dans sa collection)
        boolean dl2 = u1.downloadBook(hp);
        System.out.println("Zakaria retélécharge HP => " + dl2);

        // --- Liste des livres téléchargés par Zakaria ---
        System.out.println("\nLivres téléchargés par Zakaria:");
        for (Book b : u1.listDownloadedBooks()) System.out.println(" - " + b.getTitle());

        // --- Supprimer un livre de la collection de l'utilisateur ---
        System.out.println("\n== Suppression de la collection ==");
        boolean removed = u1.removeBook(hp);
        System.out.println("Zakaria retire HP de sa collection => " + removed);
        System.out.println("Disponibilité redevenue: " + hp.isAvailable());

        // --- Supprimer un livre du catalogue ---
        System.out.println("\n== Suppression du catalogue ==");
        boolean removedFromCatalog = library.removeBook(b4.getIsbn());
        System.out.println("Suppression 'Design Patterns' => " + removedFromCatalog);
        System.out.println("Livres dans le catalogue (après suppression): " + library.listBooks().size());

        // --- Affichage final ---
        System.out.println("\nÉtat final Library: " + library);
        System.out.println("== Fin ==");
    }
}