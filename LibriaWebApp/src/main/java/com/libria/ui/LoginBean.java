package com.libria.ui;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Map;

@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String password;
    private String role;

    @Inject
    private LibriaWebAppService service;

    public String login() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        if (userId == null || userId.isBlank() || password == null || password.isBlank()) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Veuillez remplir tous les champs.", null));
            return null;
        }

        try {
            Map<String, Object> res = service.login(userId, password);

            if (res == null || res.isEmpty()) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Identifiants invalides", null));
                return null;
            }

            this.role = (String) res.get("role");

            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Connexion réussie", null));

            // ✅ redirige vers la page principale
            return "library?faces-redirect=true";

        } catch (Exception e) {
            e.printStackTrace();
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Erreur de connexion au serveur", null));
            return null;
        }
    }

    public String logout() {
        try {
            service.logout();
        } catch (Exception ignored) {}

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Déconnexion réussie", null));

        this.userId = null;
        this.password = null;
        this.role = null;

        // retour à la page de connexion
        return "login?faces-redirect=true";
    }

    // --- Getters / Setters ---
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}