package com.libria.rest;

import com.libria.domain.Admin;
import com.libria.domain.User;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Context
    HttpServletRequest req;


    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String ping() { return "auth-ok"; }
    /**
     * Connexion utilisateur
     */
    @POST
    @Path("/login")
    public Response login(Map<String, String> body) {
        String userId = body.getOrDefault("userId", "").trim();
        String password = body.getOrDefault("password", "").trim();

        if (userId.isEmpty() || password.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.TEXT_PLAIN)
                    .entity("userId et password requis")
                    .build();
        }

        // 🔹 Vérifie dans la bibliothèque partagée
        User u = SharedLibrary.INSTANCE.getUser(userId);
        if (u == null || !u.getPassword().equals(password)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .type(MediaType.TEXT_PLAIN)
                    .entity("Identifiants invalides")
                    .build();
        }

        // 🔹 Crée une session
        var session = req.getSession(true);
        session.setAttribute("uuid", u.getUserId());
        session.setAttribute("username", u.getName());
        session.setAttribute("role", (u instanceof Admin) ? "ADMIN" : u.getRole());

        // 🔹 Retourne les infos basiques
        return Response.ok(Map.of(
                "userId", u.getUserId(),
                "name", u.getName(),
                "role", session.getAttribute("role")
        )).build();
    }

    /**
     * Déconnexion utilisateur
     */
    @POST
    @Path("/logout")
    public Response logout() {
        var session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return Response.noContent().build();
    }

    /**
     * Vérifie la session actuelle
     */
    @GET
    @Path("/session")
    public Response getSessionInfo() {
        var session = req.getSession(false);
        if (session == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("status", "not logged in"))
                    .build();
        }

        return Response.ok(Map.of(
                "userId", session.getAttribute("uuid"),
                "username", session.getAttribute("username"),
                "role", session.getAttribute("role")
        )).build();
    }
}