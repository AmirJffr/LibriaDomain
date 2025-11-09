package com.libria.ui;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class LibriaWebAppService {

    // URL interne Docker -> le webapp parle au service par son hostname
    private static final String BASE = System.getenv().getOrDefault(
            "LIBRIA_SERVICE_URL",
            "http://payara-libria-service:8080/LibriaService-1.0-SNAPSHOT/api"
    );

    private Client client;
    private WebTarget auth;
    private WebTarget library;
    private WebTarget users;
    private WebTarget files;

    @PostConstruct
    void init() {
        client  = ClientBuilder.newClient();
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

    /* ===== Auth ===== */
    public Map<String, Object> login(String userId, String password) {
        Response resp = auth.path("login")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(Map.of("userId", userId, "password", password)));

        return resp.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL
                ? resp.readEntity(Map.class)
                : Map.of();
    }

    public void logout() {
        try {
            auth.path("logout").request().post(null);
        } catch (Exception ignored) {}
    }

    /* ===== Library (catalogue) ===== */
    public List<Map<String,Object>> listBooks() {
        return library.path("books")
                .request(MediaType.APPLICATION_JSON)
                .get(List.class);
    }

    public List<Map<String,Object>> searchBooksByTitle(String title) {
        return library.path("books").path("search")
                .queryParam("title", title)
                .request(MediaType.APPLICATION_JSON)
                .get(List.class);
    }

    public Map<String,Object> getBook(String isbn) {
        Response r = library.path("books").path(isbn)
                .request(MediaType.APPLICATION_JSON)
                .get();

        return r.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL
                ? r.readEntity(Map.class)
                : Map.of();
    }

    /* ===== Downloads utilisateur ===== */
    public List<Map<String,Object>> listDownloads(String userId) {
        return users.path(userId).path("downloads")
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

    /* ===== Files (covers / pdf) ===== */

    // Si ton Book stocke juste "cover-exemple.png", tu peux produire l'URL utilisable dans <img src="...">
    public String buildCoverUrl(String coverFileName) {
        if (coverFileName == null || coverFileName.isBlank()) return null;
        // retourne une URL ABSOLUE que le navigateur du user peut appeler DIRECTEMENT
        // -> donc on utilise le port EXPOSE sur ta machine : 8081
        return "http://localhost:8081/LibriaService-1.0-SNAPSHOT/api/files/cover/" + coverFileName;
    }

    // Idem pour le PDF
    public String buildPdfUrl(String pdfFileName) {
        if (pdfFileName == null || pdfFileName.isBlank()) return null;
        return "http://localhost:8081/LibriaService-1.0-SNAPSHOT/api/files/pdf/" + pdfFileName;
    }

    public Response addBook(Map<String, Object> payload) {
        // Appel du service REST côté LibriaService
        try {
            // on cible l’endpoint /admin/books de LibriaService
            WebTarget admin = client.target(BASE).path("admin").path("books");

            // on envoie la requête POST avec le JSON du livre
            Response resp = admin
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(payload, MediaType.APPLICATION_JSON));

            return resp; // on renvoie la réponse brute au bean JSF (addBookBean)
        } catch (Exception e) {
            e.printStackTrace();
            // Si le service est injoignable ou erreur réseau
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("Service LibriaService injoignable : " + e.getMessage())
                    .build();
        }
    }

    public boolean registerUser(Map<String, Object> newUserPayload) {
        try {
            Response resp = client
                    .target(BASE)
                    .path("members/register") // ✅ correspond à ton MemberResource
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(newUserPayload));

            int status = resp.getStatus();
            System.out.println("[WebApp] registerUser => HTTP " + status);

            // 201 Created ou 200 OK = succès
            return status == 201 || status == 200;

        } catch (Exception e) {
            System.out.println("[WebApp] registerUser ERROR: " + e.getMessage());
            return false;
        }
    }

}