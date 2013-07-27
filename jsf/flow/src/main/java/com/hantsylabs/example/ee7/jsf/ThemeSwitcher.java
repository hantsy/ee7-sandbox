/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hantsylabs.example.ee7.jsf;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author hantsy
 */
@Named
@SessionScoped
public class ThemeSwitcher implements Serializable {

    @Inject
    transient Instance<Logger> log;

    private String theme = "default";

    
   public void changeTheme( String _theme){
       // log.get().log(Level.INFO, "call changeTheme{0}", _theme);
        switch (_theme) {
            case "alternative":
                this.theme = "alternative";
                break;
            default:
                this.theme = "default";
                break;
        }
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
    
}
