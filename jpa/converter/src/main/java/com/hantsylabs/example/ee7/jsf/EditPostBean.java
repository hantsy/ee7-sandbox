/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hantsylabs.example.ee7.jsf;

import com.hantsylabs.example.ee7.jpa.Post;
import java.util.logging.Logger;
import javax.ejb.Stateful;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author hantsy
 */
@Named
@ViewScoped
@Stateful
public class EditPostBean {

    @Inject
    Logger log;
    
    @PersistenceContext
    EntityManager em;

    private Post post;

    public void init() {
        log.info("call init");    
        this.post=new Post();
    }

    public String save(){
        em.persist(this.post);
        return "posts?faces-redirect=true";
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    
}
