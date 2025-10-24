package com.libria.rest;

import com.libria.domain.Admin;
import com.libria.domain.Book;
import com.libria.domain.Library;
import com.libria.exception.AccessDeniedException;
import com.libria.exception.BookAlreadyExistException;
import com.libria.exception.BookNotFoundException;

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

    @POST
    @Path("/books")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addBook(Book book) {
        try {
            sysAdmin().addBookToLibrary(lib(), book);
            return Response.status(Response.Status.CREATED).entity(book).build();
        } catch (BookAlreadyExistException e) {
            return Response.status(Response.Status.CONFLICT)
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