package com.libria.ui;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Map;

@Named("profileBean")
@ViewScoped
public class ProfileBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String role;
    private String name;
    private String email;

    private String currentPassword;
    private String newPassword;
    private String confirmPassword;

    @Inject
    private LoginBean loginBean;

    @Inject
    private LibriaWebAppService service;

    @PostConstruct
    public void init() {
        // user courant depuis la session
        userId = loginBean.getUserId();
        role   = loginBean.getRole();

        if (userId == null || userId.isBlank()) {
            addMsg(FacesMessage.SEVERITY_ERROR,
                    "Session expirée. Veuillez vous reconnecter.");
            return;
        }

        try {

            Map<String,Object> profile = service.getUserProfile(userId);
            if (profile == null || profile.isEmpty()) {
                addMsg(FacesMessage.SEVERITY_WARN,
                        "Impossible de charger votre profil.");
                return;
            }

            this.name  = (String) profile.getOrDefault("name", "");
            this.email = (String) profile.getOrDefault("email", "");

        } catch (Exception e) {
            e.printStackTrace();
            addMsg(FacesMessage.SEVERITY_ERROR,
                    "Erreur lors du chargement du profil.");
        }
    }

    public void save() {
        if (name == null || name.isBlank()
                || email == null || email.isBlank()) {
            addMsg(FacesMessage.SEVERITY_WARN,
                    "Le nom et l'email sont obligatoires.");
            return;
        }


        boolean changePassword =
                (newPassword != null && !newPassword.isBlank()) ||
                        (confirmPassword != null && !confirmPassword.isBlank());

        if (changePassword) {
            if (currentPassword == null || currentPassword.isBlank()) {
                addMsg(FacesMessage.SEVERITY_WARN,
                        "Veuillez saisir votre mot de passe actuel.");
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                addMsg(FacesMessage.SEVERITY_WARN,
                        "Les nouveaux mots de passe ne correspondent pas.");
                return;
            }
        }

        try {

            var respProfile = service.updateUserProfile(userId, name, email, role);
            int statusProfile = respProfile.getStatus();

            if (statusProfile < 200 || statusProfile >= 300) {
                String body = respProfile.hasEntity() ? respProfile.readEntity(String.class) : "";
                addMsg(FacesMessage.SEVERITY_ERROR,
                        "Impossible de mettre à jour le profil (" + statusProfile + ") " + body);
                return;
            }


            loginBean.setEmail(email);

            loginBean.setRole(role);


            if (changePassword) {
                var respPwd = service.updateUserPassword(userId, newPassword);
                int statusPwd = respPwd.getStatus();

                if (statusPwd < 200 || statusPwd >= 300) {
                    String body = respPwd.hasEntity() ? respPwd.readEntity(String.class) : "";
                    addMsg(FacesMessage.SEVERITY_ERROR,
                            "Profil mis à jour, mais échec du changement de mot de passe (" +
                                    statusPwd + ") " + body);
                    return;
                }

                // reset champs mot de passe
                currentPassword = "";
                newPassword = "";
                confirmPassword = "";
            }

            addMsg(FacesMessage.SEVERITY_INFO,
                    "Profil mis à jour avec succès.");

        } catch (Exception e) {
            e.printStackTrace();
            addMsg(FacesMessage.SEVERITY_ERROR,
                    "Erreur serveur lors de la mise à jour du profil.");
        }
    }

    private void addMsg(FacesMessage.Severity sev, String msg) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(sev, msg, null));
    }

    // Getters / setters

    public String getUserId() { return userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}