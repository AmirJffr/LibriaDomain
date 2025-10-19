package com.libria.model;

import com.libria.exception.*;

import javax.security.auth.login.LoginException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws LoginException, BookAlreadyExistException, BookNotFoundException {

        Library library = new Library();
        System.out.println("== Démarrage Libria ==");


        Book b1 = new Book("978-0-7475-3269-9", "Harry Potter and the Philosopher's Stone", "J.K. Rowling", 1997, "Fantasy", true);
        Book b2 = new Book("978-0-261-10236-9", "The Lord of the Rings", "J.R.R. Tolkien", 1954, "Fantasy", true);
        Book b3 = new Book("978-0-06-112008-4", "To Kill a Mockingbird", "Harper Lee", 1960, "Classic", true);
        Book b4 = new Book("978-0-201-03801-9", "Design Patterns", "Erich Gamma", 1994, "Software", true);


        library.addBook(b1);
        library.addBook(b2);
        library.addBook(b3);
        library.addBook(b4);

        System.out.println("Livres dans le catalogue: " + library.listBooks().size());
        for (Book b : library.listBooks()) {
            System.out.println(" - " + b.getIsbn() + " | " + b.getTitle() + " | " + b.getGenre());
        }


        User u1 = new User("U001", "Zakaria", "zakaria@libria.com", "1234");
        User u2 = new User("U002", "Amir", "amir@libria.com", "abcd");


        library.registerUser(u1);
        library.registerUser(u2);
        System.out.println("\nUtilisateurs enregistrés: " + library.listUsers().size());

        try {
            System.out.println("\n[Auth] Zakaria avec '1234' => " + u1.login("1234"));
        }catch (LoginException e) {
            System.out.println(e.getMessage());
        }


        try {
            System.out.println("[Auth] Amir avec 'wrong' => " + u2.login("wrong"));
        }catch (LoginException e) {
            System.out.println(e.getMessage());
        }

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
        try{
            u1.downloadBook(hp);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        System.out.println("Disponibilité après: " + hp.isAvailable());

        try{
            u1.downloadBook(hp);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }




        System.out.println("\nLivres téléchargés par Zakaria:");
        for (Book b : u1.listDownloadedBooks()) System.out.println(" - " + b.getTitle());


        System.out.println("\n== Suppression de la collection ==");
        u1.removeBook(hp);

        System.out.println("Disponibilité redevenue: " + hp.isAvailable());


        System.out.println("\n== Suppression du catalogue ==");
        library.removeBook(b4.getIsbn());

        System.out.println("Livres dans le catalogue (après suppression): " + library.listBooks().size());


        System.out.println("\nÉtat final Library: " + library);
        System.out.println("== Fin ==");
    }
}