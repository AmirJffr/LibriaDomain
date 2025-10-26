package com.libria.rest;

import com.libria.domain.*;
import com.libria.exception.*;

final class SharedLibrary {
    static final Library INSTANCE = new Library();

    static {
        Library lib = INSTANCE;
        Admin admin = new Admin("AD01", "Super Admin", "admin@libria.com", "libria123");

        try {

            String PUBLIC_BASE = "http://localhost:8081/LibriaService-1.0-SNAPSHOT/api/files";

            admin.addBookToLibrary(lib, new Book(
                    "9780132350884",
                    "Clean Code",
                    "Robert C. Martin",
                    2008,
                    "Programming",
                    true,
                    PUBLIC_BASE + "/cover/cover-exemple.png",
                    PUBLIC_BASE + "/pdf/pdf-exemple.pdf"
            ));

            admin.addBookToLibrary(lib, new Book(
                    "9780134685991",
                    "Effective Java",
                    "Joshua Bloch",
                    2018,
                    "Programming",
                    true,
                    PUBLIC_BASE + "/cover/cover-exemple.png",
                    PUBLIC_BASE + "/pdf/pdf-exemple.pdf"
            ));

            admin.addBookToLibrary(lib, new Book(
                    "9780201616224",
                    "The Pragmatic Programmer",
                    "Andrew Hunt",
                    1999,
                    "Programming",
                    true,
                    PUBLIC_BASE + "/cover/cover-exemple.png",
                    PUBLIC_BASE + "/pdf/pdf-exemple.pdf"
            ));

            admin.addBookToLibrary(lib, new Book(
                    "9781492078005",
                    "Designing Data-Intensive Applications",
                    "Martin Kleppmann",
                    2017,
                    "Architecture",
                    true,
                    PUBLIC_BASE + "/cover/cover-exemple.png",
                    PUBLIC_BASE + "/pdf/pdf-exemple.pdf"
            ));

            admin.addBookToLibrary(lib, new Book(
                    "9780134494166",
                    "Refactoring",
                    "Martin Fowler",
                    2018,
                    "Software Engineering",
                    true,
                    PUBLIC_BASE + "/cover/cover-exemple.png",
                    PUBLIC_BASE + "/pdf/pdf-exemple.pdf"
            ));


            admin.addBookToLibrary(lib, new Book(
                    "9782070464090",
                    "L'Étranger",
                    "Albert Camus",
                    1942,
                    "Roman",
                    true,
                    PUBLIC_BASE + "/cover/cover-exemple.png",
                    PUBLIC_BASE + "/pdf/pdf-exemple.pdf"
            ));

            admin.addBookToLibrary(lib, new Book(
                    "9782253004226",
                    "Le Petit Prince",
                    "Antoine de Saint-Exupéry",
                    1943,
                    "Conte",
                    true,
                    PUBLIC_BASE + "/cover/cover-exemple.png",
                    PUBLIC_BASE + "/pdf/pdf-exemple.pdf"
            ));

            admin.addBookToLibrary(lib, new Book(
                    "9782070368220",
                    "Les Misérables",
                    "Victor Hugo",
                    1862,
                    "Roman",
                    true,
                    PUBLIC_BASE + "/cover/cover-exemple.png",
                    PUBLIC_BASE + "/pdf/pdf-exemple.pdf"
            ));

            admin.addBookToLibrary(lib, new Book(
                    "9780553283686",
                    "Dune",
                    "Frank Herbert",
                    1965,
                    "Science Fiction",
                    true,
                    PUBLIC_BASE + "/cover/cover-exemple.png",
                    PUBLIC_BASE + "/pdf/pdf-exemple.pdf"
            ));

            admin.addBookToLibrary(lib, new Book(
                    "9780345339706",
                    "The Hobbit",
                    "J.R.R. Tolkien",
                    1937,
                    "Fantasy",
                    true,
                    PUBLIC_BASE + "/cover/cover-exemple.png",
                    PUBLIC_BASE + "/pdf/pdf-exemple.pdf"
            ));

            admin.addBookToLibrary(lib, new Book(
                    "9780553103540",
                    "A Game of Thrones",
                    "George R.R. Martin",
                    1996,
                    "Fantasy",
                    true,
                    PUBLIC_BASE + "/cover/cover-exemple.png",
                    PUBLIC_BASE + "/pdf/pdf-exemple.pdf"
            ));

            admin.addBookToLibrary(lib, new Book(
                    "9780451524935",
                    "1984",
                    "George Orwell",
                    1949,
                    "Dystopie",
                    true,
                    PUBLIC_BASE + "/cover/cover-exemple.png",
                    PUBLIC_BASE + "/pdf/pdf-exemple.pdf"
            ));

            admin.addBookToLibrary(lib, new Book(
                    "9780060850524",
                    "Fahrenheit 451",
                    "Ray Bradbury",
                    1953,
                    "Dystopie",
                    true,
                    PUBLIC_BASE + "/cover/cover-exemple.png",
                    PUBLIC_BASE + "/pdf/pdf-exemple.pdf"
            ));

            Member zakaria = new Member("MB01", "Zakaria Charouite", "zakaria@libria.com", "zack123");
            Member ismael = new Member("MB02", "Ismael Benali", "ismael@libria.com", "ism123");
            Member aurelie = new Member("MB03", "Aurélie Dupont", "aurelie@libria.com", "aury123");
            Member karen = new Member("MB04", "Karen Lemoine", "karen@libria.com", "karen321");
            Member julien = new Member("MB05", "Julien Mercier", "julien@libria.com", "jmerc");

            lib.registerUser(zakaria);
            lib.registerUser(ismael);
            lib.registerUser(aurelie);
            lib.registerUser(karen);
            lib.registerUser(julien);
            lib.registerUser(admin);

            System.out.println("✅ LibriaService - SharedLibrary statique chargée !");
            System.out.println("   → " + lib.listBooks().size() + " livres");
            System.out.println("   → " + lib.listUsers().size() + " utilisateurs");

        } catch (UserAlreadyExistException | BookAlreadyExistException | AccessDeniedException e) {
            System.err.println("⚠️ Erreur lors du seed : " + e.getMessage());
        }
    }

    private SharedLibrary() {}
}