package com.libria.rest;

import com.libria.domain.Book;
import com.libria.domain.Library;
import com.libria.domain.User;
import com.libria.exception.BookAlreadyExistException;
import com.libria.exception.BookNotFoundException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.Map;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    private static Library lib() { return SharedLibrary.INSTANCE; }

    private static User requireUser(String userId) {
        User u = lib().getUser(userId);
        if (u == null) throw new WebApplicationException("Utilisateur introuvable", Response.Status.NOT_FOUND);
        return u;
    }

    // ===== Auth =====
    public static class LoginBody { public String userId; public String password; }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(LoginBody body) {
        if (body == null || body.userId == null || body.userId.isBlank()
                || body.password == null || body.password.isBlank()) {
            throw new WebApplicationException("Identifiants requis", Response.Status.BAD_REQUEST);
        }
        User u = lib().getUser(body.userId);
        if (u == null) throw new WebApplicationException("Utilisateur introuvable", Response.Status.NOT_FOUND);
        try {
            u.login(body.password); // lève LoginException si mauvais mdp
            return Response.ok(Map.of(
                    "userId", u.getUserId(),
                    "name",   u.getName(),
                    "email",  u.getEmail(),
                    "role",   u.getRole(),
                    "message","Login ok"
            )).build();
        } catch (LoginException e) {
            throw new WebApplicationException(e.getMessage(), Response.Status.UNAUTHORIZED);
        }
    }

    // ===== Profil =====
    @GET
    @Path("/{userId}")
    public Map<String, Object> getProfile(@PathParam("userId") String userId) {
        User u = requireUser(userId);
        return Map.of("userId", u.getUserId(), "name", u.getName(), "email", u.getEmail(), "role", u.getRole());
    }

    public static class ChangePasswordBody { public String newPassword; }

    @PUT
    @Path("/{userId}/password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changePassword(@PathParam("userId") String userId, ChangePasswordBody body) {
        if (body == null || body.newPassword == null || body.newPassword.isBlank())
            throw new WebApplicationException("Nouveau mot de passe requis", Response.Status.BAD_REQUEST);
        User u = requireUser(userId);
        u.changePassword(body.newPassword);
        return Response.noContent().build();
    }

    // ===== Téléchargements =====
    @GET
    @Path("/{userId}/downloads")
    public List<Book> listDownloads(@PathParam("userId") String userId) {
        return requireUser(userId).listDownloadedBooks();
    }

    @POST
    @Path("/{userId}/downloads/{isbn}")
    public Response download(@PathParam("userId") String userId,
                             @PathParam("isbn") String isbn) {
        User u = requireUser(userId);
        Book b = lib().getBook(isbn);
        if (b == null) throw new WebApplicationException("Livre introuvable", Response.Status.NOT_FOUND);
        try {
            u.downloadBook(b);
            return Response.status(Response.Status.CREATED).entity(b).build();
        } catch (BookAlreadyExistException e) {
            throw new WebApplicationException(e.getMessage(), Response.Status.CONFLICT);
        }
    }

    @DELETE
    @Path("/{userId}/downloads/{isbn}")
    public Response removeDownload(@PathParam("userId") String userId,
                                   @PathParam("isbn") String isbn) {
        User u = requireUser(userId);
        Book b = lib().getBook(isbn);
        if (b == null) throw new WebApplicationException("Livre introuvable", Response.Status.NOT_FOUND);
        try {
            u.removeBook(b);
            return Response.noContent().build();
        } catch (BookNotFoundException e) {
            throw new WebApplicationException(e.getMessage(), Response.Status.NOT_FOUND);
        }
    }

    // Rechercher DANS les téléchargements de l'utilisateur
    @GET
    @Path("/{userId}/downloads/search")
    public List<Book> searchDownloads(@PathParam("userId") String userId,
                                      @QueryParam("title") String title,
                                      @QueryParam("genre") String genre) {
        User u = requireUser(userId);
        var list = u.listDownloadedBooks();
        if ((title == null || title.isBlank()) && (genre == null || genre.isBlank())) return list;
        return list.stream()
                .filter(b ->
                        (title == null || title.isBlank() || b.getTitle().toLowerCase().contains(title.toLowerCase())) &&
                                (genre == null || genre.isBlank() || genre.equalsIgnoreCase(b.getGenre()))
                )
                .toList();
    }
    public static class UpdateProfileBody {
        public String name;
        public String email;
    }
    @PUT
    @Path("/{userId}/profile")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateProfile(@PathParam("userId") String userId, UpdateProfileBody body) {
        if (body == null)
            throw new WebApplicationException("Données de profil requises", Response.Status.BAD_REQUEST);

        User u = requireUser(userId);

        if (body.name != null && !body.name.isBlank()) {
            u.setName(body.name);
        }
        if (body.email != null && !body.email.isBlank()) {
            u.setEmail(body.email);
        }

        return Response.ok(Map.of(
                "message", "Profil mis à jour",
                "userId", u.getUserId(),
                "name", u.getName(),
                "email", u.getEmail()
        )).build();
    }


}