// LibriaService/src/main/java/com/libria/rest/LibraryResource.java
package com.libria.rest;

import com.libria.domain.Book;
import com.libria.domain.Library;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/library")
@Produces(MediaType.APPLICATION_JSON)
public class LibraryResource {
    //C'est le singleton, comme ca ya tjr une seul instance de library
    private static final Library LIB = SharedLibrary.INSTANCE;

    @GET
    @Path("/books")
    public List<Book> listBooks() { return LIB.listBooks(); }

    @GET
    @Path("/books/{isbn}")
    public Response getOne(@PathParam("isbn") String isbn) {
        var b = LIB.getBook(isbn);
        return b == null ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok(b).build();
    }

    @GET
    @Path("/books/search")
    public List<Book> searchByTitle(@QueryParam("title") String title) {
        if (title == null || title.isBlank()) return List.of();
        return LIB.searchByTitle(title);
    }

    @GET
    @Path("/books/genre/{genre}")
    public List<Book> searchByGenre(@PathParam("genre") String genre) {
        return LIB.searchByGenre(genre);
    }

    @GET
    @Path("/books/author/{author}")
    public List<Book> searchByAuthor(@PathParam("author") String author) {
        return LIB.searchByAuthor(author);
    }
}