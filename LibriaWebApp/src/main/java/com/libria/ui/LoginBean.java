package com.libria.ui;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // ---------- Champs de login ----------
    private String email;      // utilisé pour se connecter
    private String password;

    // ---------- Infos de session ----------
    private String userId;     // MBxxxx renvoyé par l'API
    private String role;       // ADMIN / USER ...

    @Inject
    private LibriaWebAppService service;

    // ---------- Helpers de session ----------
    public static HttpSession getSession(boolean create) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null) return null;
        var ext = fc.getExternalContext();
        if (ext == null) return null;
        return (HttpSession) ext.getSession(create);
    }

    public static void invalidateSession() {
        HttpSession s = getSession(false);
        if (s != null) s.invalidate();
    }

    private void addMsg(FacesMessage.Severity sev, String msg) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(sev, msg, null));
    }

    // ---------- Actions ----------

    public String login() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        if (isBlank(email) || isBlank(password)) {
            addMsg(FacesMessage.SEVERITY_WARN,
                    "Veuillez saisir l'email et le mot de passe.");
            return null;
        }

        try {
            // appel REST avec email + password
            Map<String, Object> data = service.loginByEmail(email, password);

            if (data == null || data.isEmpty()) {
                addMsg(FacesMessage.SEVERITY_ERROR,
                        "Email ou mot de passe incorrect.");
                return null;
            }

            // valeurs renvoyées par le service
            this.userId = (String) data.get("userId");             // MBxxxx
            this.role   = (String) data.getOrDefault("role", "USER");

            // redirection vers la bibliothèque
            return "library?faces-redirect=true";

        } catch (Exception e) {
            e.printStackTrace();
            addMsg(FacesMessage.SEVERITY_ERROR,
                    "Erreur serveur lors de la connexion.");
            return null;
        }
    }

    public String logout() {
        try {
            service.logout();   // si le backend gère quelque chose
        } catch (Exception ignored) {}

        invalidateSession();    // détruit la session HTTP

        this.email = null;
        this.password = null;
        this.userId = null;
        this.role = null;

        addMsg(FacesMessage.SEVERITY_INFO, "Déconnexion réussie.");
        return "login?faces-redirect=true";
    }

    public boolean isLoggedIn() {
        return userId != null && !userId.isBlank();
    }

    public void checkLoggedIn() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx == null) return;


        String viewId = ctx.getViewRoot().getViewId();

        // On laisse passer la page login (et éventuellement register)
        if (viewId != null && (viewId.endsWith("login.xhtml") || viewId.endsWith("register.xhtml"))) {
            return;
        }

        if (!isLoggedIn()) {
            try {
                var ec = ctx.getExternalContext();
                // redirection vers login + arrêt du cycle JSF
                ec.redirect(ec.getRequestContextPath() + "/login.xhtml");
                ctx.responseComplete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    // ---------- Getters / Setters (JSF) ----------

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

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
}