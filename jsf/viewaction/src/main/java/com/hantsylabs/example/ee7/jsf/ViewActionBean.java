/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hantsylabs.example.ee7.jsf;

import java.util.logging.Logger;
import javax.enterprise.inject.Model;
import javax.inject.Inject;

/**
 *
 * @author hantsy
 */
@Model
public class ViewActionBean {
    
    @Inject Logger log;
    
    private String flag="page1";
    
    public String init(){
        log.info("call init");
        switch(flag){
            case "page1":
                return "page1";
            default:
                return "page2";  
        } 
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }
    
}
