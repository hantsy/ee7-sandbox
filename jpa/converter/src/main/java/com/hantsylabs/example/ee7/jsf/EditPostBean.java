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

    private Long id;

    public void init() {
        log.info("call init, id @" + id);
        if (id != null) {
            this.post = em.find(Post.class, this.id);
        } else {
            this.post = new Post();
        }
    }

    public String save() {
        if (this.id != null) {
            em.merge(this.post);
        } else {
            em.persist(this.post);
        }
        return "posts?faces-redirect=true";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

}
