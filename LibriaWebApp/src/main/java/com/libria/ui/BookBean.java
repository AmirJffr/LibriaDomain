package com.libria.ui;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.faces.context.FacesContext;
import jakarta.faces.application.FacesMessage;

import jakarta.ws.rs.core.Response;
import java.io.Serializable;
import java.util.Map;

@Named("bookBean")
@ViewScoped
public class BookBean implements Serializable {

    private static final long serialVersionUID = 1L;


    private String isbn;

    private BookView book;

    @Inject
    private LibriaWebAppService service;

    @Inject
    private LoginBean loginBean;


    public void load() {
        if (isbn == null || isbn.isBlank()) {
            addMsg(FacesMessage.SEVERITY_ERROR,
                    "Aucun ISBN fourni dans l'URL.");
            book = null;
            return;
        }

        try {
            Map<String, Object> map = service.getBook(isbn);

            if (map == null || map.isEmpty()) {
                addMsg(FacesMessage.SEVERITY_ERROR,
                        "Livre introuvable pour l'ISBN " + isbn);
                book = null;
                return;
            }

            this.book = mapToView(map);

        } catch (Exception e) {
            e.printStackTrace();
            addMsg(FacesMessage.SEVERITY_ERROR,
                    "Erreur lors du chargement du livre.");
            book = null;
        }
    }


    public void download() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        String userId = loginBean.getUserId();
        if (userId == null || userId.isBlank()) {
            addMsg(FacesMessage.SEVERITY_ERROR,
                    "Session expirée, veuillez vous reconnecter.");
            return;
        }

        Response resp = service.downloadBook(userId, isbn);
        int status = resp.getStatus();

        if (status == 201) {
            addMsg(FacesMessage.SEVERITY_INFO,
                    "Livre téléchargé ✅");
        } else if (status == 409) {
            addMsg(FacesMessage.SEVERITY_WARN,
                    "Vous avez déjà téléchargé ce livre.");
        } else if (status == 404) {
            addMsg(FacesMessage.SEVERITY_ERROR,
                    "Livre introuvable.");
        } else {
            String msg = resp.hasEntity()
                    ? resp.readEntity(String.class)
                    : "";
            addMsg(FacesMessage.SEVERITY_ERROR,
                    "Erreur (" + status + ") " + msg);
        }
    }


    private BookView mapToView(Map<String, Object> m) {
        BookView v = new BookView();

        v.setIsbn((String) m.get("isbn"));
        v.setTitle((String) m.get("title"));
        v.setAuthor((String) m.get("author"));
        v.setGenre((String) m.get("genre"));

        Object yearObj = m.get("year");
        if (yearObj != null) {
            try {
                v.setYear(Integer.parseInt(yearObj.toString()));
            } catch (NumberFormatException e) {
                v.setYear(0);
            }
        }

        Object availObj = m.get("available");
        if (availObj != null) {
            v.setAvailable(Boolean.parseBoolean(availObj.toString()));
        }

        // si tu veux plus tard : coverImage / pdf
        v.setCoverImage((String) m.get("coverImage"));
        v.setPdf((String) m.get("pdf"));

        return v;
    }

    private void addMsg(FacesMessage.Severity sev, String msg) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(sev, msg, null));
    }



    public String getIsbn() {
        return isbn;
    }
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public BookView getBook() {
        return book;
    }
    public void setBook(BookView book) {
        this.book = book;
    }
    public String getPdfUrl() {
        if (book == null || book.getPdf() == null || book.getPdf().isBlank()) {
            return null;
        }
        return book.getPdf(); // tu stockes déjà l'URL complète
    }

    public String getPdfProxyUrl() {
        if (book == null || book.getPdf() == null || book.getPdf().isBlank()) {
            return null;
        }
        String pdfUrl = book.getPdf();
        int idx = pdfUrl.lastIndexOf('/');
        if (idx < 0 || idx == pdfUrl.length() - 1) {
            return null;
        }
        String fileName = pdfUrl.substring(idx + 1);   // ex: "clean-code.pdf"

        String ctx = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestContextPath();              // ex: "/LibriaWebApp"

        try {
            String encoded = java.net.URLEncoder.encode(fileName, java.nio.charset.StandardCharsets.UTF_8);
            return ctx + "/pdfproxy?file=" + encoded;  // URL que le navigateur va appeler
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String delete() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        String adminId = loginBean.getUserId();
        String role    = loginBean.getRole();

        if (adminId == null || adminId.isBlank()) {
            addMsg(FacesMessage.SEVERITY_ERROR,
                    "Session expirée, veuillez vous reconnecter.");
            return null; // reste sur la même page
        }
        if (!"ADMIN".equals(role)) {
            addMsg(FacesMessage.SEVERITY_ERROR,
                    "Seuls les administrateurs peuvent supprimer des livres.");
            return null;
        }

        Response resp = service.deleteBook(adminId, isbn);
        int status = resp.getStatus();

        if (status == 204) { // supprimé OK
            addMsg(FacesMessage.SEVERITY_INFO,
                    "Livre supprimé avec succès.");
            // Redirection JSF (pas de redirect() manuel)
            return "library?faces-redirect=true";
        }

        String msg = resp.hasEntity() ? resp.readEntity(String.class) : null;

        if (status == 403) {
            addMsg(FacesMessage.SEVERITY_ERROR,
                    msg != null ? msg : "Accès refusé.");
        } else if (status == 404) {
            addMsg(FacesMessage.SEVERITY_ERROR,
                    msg != null ? msg : "Livre introuvable.");
        } else {
            addMsg(FacesMessage.SEVERITY_ERROR,
                    "Erreur (" + status + ") " + (msg != null ? msg : "Erreur inconnue."));
        }

        return null; // rester sur la page en cas d'erreur
    }
    public static class BookView implements Serializable {
        private String isbn;
        private String title;
        private String author;
        private int year;
        private String genre;
        private boolean available;
        private String coverImage;
        private String pdf;

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

        public String getCoverImage() { return coverImage; }
        public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

        public String getPdf() { return pdf; }
        public void setPdf(String pdf) { this.pdf = pdf; }


    }
}