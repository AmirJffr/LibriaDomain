package com.libria.rest;

import com.libria.domain.ApplicationState;
import com.libria.domain.Book;
import com.libria.exception.BookNotFoundException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;


//pour chat bot request
import java.net.HttpURLConnection;
import java.net.URL;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;



@Path("/files")
public class FileResource {

    @Inject
    private ApplicationState state;

    private static final java.nio.file.Path BASE_DIR =
            java.nio.file.Paths.get("/opt/payara/appserver/files");

    // Sous-dossiers
    private static final java.nio.file.Path COVER_DIR = BASE_DIR.resolve("cover");
    private static final java.nio.file.Path PDF_DIR   = BASE_DIR.resolve("pdf");

    static {
        try {
            Files.createDirectories(COVER_DIR);
            Files.createDirectories(PDF_DIR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private Response serveCoverByFilename(String filename) {
        if (filename == null || filename.contains("..")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Nom de fichier invalide")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }

        try {
            java.nio.file.Path filePath = COVER_DIR.resolve(filename);
            if (!Files.exists(filePath)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Couverture introuvable")
                        .type(MediaType.TEXT_PLAIN)
                        .build();
            }

            String mime = filename.endsWith(".png") ? "image/png"
                    : (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) ? "image/jpeg"
                    : filename.endsWith(".gif") ? "image/gif"
                    : "application/octet-stream";

            InputStream in = Files.newInputStream(filePath);
            return Response.ok(in, mime).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError()
                    .entity("Erreur lors de la lecture de la couverture")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    private Response servePdfByFilename(String filename) {
        if (filename == null || filename.contains("..")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Nom de fichier invalide")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }

        try {
            java.nio.file.Path filePath = PDF_DIR.resolve(filename);
            if (!Files.exists(filePath)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("PDF introuvable")
                        .type(MediaType.TEXT_PLAIN)
                        .build();
            }

            InputStream in = Files.newInputStream(filePath);

            // 👇 IMPORTANT : plus "attachment", mais "inline"
            return Response.ok(in, "application/pdf")
                    .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError()
                    .entity("Erreur lors de la lecture du PDF")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }



    @GET
    @Path("/cover/{filename}")
    public Response getCover(@PathParam("filename") String filename) {
        return serveCoverByFilename(filename);
    }

    //@GET
    //@Path("/pdf/{filename}")
    //public Response getPdf(@PathParam("filename") String filename) {
    //    return servePdfByFilename(filename);
    //}

    @GET
    @Path("/pdf/{filename}")
    public Response getPdf(@PathParam("filename") String filename) {
        if (filename == null || filename.contains("..")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Nom de fichier invalide")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }

        try {
            java.nio.file.Path filePath = PDF_DIR.resolve(filename);
            if (!Files.exists(filePath)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("PDF introuvable")
                        .type(MediaType.TEXT_PLAIN)
                        .build();
            }

            InputStream in = Files.newInputStream(filePath);

            return Response.ok(in, "application/pdf")
                    // 🔥 clé : inline, pas attachment
                    .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError()
                    .entity("Erreur lors de la lecture du PDF")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }



    @GET
    @Path("/book/{isbn}/cover")
    public Response getBookCover(@PathParam("isbn") String isbn) {
        Book book;
        try {
            book = state.getLibrary().getBook(isbn);
        } catch (BookNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Livre introuvable")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }

        String coverPath = book.getCoverImage();
        if (coverPath == null || coverPath.isBlank()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Aucune couverture définie pour ce livre")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }

        String filename = coverPath.substring(coverPath.lastIndexOf('/') + 1);
        return serveCoverByFilename(filename);
    }

    @GET
    @Path("/book/{isbn}/pdf")
    public Response getBookPdf(@PathParam("isbn") String isbn) {
        Book book;
        try {
            book = state.getLibrary().getBook(isbn);
        } catch (BookNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Livre introuvable")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }

        String pdfPath = book.getPdf();
        if (pdfPath == null || pdfPath.isBlank()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Aucun PDF défini pour ce livre")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }


        String filename = pdfPath.substring(pdfPath.lastIndexOf('/') + 1);
        return servePdfByFilename(filename);
    }



    @POST
    @Path("/cover")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadCover(@FormDataParam("file") InputStream in,
                                @FormDataParam("file") FormDataContentDisposition meta) {
        try {
            String fileName = meta.getFileName(); // ex: "cover.png"
            java.nio.file.Path target = COVER_DIR.resolve(fileName);

            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);

            String url = "http://localhost:8081/LibriaService/api/files/cover/" + fileName;

            Map<String, String> body = Map.of(
                    "fileName", fileName,
                    "url", url
            );

            return Response.status(Response.Status.CREATED)
                    .entity(body)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Unable to save file")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }


    private void writeFormField(java.io.PrintWriter writer,
                                String boundary,
                                String name,
                                String value) {
        String lineEnd = "\r\n";
        String twoHyphens = "--";

        writer.append(twoHyphens).append(boundary).append(lineEnd);
        writer.append("Content-Disposition: form-data; name=\"")
                .append(name).append("\"").append(lineEnd);
        writer.append("Content-Type: text/plain; charset=UTF-8").append(lineEnd);
        writer.append(lineEnd);
        writer.append(value).append(lineEnd);
        writer.flush();
    }



    //pour le chat bot


    private static final String CHATBOT_BASE     = "https://6252532b-33cf-49cc-9acb-c87718fe0974.fly.dev/api/v1";
    private static final String CHATBOT_EMAIL    = "johndoe@test.com";
    private static final String CHATBOT_PASSWORD = "johnTest123";

    private String chatbotLoginJwt() throws Exception {
        URL url = new URL(CHATBOT_BASE + "/auth/login");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        String body = """
        {
          "email": "%s",
          "password": "%s"
        }
        """.formatted(CHATBOT_EMAIL, CHATBOT_PASSWORD);

        try (var os = conn.getOutputStream()) {
            os.write(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        if (status != 200) {
            String err = new String(conn.getErrorStream().readAllBytes(),
                    java.nio.charset.StandardCharsets.UTF_8);
            throw new RuntimeException("Chatbot login failed: " + status + " " + err);
        }

        String json = new String(conn.getInputStream().readAllBytes(),
                java.nio.charset.StandardCharsets.UTF_8);
        System.out.println("[FileResource] Chatbot login body = " + json);

        var reader = jakarta.json.Json.createReader(new java.io.StringReader(json));
        var root   = reader.readObject();
        String jwt = root.getJsonObject("data").getString("jwt");

        return jwt;
    }

    private void sendPdfToChatbot(java.nio.file.Path pdfPath) throws Exception {
        String jwt = chatbotLoginJwt();   // récupère le JWT

        String boundary = "----LibriaBoundary" + System.currentTimeMillis();

        URL url = new URL(CHATBOT_BASE + "/document/ingest/file");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        // 🔥 EXACTEMENT comme Postman : cookie jwt=...
        conn.setRequestProperty("Cookie", "jwt=" + jwt);

        try (var os = conn.getOutputStream();
             var writer = new java.io.PrintWriter(
                     new java.io.OutputStreamWriter(os, java.nio.charset.StandardCharsets.UTF_8),
                     true)) {

            String fileName = pdfPath.getFileName().toString();

            // ---- part fichier ----
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"files\"; filename=\"")
                    .append(fileName).append("\"\r\n");
            writer.append("Content-Type: application/pdf\r\n\r\n");
            writer.flush();

            java.nio.file.Files.copy(pdfPath, os);
            os.flush();
            writer.append("\r\n");

            // ---- champs texte ----
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"companyId\"\r\n\r\n");
            writer.append("68b5d8e600377d4ed6cf\r\n");

            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"chatbotId\"\r\n\r\n");
            writer.append("692c55570008f35a92e2\r\n");

            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"createdByUserId\"\r\n\r\n");
            writer.append("68bb214100293c6062c6\r\n");

            writer.append("--").append(boundary).append("--\r\n");
            writer.flush();
        }

        int status = conn.getResponseCode();
        String respBody = new String(
                (status >= 200 && status < 300 ? conn.getInputStream() : conn.getErrorStream())
                        .readAllBytes(),
                java.nio.charset.StandardCharsets.UTF_8
        );

        System.out.println("[FileResource] Chatbot ingest HTTP status = " + status);
        System.out.println("[FileResource] Chatbot ingest body = " + respBody);

        if (status != 200 && status != 201) {
            throw new RuntimeException("Chatbot ingest failed: " + status + " " + respBody);
        }
    }

    @POST
    @Path("/pdf")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadPdf(@FormDataParam("file") InputStream in,
                              @FormDataParam("file") FormDataContentDisposition meta) {
        try {
            String fileName = meta.getFileName();              // ex: "book.pdf"
            java.nio.file.Path target = PDF_DIR.resolve(fileName);

            // 1) Sauvegarde en local
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);

            // 2) Envoi au chatbot (on ne casse pas l’upload si ça échoue)
            try {
                sendPdfToChatbot(target);
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("[FileResource] ERREUR lors de l'upload vers le chatbot : "
                        + ex.getMessage());
                // on continue quand même, le PDF est bien sauvegardé pour Libria
            }

            // 3) URL publique pour Libria
            String url = "http://localhost:8081/LibriaService/api/files/pdf/" + fileName;

            Map<String, String> body = Map.of(
                    "fileName", fileName,
                    "url", url
            );

            return Response.status(Response.Status.CREATED)
                    .entity(body)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Unable to save file")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }


}