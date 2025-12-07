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

    // --------- utilitaire ---------
    private User requireUser(String userId) {
        try {
            return state.findUserById(userId);   // ✅ BDD
        } catch (UserNotFoundException e) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity(e.getMessage())
                            .type(MediaType.TEXT_PLAIN)
                            .build()
            );
        }
    }

    // --------- login ---------
    public static class LoginBody {
        public String email;
        public String password;
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(LoginBody body) {
        try {
            User u = state.authenticate(body.email, body.password);

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

    // --------- profil ---------

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

    public static class ChangePasswordBody {
        public String newPassword;
    }

    @PUT
    @Path("/{userId}/password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changePassword(@PathParam("userId") String userId,
                                   ChangePasswordBody body) {
        if (body == null || body.newPassword == null || body.newPassword.isBlank()) {
            throw new WebApplicationException("Nouveau mot de passe requis",
                    Response.Status.BAD_REQUEST);
        }

        try {
            state.changePassword(userId, body.newPassword);   // ✅ BDD
            return Response.noContent().build();
        } catch (UserNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @DELETE
    @Path("/{userId}")
    public Response deleteUser(@PathParam("userId") String userId) {
        try {
            state.deleteUser(userId);   // ✅ BDD
            return Response.noContent().build();

        } catch (UserNotFoundException e) {
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

    // --------- téléchargements ---------

    @GET
    @Path("/{userId}/downloads")
    public List<Book> listDownloads(@PathParam("userId") String userId) {
        try {
            return state.listDownloads(userId);   // ✅ BDD
        } catch (UserNotFoundException e) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity(e.getMessage())
                            .type(MediaType.TEXT_PLAIN)
                            .build()
            );
        }
    }

    @POST
    @Path("/{userId}/downloads/{isbn}")
    public Response download(@PathParam("userId") String userId,
                             @PathParam("isbn") String isbn) {
        try {
            state.addDownload(userId, isbn);   // ✅ BDD
            // On renvoie le book pour rester compatible
            Book b = state.findBookByIsbn(isbn);
            return Response.status(Response.Status.CREATED).entity(b).build();

        } catch (UserNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();

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
    public Response removeDownload(@PathParam("userId") String userId,
                                   @PathParam("isbn") String isbn) {
        try {
            state.removeDownload(userId, isbn);   // ✅ BDD
            return Response.noContent().build();

        } catch (UserNotFoundException | BookNotFoundException e) {
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
        try {
            return state.searchDownloads(userId, title, genre);   // ✅ BDD
        } catch (UserNotFoundException e) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity(e.getMessage())
                            .type(MediaType.TEXT_PLAIN)
                            .build()
            );
        }
    }

    // --------- update profile ---------

    public static class UpdateProfileBody {
        public String name;
        public String email;
    }

    @PUT
    @Path("/{userId}/profile")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateProfile(@PathParam("userId") String userId,
                                  UpdateProfileBody body) {
        if (body == null)
            throw new WebApplicationException("Données de profil requises",
                    Response.Status.BAD_REQUEST);

        try {
            User u = state.updateProfile(userId, body.name, body.email);  // ✅ BDD

            return Response.ok(Map.of(
                    "message", "Profil mis à jour",
                    "userId", u.getUserId(),
                    "name", u.getName(),
                    "email", u.getEmail()
            )).build();

        } catch (UserAlreadyExistException e) {
            throw new WebApplicationException(
                    Response.status(Response.Status.CONFLICT)
                            .entity(e.getMessage())
                            .type(MediaType.TEXT_PLAIN)
                            .build()
            );
        } catch (UserNotFoundException e) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity(e.getMessage())
                            .type(MediaType.TEXT_PLAIN)
                            .build()
            );
        }
    }
}