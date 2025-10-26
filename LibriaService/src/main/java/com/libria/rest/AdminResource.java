package com.libria.rest;

import com.libria.domain.Admin;
import com.libria.domain.Book;
import com.libria.domain.Library;
import com.libria.exception.AccessDeniedException;
import com.libria.exception.BookAlreadyExistException;
import com.libria.exception.BookNotFoundException;


import com.libria.exception.pdfBookMissingException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Endpoints d'admin (écriture) branchés sur la même instance que LibraryResource.
 * On suppose que tu as une classe SharedLibrary avec un champ public static final Library LIB.
 */
@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
public class AdminResource {

    private static com.libria.domain.Library lib() {
        return SharedLibrary.INSTANCE; // ✅ utilise ton état partagé existant
    }

    // Admin “système” minimal pour déléguer au domaine
    private static Admin sysAdmin() {
        return new Admin("sys", "System", "sys@libria", "secret");
    }

    //il faut valider un livre avant de le créer car en param des api on recoi des book vide
    private void validateBook(Book book) {
        if (book.getIsbn() == null || book.getIsbn().isBlank())
            throw new IllegalArgumentException("ISBN est obligatoire.");
        if (book.getTitle() == null || book.getTitle().isBlank())
            throw new IllegalArgumentException("Le titre est obligatoire.");
        if (book.getAuthor() == null || book.getAuthor().isBlank())
            throw new IllegalArgumentException("L'auteur est obligatoire.");
        if (book.getYear() <= 0)
            throw new IllegalArgumentException("L'année doit être positive.");
        if (book.getPdf() == null || book.getPdf().isBlank())
            throw new pdfBookMissingException("Le fichier PDF est obligatoire.");
    }



    @POST
    @Path("/books")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addBook(Book book) {
        try {
            // 1. on vérifie que le JSON reçu est un vrai livre valide
            System.out.println("📘 JSON reçu → " + book);
            validateBook(book);
            // 2. on essaie de l'ajouter dans la librairie via l'admin système
            sysAdmin().addBookToLibrary(lib(), book);

            // 3. succès → 201
            return Response.status(Response.Status.CREATED)
                    .entity(book)
                    .build();

        } catch (BookAlreadyExistException e) {
            // même ISBN déjà existant
            return Response.status(Response.Status.CONFLICT) // 409
                    .type(MediaType.TEXT_PLAIN)
                    .entity(e.getMessage())
                    .build();

        } catch (pdfBookMissingException e) {
            // PDF manquant = spécifique
            return Response.status(Response.Status.BAD_REQUEST) // 400
                    .type(MediaType.TEXT_PLAIN)
                    .entity(e.getMessage())
                    .build();

        } catch (IllegalArgumentException e) {
            // champs manquants / invalides
            return Response.status(Response.Status.BAD_REQUEST) // 400
                    .type(MediaType.TEXT_PLAIN)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("/books/{isbn}")
    public Response removeBook(@PathParam("isbn") String isbn) {
        try {
            sysAdmin().removeBookFromLibrary(lib(), isbn);
            return Response.noContent().build();
        } catch (BookNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.TEXT_PLAIN)
                    .entity(e.getMessage())
                    .build();
        } catch (AccessDeniedException | IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.TEXT_PLAIN)
                    .entity(e.getMessage())
                    .build();
        }
    }
    @PUT
    @Path("/books/{isbn}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateBook(@PathParam("isbn") String isbn, Book updated) {
        try {
            sysAdmin().updateBookInLibrary(lib(), isbn, updated);
            return Response.noContent().build();
        } catch (BookNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.TEXT_PLAIN)
                    .entity(e.getMessage())
                    .build();
        } catch (AccessDeniedException | IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.TEXT_PLAIN)
                    .entity(e.getMessage())
                    .build();
        }
    }
}