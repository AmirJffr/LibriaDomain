// src/main/java/com/libria/ui/DownloadsBean.java
package com.libria.ui;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Named("downloadsBean")
@ViewScoped
public class DownloadsBean implements Serializable {
    private List<Map<String,Object>> books = new ArrayList<>();
    private List<Map<String,Object>> filtered = new ArrayList<>();
    private String queryTitle;
    private String queryGenre;

    @Inject LibriaWebAppService service;
    @Inject LoginBean loginBean;

    @PostConstruct
    public void init() { reload(); }

    public void reload() {
        var userId = loginBean.getUserId();
        if (userId == null || userId.isBlank()) {
            books = List.of();
            filtered = List.of();
            return;
        }
        books = service.listDownloads(userId);
        filtered = new ArrayList<>(books);
    }

    public void filter() {
        String t = queryTitle==null? "" : queryTitle.trim().toLowerCase();
        String g = queryGenre==null? "" : queryGenre.trim().toLowerCase();
        filtered = books.stream().filter(b ->
                (t.isEmpty() || String.valueOf(b.get("title")).toLowerCase().contains(t)) &&
                        (g.isEmpty() || String.valueOf(b.get("genre")).toLowerCase().contains(g))
        ).toList();
    }

    public void remove(String isbn) {
        var ctx = FacesContext.getCurrentInstance();
        var r = service.removeDownload(loginBean.getUserId(), isbn);
        if (r.getStatus()==204) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Retiré ✅", null));
            reload();
        } else {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur ("+r.getStatus()+")", null));
        }
    }

    // getters/setters pour la page
    public List<Map<String,Object>> getBooks() { return filtered; }
    public String getQueryTitle() { return queryTitle; }
    public void setQueryTitle(String queryTitle) { this.queryTitle = queryTitle; }
    public String getQueryGenre() { return queryGenre; }
    public void setQueryGenre(String queryGenre) { this.queryGenre = queryGenre; }
}