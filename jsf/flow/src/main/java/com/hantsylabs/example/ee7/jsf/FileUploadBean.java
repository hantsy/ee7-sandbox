/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hantsylabs.example.ee7.jsf;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.inject.Model;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.Part;

/**
 *
 * @author hantsy
 */
@Model
public class FileUploadBean {
    
    @Inject Logger log;
    
    private Part file;
    
    public void upload(){
        log.info("call upload...");      
        log.log(Level.INFO, "content-type:{0}", file.getContentType());
        log.log(Level.INFO, "filename:{0}", file.getName());
        log.log(Level.INFO, "submitted filename:{0}", file.getSubmittedFileName());
        log.log(Level.INFO, "size:{0}", file.getSize());
        try {
            
            byte[] results=new byte[(int)file.getSize()];
            InputStream in=file.getInputStream();
            in.read(results);         
        } catch (IOException ex) {
           log.log(Level.SEVERE, " ex @{0}", ex);
        }
        
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Uploaded!"));
    }

    public Part getFile() {
        return file;
    }

    public void setFile(Part file) {
        this.file = file;
    }
    
}
