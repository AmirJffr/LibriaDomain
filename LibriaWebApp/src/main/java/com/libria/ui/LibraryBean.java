package com.libria.ui;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named("libraryBean")
@RequestScoped
public class LibraryBean implements Serializable {

    private List<Map<String,Object>> books;
    private String queryTitle;
    private String queryGenre;

    @Inject
    LibriaWebAppService service;

    @PostConstruct
    public void init() {
        load();
    }

    /** Charge la liste complète, puis applique le filtre de genre si besoin */
    public String load() {
        List<Map<String,Object>> all = service.listBooks();
        books = filterByGenre(all);
        return null;
    }

    /** Recherche par titre + filtre catégorie */
    public String search() {
        try {
            List<Map<String,Object>> base;

            if (queryTitle == null || queryTitle.isBlank()) {

                base = service.listBooks();
            } else {

                base = service.searchBooksByTitle(queryTitle.trim());
            }


            books = filterByGenre(base);

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erreur de recherche", null)
            );
        }
        return null;
    }

    /** Petit helper pour filtrer par genre sur une liste de livres (List<Map>) */
    private List<Map<String,Object>> filterByGenre(List<Map<String,Object>> source) {
        if (queryGenre == null || queryGenre.isBlank()) {
            return source; // aucune catégorie choisie → on renvoie tout
        }

        String g = queryGenre.toLowerCase();

        return source.stream()
                .filter(m -> {
                    Object genreObj = m.get("genre");
                    return genreObj != null
                            && genreObj.toString().equalsIgnoreCase(queryGenre);
                })
                .collect(Collectors.toList());
    }

    /* ========= RATING GOOGLE BOOKS ========= */

    public String reviewSummary(String isbn) {
        try {
            Map<String, Object> rating = service.getBookRating(isbn);

            System.out.println("[WebApp] reviewSummary() isbn = " + isbn);
            System.out.println("[WebApp] reviewSummary() rating map = " + rating);

            if (rating == null || rating.isEmpty()) {
                return "—";  // aucune note
            }

            Object avgObj   = rating.get("averageRating");
            Object countObj = rating.get("ratingsCount");

            System.out.println("[WebApp] reviewSummary() avgObj=" + avgObj + " (" +
                    (avgObj != null ? avgObj.getClass() : "null") + ")");
            System.out.println("[WebApp] reviewSummary() countObj=" + countObj + " (" +
                    (countObj != null ? countObj.getClass() : "null") + ")");

            if (avgObj == null || countObj == null) {
                return "—";
            }

            double avg = Double.parseDouble(avgObj.toString());
            int count  = Integer.parseInt(countObj.toString());

            if (count == 0) {
                return "N/A";
            }

            return String.format(java.util.Locale.US, "%.1f (%d)", avg, count);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[WebApp] reviewSummary() EXCEPTION: " + e.getMessage());
            return "N/A";
        }
    }

    // ========= getters / setters =========

    public List<Map<String, Object>> getBooks() {
        return books;
    }

    public String getQueryTitle() {
        return queryTitle;
    }
    public void setQueryTitle(String queryTitle) {
        this.queryTitle = queryTitle;
    }

    public String getQueryGenre() {
        return queryGenre;
    }
    public void setQueryGenre(String queryGenre) {
        this.queryGenre = queryGenre;
    }
}