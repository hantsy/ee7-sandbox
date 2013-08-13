package com.hantsylabs.example.ee7.jsf;

import java.io.Serializable;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.flow.FlowScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@FlowScoped("registration")
public class RegistrationBean implements Serializable {

    @Inject
    transient Logger log;

    @Inject
    UserService userService;

    private boolean licenseAccepted = false;

    private User user = null;

    private String passwordConfirm;

    @PostConstruct
    public void init() {
        log.info("call init...");
        user = new User();
    }

    public boolean isLicenseAccepted() {
        return licenseAccepted;
    }

    public void setLicenseAccepted(boolean licenseAccepted) {
        this.licenseAccepted = licenseAccepted;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public String getReturnValue() {
        return "/return1";
    }

    public String accept() {
        log.info("call accept...");
        if (this.licenseAccepted) {
            return "account";
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "You have to read and accept the license!", "You have to read and accept the license!"));
            return null;
        }
    }

    public String confirm() {
        log.info("call confirm...");
        
        if (!user.getPassword().equals(passwordConfirm)) {
            FacesContext.getCurrentInstance().addMessage("password", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Password is mismatched!", "Password is mismatched!"));
            return null;
        }
        
        if (userService.findByEmail(this.user.getEmail()) != null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "User " + this.user.getEmail() + " is existed!", "User " + this.user.getEmail() + " is existed!"));
            return null;
        }

        userService.save(this.user);
        
        return "confirm";
    }
}
