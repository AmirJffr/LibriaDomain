package com.libria.rest;

import com.libria.domain.Book;
import com.libria.domain.Library;
import com.libria.domain.ApplicationState;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Path("/library")
@Produces(MediaType.APPLICATION_JSON)
public class LibraryResource {

    @Inject
    private ApplicationState state;

    private Library lib() {
        return state.getLibrary();
    }

    @GET
    @Path("/books")
    public List<Book> listBooks() { return lib().listBooks(); }

    @GET
    @Path("/books/{isbn}")
    public Response getOne(@PathParam("isbn") String isbn) {
        var b = lib().getBook(isbn);
        return b == null ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok(b).build();
    }

    @GET
    @Path("/books/search")
    public List<Book> searchByTitle(@QueryParam("title") String title) {
        if (title == null || title.isBlank()) return List.of();
        return lib().searchByTitle(title);
    }

    @GET
    @Path("/books/genre/{genre}")
    public List<Book> searchByGenre(@PathParam("genre") String genre) {
        return lib().searchByGenre(genre);
    }

    @GET
    @Path("/books/author/{author}")
    public List<Book> searchByAuthor(@PathParam("author") String author) {
        return lib().searchByAuthor(author);
    }

    @GET
    @Path("/books/reviews/{isbn}")
    public Response getReviews(@PathParam("isbn") String isbn) {
        try {
            var url = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn;
            var client = HttpClient.newHttpClient();
            var req = HttpRequest.newBuilder(URI.create(url))
                    .header("User-Agent", "LibriaService/1.0")
                    .GET()
                    .build();
            var res = client.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() != 200) {
                return Response.status(Response.Status.BAD_GATEWAY)
                        .entity("{\"error\":\"Google Books API error\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }

            var root = new org.json.JSONObject(res.body());
            if (!root.has("items")) {
                return Response.ok("{\"averageRating\":null,\"ratingsCount\":null}")
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }

            var items = root.getJSONArray("items");
            var volumeInfo = items.getJSONObject(0).getJSONObject("volumeInfo");

            Double avg = volumeInfo.has("averageRating") ? volumeInfo.getDouble("averageRating") : null;
            Integer count = volumeInfo.has("ratingsCount") ? volumeInfo.getInt("ratingsCount") : null;

            var result = new org.json.JSONObject();
            result.put("averageRating", avg);
            result.put("ratingsCount", count);

            return Response.ok(result.toString(), MediaType.APPLICATION_JSON).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }
}