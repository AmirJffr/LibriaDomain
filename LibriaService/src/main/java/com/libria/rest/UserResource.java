package com.libria.rest;

import com.libria.domain.*;
import com.libria.exception.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.Map;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    private ApplicationState state;

    private User requireUser(String userId) {
        try {
            return state.getLibrary().getUser(userId);
        } catch (UserNotFoundException e) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity(e.getMessage())
                            .type(MediaType.TEXT_PLAIN)
                            .build()
            );
        }
    }

    public static class LoginBody {
        public String userId;
        public String password;
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(LoginBody body) {
        try {
            User u = state.authenticate(body.userId, body.password);

            return Response.ok(Map.of(
                    "userId", u.getUserId(),
                    "name", u.getName(),
                    "email", u.getEmail(),
                    "role", u.getRole(),
                    "message", "Login ok"
            )).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();

        } catch (UserNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();

        } catch (LoginException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @GET
    @Path("/{userId}")
    public Map<String, Object> getProfile(@PathParam("userId") String userId) {
        User u = requireUser(userId);
        return Map.of(
                "userId", u.getUserId(),
                "name", u.getName(),
                "email", u.getEmail(),
                "role", u.getRole()
        );
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

    @DELETE
    @Path("/{userId}")
    public Response deleteUser(@PathParam("userId") String userId) {
        try {
            Library lib = state.getLibrary();
            User user = lib.getUser(userId);

            if (user == null)
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Utilisateur introuvable")
                        .type(MediaType.TEXT_PLAIN)
                        .build();

            lib.removeUser(userId);
            return Response.noContent().build();

        } catch (UserNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();

        } catch (AccessDeniedException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erreur interne : " + e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }




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
        try {
            Book b = state.getLibrary().getBook(isbn); // peut lancer BookNotFoundException
            u.downloadBook(b);
            return Response.status(Response.Status.CREATED).entity(b).build();
        } catch (BookNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        } catch (BookAlreadyExistException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erreur interne : " + e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @DELETE
    @Path("/{userId}/downloads/{isbn}")
    public Response removeDownload(@PathParam("userId") String userId,@PathParam("isbn") String isbn) {
        User u = requireUser(userId);
        try {
            Book b = state.getLibrary().getBook(isbn); // peut lever BookNotFoundException
            u.removeBook(b);
            return Response.noContent().build();
        } catch (BookNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erreur interne : " + e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @GET
    @Path("/{userId}/downloads/search")
    public List<Book> searchDownloads(@PathParam("userId") String userId,
                                      @QueryParam("title") String title,
                                      @QueryParam("genre") String genre) {
        User u = requireUser(userId);
        var list = u.listDownloadedBooks();
        if ((title == null || title.isBlank()) && (genre == null || genre.isBlank()))
            return list;

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