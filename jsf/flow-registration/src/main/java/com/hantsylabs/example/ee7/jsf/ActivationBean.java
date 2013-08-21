package com.hantsylabs.example.ee7.jsf;

import java.io.Serializable;
import java.util.Date;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.flow.FlowScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@FlowScoped("activation")
public class ActivationBean implements Serializable {

    @Inject
    Logger log;

    @Inject
    UserService userService;

    private String email;
    private String tokenValue;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public String getReturnValue() {
        return "/return1";
    }

    public String activate() {
        log.info("call activate....");

        User user = userService.findByEmail(this.email);
        if (user == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "User " + this.getEmail() + " not found!", "User " + this.getEmail() + " not found!"));
            return null;
        }

        if ("123".equals(this.tokenValue)) {
            user.setActivated(true);
            user.setActivatedOn(new Date());
            userService.save(user);
            return "ok";
        }

        return null;
    }
}
