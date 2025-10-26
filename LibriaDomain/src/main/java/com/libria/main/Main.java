package com.libria.main;

import com.libria.domain.Admin;
import com.libria.domain.Book;
import com.libria.domain.Library;
import com.libria.domain.Member;
import com.libria.exception.*;
import javax.security.auth.login.LoginException;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        Library library = new Library();
        System.out.println("== Démarrage Libria ==");

        // ==== Création des utilisateurs ====
        Admin admin = new Admin("U001", "Zakaria", "zakaria@libria.com", "1234");
        Member member = new Member("U002", "Amir", "amir@libria.com", "abcd");

        try {
            library.registerUser(admin);
            library.registerUser(member);
        } catch (UserAlreadyExistException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\nUtilisateurs enregistrés: " + library.listUsers().size());

        // ==== Création des livres ====
        // NOTE: nouveau constructeur Book(
        //   isbn, title, author, year, genre, available, coverPath, pdfPath
        // )
        Book b1 = new Book(
                "978-0-7475-3269-9",
                "Harry Potter and the Philosopher's Stone",
                "J.K. Rowling",
                1997,
                "Fantasy",
                true,
                "/covers/hp1.jpg",
                "/pdf/hp1.pdf"
        );
        Book b2 = new Book(
                "978-0-261-10236-9",
                "The Lord of the Rings",
                "J.R.R. Tolkien",
                1954,
                "Fantasy",
                true,
                "/covers/lotr.jpg",
                "/pdf/lotr.pdf"
        );
        Book b3 = new Book(
                "978-0-06-112008-4",
                "To Kill a Mockingbird",
                "Harper Lee",
                1960,
                "Classic",
                true,
                "/covers/mockingbird.jpg",
                "/pdf/mockingbird.pdf"
        );
        Book b4 = new Book(
                "978-0-201-03801-9",
                "Design Patterns",
                "Erich Gamma",
                1994,
                "Software",
                true,
                "/covers/designpatterns.jpg",
                "/pdf/designpatterns.pdf"
        );

        // ==== Ajout des livres par l'admin ====
        try {
            admin.addBookToLibrary(library, b1);
            admin.addBookToLibrary(library, b2);
            admin.addBookToLibrary(library, b3);
            admin.addBookToLibrary(library, b4);
        } catch (AccessDeniedException | BookAlreadyExistException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("Livres dans le catalogue: " + library.listBooks().size());
        for (Book b : library.listBooks()) {
            System.out.println(" - " + b.getIsbn()
                    + " | " + b.getTitle()
                    + " | " + b.getGenre());
        }

        // ==== Authentification ====
        try {
            System.out.println("\n[Auth] Admin avec '1234' => " + admin.login("1234"));
        } catch (LoginException e) {
            System.out.println(e.getMessage());
        }

        try {
            System.out.println("[Auth] Membre avec 'wrong' => " + member.login("wrong"));
        } catch (LoginException e) {
            System.out.println(e.getMessage());
        }

        // ==== Recherche ====
        System.out.println("\nRecherche titre contient 'Harry':");
        List<Book> searchTitle = library.searchByTitle("Harry");
        for (Book b : searchTitle) {
            System.out.println(" - " + b.getTitle());
        }

        System.out.println("\nRecherche genre = 'Fantasy':");
        List<Book> searchGenre = library.searchByGenre("Fantasy");
        for (Book b : searchGenre) {
            System.out.println(" - " + b.getTitle());
        }

        // ==== Téléchargement par un membre ====
        System.out.println("\n== Téléchargement ==");
        Book hp = library.getBook("978-0-7475-3269-9");
        try {
            member.downloadBook(hp);
            System.out.println("Téléchargé: " + hp.getTitle());
        } catch (BookAlreadyExistException e) {
            System.out.println(e.getMessage());
        }

        // retélécharger le même livre → doit lever BookAlreadyExistException
        try {
            member.downloadBook(hp);
        } catch (BookAlreadyExistException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\nLivres téléchargés par " + member.getName() + " :");
        for (Book b : member.listDownloadedBooks()) {
            System.out.println(" - " + b.getTitle());
        }

        // ==== Retirer un livre téléchargé ====
        System.out.println("\n== Suppression de la collection ==");
        try {
            member.removeBook(hp);
            System.out.println("Retiré de la collection: " + hp.getTitle());
        } catch (BookNotFoundException e) {
            System.out.println(e.getMessage());
        }

        // ==== Supprimer du catalogue (admin) ====
        System.out.println("\n== Suppression du catalogue ==");
        try {
            admin.removeBookFromLibrary(library, b4.getIsbn());
            System.out.println("Supprimé du catalogue: " + b4.getTitle());
        } catch (AccessDeniedException | BookNotFoundException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("Livres dans le catalogue (après suppression): " + library.listBooks().size());

        System.out.println("\nÉtat final Library: " + library);
        System.out.println("== Fin ==");
    }
}