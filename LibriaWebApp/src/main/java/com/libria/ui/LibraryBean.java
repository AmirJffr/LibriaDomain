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

@Named("libraryBean")
@RequestScoped
public class LibraryBean implements Serializable {
    private List<Map<String,Object>> books;
    private String queryTitle;
    private String queryGenre;

    @Inject
    LibriaWebAppService service;

    @PostConstruct
    public void init() { load(); }

    public String load() {
        books = service.listBooks();
        return null;
    }

    public String search() {
        if (queryTitle == null || queryTitle.isBlank()) {
            return load();
        }
        try {
            books = service.searchBooksByTitle(queryTitle.trim());
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur de recherche", null));
        }
        return null;
    }

    // getters
    public List<Map<String, Object>> getBooks() { return books; }
    public String getQueryTitle() { return queryTitle; }
    public void setQueryTitle(String queryTitle) { this.queryTitle = queryTitle; }
    public String getQueryGenre() {
        return queryGenre;
    }

    public void setQueryGenre(String queryGenre) {
        this.queryGenre = queryGenre;
    }
}