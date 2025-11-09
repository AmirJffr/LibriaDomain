package com.libria.rest;

import com.libria.domain.ApplicationState;
import com.libria.domain.Book;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.io.InputStream;

@Path("/files")
public class FileResource {

    @Inject
    private ApplicationState state;

    // ---------- helpers ----------
    private Response serveCoverByFilename(String filename) {
        if (filename == null || filename.contains("..")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Nom de fichier invalide")
                    .type("text/plain")
                    .build();
        }

        InputStream in = getClass().getClassLoader().getResourceAsStream("cover/" + filename);
        if (in == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Couverture introuvable")
                    .type("text/plain")
                    .build();
        }

        String mime = filename.endsWith(".png") ? "image/png"
                : (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) ? "image/jpeg"
                : filename.endsWith(".gif") ? "image/gif"
                : "application/octet-stream";

        return Response.ok(in, mime).build();
    }

    private Response servePdfByFilename(String filename) {
        if (filename == null || filename.contains("..")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Nom de fichier invalide")
                    .type("text/plain")
                    .build();
        }

        InputStream in = getClass().getClassLoader().getResourceAsStream("pdf/" + filename);
        if (in == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("PDF introuvable")
                    .type("text/plain")
                    .build();
        }

        return Response.ok(in, "application/pdf")
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .build();
    }


    @GET
    @Path("/cover/{filename}")
    public Response getCover(@PathParam("filename") String filename) {
        return serveCoverByFilename(filename);
    }

    @GET
    @Path("/pdf/{filename}")
    public Response getPdf(@PathParam("filename") String filename) {
        return servePdfByFilename(filename);
    }


    @GET
    @Path("/book/{isbn}/cover")
    public Response getBookCover(@PathParam("isbn") String isbn) {
        Book book = state.getLibrary().getBook(isbn);
        if (book == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Livre introuvable")
                    .type("text/plain")
                    .build();
        }
        String coverPath = book.getCoverImage();
        if (coverPath == null || coverPath.isBlank()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Aucune couverture définie pour ce livre")
                    .type("text/plain")
                    .build();
        }
        String filename = coverPath.substring(coverPath.lastIndexOf('/') + 1);
        return serveCoverByFilename(filename);
    }

    @GET
    @Path("/book/{isbn}/pdf")
    public Response getBookPdf(@PathParam("isbn") String isbn) {
        Book book = state.getLibrary().getBook(isbn);
        if (book == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Livre introuvable")
                    .type("text/plain")
                    .build();
        }
        String pdfPath = book.getPdf();
        if (pdfPath == null || pdfPath.isBlank()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Aucun PDF défini pour ce livre")
                    .type("text/plain")
                    .build();
        }
        String filename = pdfPath.substring(pdfPath.lastIndexOf('/') + 1);
        return servePdfByFilename(filename);
    }
}