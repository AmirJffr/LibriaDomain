package com.libria.rest;

import com.libria.domain.*;
import com.libria.exception.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
public class AdminResource {

    @Inject
    private ApplicationState state;

    private void validateBook(Book book) {
        if (book == null)
            throw new IllegalArgumentException("Livre non fourni.");
        if (book.getIsbn() == null || book.getIsbn().isBlank())
            throw new IllegalArgumentException("ISBN obligatoire.");
        if (book.getTitle() == null || book.getTitle().isBlank())
            throw new IllegalArgumentException("Titre obligatoire.");
        if (book.getAuthor() == null || book.getAuthor().isBlank())
            throw new IllegalArgumentException("Auteur obligatoire.");
        if (book.getPdf() == null || book.getPdf().isBlank())
            throw new pdfBookMissingException("PDF obligatoire.");
    }

    @GET
    @Path("/users")
    public List<User> listUsers() {
        return state.getLibrary().listUsers();
    }

    @POST
    @Path("/books/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addBook(@PathParam("userId") String userId, Book book) {
        try {
            validateBook(book);
            state.addBook(userId, book);
            return Response.status(Response.Status.CREATED).entity(book).build();

        } catch (AccessDeniedException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();

        } catch (BookAlreadyExistException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();

        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/books/{userId}/{isbn}")
    public Response removeBook(@PathParam("userId") String userId, @PathParam("isbn") String isbn) {
        try {
            state.removeBook(userId, isbn);
            return Response.noContent().build();
        } catch (AccessDeniedException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (BookNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/books/{userId}/{isbn}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateBook(@PathParam("userId") String userId,
                               @PathParam("isbn") String isbn,
                               Book updated) {
        try {
            state.updateBook(userId, isbn, updated);
            return Response.noContent().build();
        } catch (AccessDeniedException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (BookNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }
}