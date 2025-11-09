package com.libria.ui;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Map;

@Named("registerBean")
@SessionScoped
public class RegisterBean implements Serializable {

    private String userId;
    private String name;
    private String email;
    private String password;

    @Inject
    private LibriaWebAppService service;

    /**
     * Appelé par le bouton "S'inscrire"
     */
    public String register() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        // validation côté UI (basique)
        if (isBlank(userId) || isBlank(name) || isBlank(email) || isBlank(password)) {
            ctx.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Veuillez remplir tous les champs obligatoires.",
                            null));
            return null; // reste sur la page
        }

        try {
            // Prépare le payload pour l'API
            Map<String, Object> newUserPayload = Map.of(
                    "userId", userId,
                    "name", name,
                    "email", email,
                    "password", password
            );

            // Appel REST -> POST /users
            boolean ok = service.registerUser(newUserPayload);

            if (ok) {
                ctx.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Inscription réussie. Vous pouvez vous connecter.",
                                null));

                // reset des champs en mémoire
                clear();

                // redirection vers login.xhtml
                return "login?faces-redirect=true";
            } else {
                ctx.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Impossible de créer le compte (ID déjà utilisé ?).",
                                null));
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            ctx.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erreur serveur lors de l'inscription.",
                            null));
            return null;
        }
    }

    /**
     * Remet les champs à vide après succès
     */
    private void clear() {
        this.userId = null;
        this.name = null;
        this.email = null;
        this.password = null;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    // --- Getters / Setters nécessaires pour JSF ---

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}