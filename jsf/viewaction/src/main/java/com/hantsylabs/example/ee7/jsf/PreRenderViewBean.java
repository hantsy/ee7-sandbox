/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hantsylabs.example.ee7.jsf;

import java.util.logging.Logger;
import javax.enterprise.inject.Model;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

/**
 *
 * @author hantsy
 */
@Model
public class PreRenderViewBean {
    
    @Inject Logger log;
    
    private String flag="page1";
    
    public void init(ComponentSystemEvent e){
        log.info("call init");
        log.info("flag @"+ flag);
        
        if(!FacesContext.getCurrentInstance().isPostback()){
            //initialized some data...but avoid loading in postback request
        }
        
        if("page2".equals(flag)){
            ConfigurableNavigationHandler handler=(ConfigurableNavigationHandler)  
                    FacesContext.getCurrentInstance().getApplication().getNavigationHandler();      
            handler.performNavigation("page2");
        }
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }
    
}
