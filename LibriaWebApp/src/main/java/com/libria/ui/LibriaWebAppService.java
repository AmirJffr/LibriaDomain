package com.libria.ui;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.primefaces.model.file.UploadedFile;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class LibriaWebAppService {

    private static final String BASE = System.getenv().getOrDefault(
            "LIBRIA_SERVICE_URL",
            "http://payara-libria-service:8080/LibriaService/api"
    );

    private Client client;
    private WebTarget auth;
    private WebTarget library;
    private WebTarget users;
    private WebTarget files;

    @PostConstruct
    void init() {

        client = ClientBuilder.newBuilder()
                .register(MultiPartFeature.class)
                .build();

        auth    = client.target(BASE).path("auth");
        library = client.target(BASE).path("library");
        users   = client.target(BASE).path("users");
        files   = client.target(BASE).path("files");

        System.out.println("[WebApp] Using service base URL: " + BASE);
    }

    @PreDestroy
    void close() {
        if (client != null) client.close();
    }


    // ===========================
    // UPLOAD COVER
    // ===========================
    public String uploadCover(UploadedFile file) throws Exception {
        if (file == null || file.getFileName() == null) return null;

        WebTarget target = files.path("cover");

        String cleanName = System.currentTimeMillis() + "-" +
                file.getFileName().replaceAll("\\s+", "_");

        try (FormDataMultiPart multi = new FormDataMultiPart()) {

            BodyPart part = new BodyPart(
                    file.getInputStream(),
                    MediaType.APPLICATION_OCTET_STREAM_TYPE
            );

            part.setContentDisposition(
                    FormDataContentDisposition.name("file")
                            .fileName(cleanName)
                            .build()
            );

            multi.bodyPart(part);

            Response resp = target.request().post(Entity.entity(multi, multi.getMediaType()));

            if (resp.getStatus() != 201) {
                throw new RuntimeException("Upload cover failed: " + resp.getStatus());
            }

            Map<String,Object> json = resp.readEntity(Map.class);
            return (String) json.get("url");
        }
    }

    // ===========================
    // UPLOAD PDF
    // ===========================
    public String uploadPdf(UploadedFile file) throws Exception {
        if (file == null || file.getFileName() == null) return null;

        WebTarget target = files.path("pdf");

        String cleanName = System.currentTimeMillis() + "-" +
                file.getFileName().replaceAll("\\s+", "_");

        try (FormDataMultiPart multi = new FormDataMultiPart()) {

            BodyPart part = new BodyPart(
                    file.getInputStream(),
                    MediaType.APPLICATION_OCTET_STREAM_TYPE
            );

            part.setContentDisposition(
                    FormDataContentDisposition.name("file")
                            .fileName(cleanName)
                            .build()
            );

            multi.bodyPart(part);

            Response resp = target.request().post(Entity.entity(multi, multi.getMediaType()));

            if (resp.getStatus() != 201) {
                throw new RuntimeException("Upload PDF failed: " + resp.getStatus());
            }

            Map<String,Object> json = resp.readEntity(Map.class);
            return (String) json.get("url");
        }
    }

    // ===========================
    // ADMIN ADD BOOK
    // ===========================
    public Response addBook(String userId, Map<String,Object> payload) {

        WebTarget admin = client
                .target(BASE)
                .path("admin")
                .path("books")
                .path(userId);

        return admin.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(payload, MediaType.APPLICATION_JSON));
    }

    public Response deleteBook(String adminId, String isbn) {
        try {
            WebTarget adminTarget = client
                    .target(BASE)
                    .path("admin")
                    .path("books")
                    .path(adminId)   // /admin/books/{userId}
                    .path(isbn);     // /admin/books/{userId}/{isbn}

            return adminTarget
                    .request()
                    .delete();

        } catch (Exception e) {
            e.printStackTrace();

            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("Service LibriaService injoignable : " + e.getMessage())
                    .build();
        }
    }

    // ===========================
    // REMAINING METHODS (UNCHANGED)
    // ===========================

    public Map<String, Object> loginByEmail(String email, String password) {
        Response resp = users.path("login")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(Map.of("email", email, "password", password)));

        return resp.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL
                ? resp.readEntity(Map.class)
                : Map.of();
    }

    public void logout() {
        try {
            users.path("logout").request().post(null);
        } catch (Exception ignored) {}
    }

    public List<Map<String,Object>> listBooks() {
        return library.path("books")
                .request(MediaType.APPLICATION_JSON)
                .get(List.class);
    }
    public List<Map<String,Object>> listDownloads(String userId) {
        return users.path(userId).path("downloads")
                .request(MediaType.APPLICATION_JSON)
                .get(List.class);
    }
    public List<Map<String,Object>> searchBooksByTitle(String title) {
        return library.path("books").path("search")
                .queryParam("title", title)
                .request(MediaType.APPLICATION_JSON)
                .get(List.class);
    }
    public Response downloadBook(String userId, String isbn) {
        return users.path(userId).path("downloads").path(isbn)
                .request()
                .post(null);
    }

    public Response removeDownload(String userId, String isbn) {
        return users.path(userId).path("downloads").path(isbn)
                .request()
                .delete();
    }
    public Map<String,Object> getBook(String isbn) {
        Response r = library.path("books").path(isbn)
                .request(MediaType.APPLICATION_JSON)
                .get();

        return r.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL
                ? r.readEntity(Map.class)
                : Map.of();
    }

    public boolean registerUser(Map<String, Object> newUserPayload) {
        Response resp = client
                .target(BASE)
                .path("members/register")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(newUserPayload));

        return resp.getStatus() == 200 || resp.getStatus() == 201;
    }

    public Map<String, Object> getBookRating(String isbn) {
        // cible: /library/books/reviews/{isbn}
        WebTarget target = library
                .path("books")
                .path("reviews")
                .path(isbn);

        Response resp = target
                .request(MediaType.APPLICATION_JSON)
                .get();

        int status = resp.getStatus();
        System.out.println("[WebApp] Rating HTTP status = " + status +
                " uri " + target.getUri());

        if (resp.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            System.out.println("[WebApp] Rating -> pas succès, retour map vide");
            return Map.of();
        }

        // 🔥 lire UNE SEULE FOIS en Map
        Map<String,Object> map = resp.readEntity(Map.class);
        System.out.println("[WebApp] Rating MAP = " + map);

        return map != null ? map : Map.of();
    }


    /* ========= Profil utilisateur ========= */

    public Map<String,Object> getUserProfile(String userId) {
        Response resp = users
                .path(userId)             // -> /users/{userId}
                .request(MediaType.APPLICATION_JSON)
                .get();

        if (resp.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            System.out.println("[WebApp] getUserProfile HTTP " + resp.getStatus());
            return Map.of();
        }

        return resp.readEntity(Map.class);
    }

    // ===== User profile / settings =====

    public Response updateUserProfile(String userId, String name, String email, String role) {
        try {
            Map<String, Object> body = Map.of(
                    "name",  name,
                    "email", email,
                    "role",  role
            );

            Response resp = users
                    .path(userId)          // /users/{userId}
                    .path("profile")       // /profile
                    .request(MediaType.APPLICATION_JSON)
                    .put(Entity.json(body));

            System.out.println("[WebApp] updateUserProfile " + userId + " => HTTP " + resp.getStatus());
            return resp;

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("Service LibriaService injoignable : " + e.getMessage())
                    .build();
        }
    }

    public Response updateUserPassword(String userId, String newPassword) {
        try {
            Map<String, Object> body = Map.of(
                    "newPassword", newPassword
            );

            Response resp = users
                    .path(userId)          // /users/{userId}
                    .path("password")      // /password
                    .request(MediaType.APPLICATION_JSON)
                    .put(Entity.json(body));

            System.out.println("[WebApp] updateUserPassword " + userId + " => HTTP " + resp.getStatus());
            return resp;

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("Service LibriaService injoignable : " + e.getMessage())
                    .build();
        }
    }
}