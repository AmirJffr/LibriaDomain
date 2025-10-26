package com.libria.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.io.InputStream;

/**
 * Sert les fichiers statiques : couvertures et PDFs.
 * Exemple:
 *   GET /api/files/cover/dune.png  -> image/png
 *   GET /api/files/pdf/dune.pdf    -> application/pdf
 */
@Path("/files")
public class FileResource {

    /**
     * Récupère une image de couverture depuis src/main/resources/cover/
     */
    @GET
    @Path("/cover/{filename}")
    public Response getCover(
            @PathParam("filename") String filename,
            @Context Request request) {

        // sécurité simple : pas de "../"
        if (filename.contains("..")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Nom de fichier invalide")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }

        // essaie de charger le fichier depuis le classpath
        InputStream in = getClass().getClassLoader()
                .getResourceAsStream("cover/" + filename);

        if (in == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Couverture introuvable")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }

        // type MIME basique par extension
        String mime;
        if (filename.endsWith(".png")) {
            mime = "image/png";
        } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            mime = "image/jpeg";
        } else if (filename.endsWith(".gif")) {
            mime = "image/gif";
        } else {
            mime = "application/octet-stream";
        }

        return Response.ok(in, mime).build();
    }

    /**
     * Récupère un PDF du livre depuis src/main/resources/pdf/
     */
    @GET
    @Path("/pdf/{filename}")
    public Response getPdf(
            @PathParam("filename") String filename,
            @Context Request request) {

        if (filename.contains("..")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Nom de fichier invalide")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }

        InputStream in = getClass().getClassLoader()
                .getResourceAsStream("pdf/" + filename);

        if (in == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("PDF introuvable")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }

        // on renvoie en "application/pdf"
        return Response.ok(in, "application/pdf")
                // ça dit au navigateur "télécharge" au lieu "ouvre dans l'onglet"
                .header("Content-Disposition",
                        "attachment; filename=\"" + filename + "\"")
                .build();
    }
}