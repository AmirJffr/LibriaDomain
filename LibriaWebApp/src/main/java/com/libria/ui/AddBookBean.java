package com.libria.ui;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import java.io.Serializable;
import jakarta.ws.rs.core.Response;
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
    private String coverPath;
    private String pdf;

    @Inject
    private LibriaWebAppService service; // on va lui ajouter une méthode addBook()

    public void submit() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        // construire l'objet comme JSON pour le service REST
        Map<String,Object> payload = Map.of(
                "isbn", isbn,
                "title", title,
                "author", author,
                "year", year,
                "genre", genre,
                "available", available,
                "coverPath", coverPath,
                "pdf", pdf
        );

        Response resp = service.addBook(payload);

        if (resp.getStatus() == 201) {
            ctx.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Livre ajouté ✅", null));
            reset();
        } else {
            String msg = resp.hasEntity() ? resp.readEntity(String.class)
                    : "Erreur ("+resp.getStatus()+")";
            ctx.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            msg, null));
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

    // getters / setters JSF
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
    public void setPdf(String pdf) { this.pdf = pdf; }
}