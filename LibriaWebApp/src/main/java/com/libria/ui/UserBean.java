package com.libria.ui;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;

@Named("userBean")
@SessionScoped
public class UserBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private LoginBean loginBean;

    @Inject
    private LibriaWebAppService service;

    public void download(String isbn) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        String userId = loginBean.getUserId();

        var resp = service.downloadBook(userId, isbn);
        int status = resp.getStatus();

        if (status == 201) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Livre téléchargé ✅", null));
        } else if (status == 409) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Déjà téléchargé.", null));
        } else if (status == 404) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Livre introuvable.", null));
        } else {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur (" + status + ")", null));
        }
    }

    public boolean hasDownloaded(String isbn) {
        String userId = loginBean.getUserId();
        try {
            var list = service.listDownloads(userId); // List<Map>
            if (list == null) return false;

            return list.stream().anyMatch(d ->
                    isbn.equals(d.get("isbn"))
            );
        } catch (Exception e) {
            return false;
        }
    }
}