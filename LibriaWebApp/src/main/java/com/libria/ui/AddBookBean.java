package com.libria.ui;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import jakarta.ws.rs.core.Response;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import java.io.Serializable;
import java.util.Map;

@Named("addBookBean")
@ViewScoped
public class AddBookBean implements Serializable {

    private String isbn;
    private String title;
    private String author;
    private int year;
    private String genre;
    private boolean available = true;

    // URL retournées par le service upload
    private String coverPath;   // pour "cover"
    private String pdf;     // pour "pdf"

    @Inject
    private LibriaWebAppService service;

    @Inject
    private LoginBean loginBean;


    public void handleCoverUpload(FileUploadEvent event) {
        try {
            UploadedFile file = event.getFile();

            String url = service.uploadCover(file); // POST /files/cover
            this.coverPath = url;

            addMsg(FacesMessage.SEVERITY_INFO,
                    "Couverture importée : " + file.getFileName());
        } catch (Exception e) {
            e.printStackTrace();
            addMsg(FacesMessage.SEVERITY_ERROR,
                    "Erreur lors de l'upload de la couverture.");
        }
    }


    public void handlePdfUpload(FileUploadEvent event) {
        try {
            UploadedFile file = event.getFile();

            String url = service.uploadPdf(file);   // POST /files/pdf
            this.pdf = url;

            addMsg(FacesMessage.SEVERITY_INFO,
                    "PDF importé : " + file.getFileName());
        } catch (Exception e) {
            e.printStackTrace();
            addMsg(FacesMessage.SEVERITY_ERROR,
                    "Erreur lors de l'upload du PDF.");
        }
    }


    public void submit() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        if (coverPath == null || coverPath.isBlank() ||
                pdf == null || pdf.isBlank()) {
            addMsg(FacesMessage.SEVERITY_WARN,
                    "Veuillez importer une couverture et un PDF.");
            return;
        }

        Map<String,Object> payload = Map.of(
                "isbn", isbn,
                "title", title,
                "author", author,
                "year", year,
                "genre", genre,
                "available", available,
                "coverImage", coverPath,
                "pdf", pdf
        );


        String adminId = loginBean.getUserId();

        Response resp = service.addBook(adminId, payload);

        if (resp.getStatus() == 201) {
            addMsg(FacesMessage.SEVERITY_INFO, "Livre ajouté à Libria et au Chat Bot assistant ✅");
            reset();
        } else {
            String msg = resp.hasEntity()
                    ? resp.readEntity(String.class)
                    : "Erreur (" + resp.getStatus() + ")";
            addMsg(FacesMessage.SEVERITY_ERROR, msg);
        }
    }

    private void reset() {
        isbn = "";
        title = "";
        author = "";
        year = 0;
        genre = "";
        available = true;
        coverPath = "";
        pdf = "";
    }

    private void addMsg(FacesMessage.Severity sev, String msg) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(sev, msg, null));
    }



    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getCoverPath() { return coverPath; }
    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }

    public String getPdf() { return pdf; }
    public void setPdf(String pdfPath) { this.pdf = pdfPath; }
}