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

    public String register() {
        FacesContext ctx = FacesContext.getCurrentInstance();


        if (isBlank(name) || isBlank(email) || isBlank(password)) {
            ctx.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Veuillez remplir tous les champs obligatoires.", null));
            return null;
        }

        try {

            String generatedId = generateUserId();
            this.userId = generatedId;

            Map<String, Object> newUserPayload = Map.of(
                    "userId", generatedId,
                    "name", name,
                    "email", email,
                    "password", password
            );

            boolean ok = service.registerUser(newUserPayload);

            if (ok) {
                ctx.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Inscription réussie. Vous pouvez vous connecter.", null));
                ctx.getExternalContext().getFlash().setKeepMessages(true);
                clear();
                return "login?faces-redirect=true";
            } else {
                ctx.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Impossible de créer le compte (ID déjà utilisé ?).", null));
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            ctx.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erreur serveur lors de l'inscription.", null));
            return null;
        }
    }

    private String generateUserId() {
        int num = (int) (Math.random() * 10000); // 0–9999
        return String.format("MB%04d", num);     // MB0000, MB1234, etc.
    }

    private void clear() {
        this.userId = null;
        this.name = null;
        this.email = null;
        this.password = null;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    // Getters / setters

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}