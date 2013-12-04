/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hantsylabs.example.ee7.jsf;

import com.hantsylabs.example.ee7.jpa.Comment;
import com.hantsylabs.example.ee7.jpa.Post;
import java.util.logging.Logger;
import javax.ejb.Stateful;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.SynchronizationType;

/**
 *
 * @author hantsy
 */
@Named
@ViewScoped
@Stateful
public class ViewPostBean {

    @Inject
    Logger log;

    @PersistenceContext(synchronization = SynchronizationType.UNSYNCHRONIZED)
    EntityManager em;

    private Post post;

    private Long id;

    private String commentBody;

    public void init() {
        log.info("call init, id @" + id);
        if (id != null) {
            this.post = em.find(Post.class, this.id);
        } else {
            throw new RuntimeException("id is required");
        }
    }

    public void save() {
        final Comment comment = new Comment(this.commentBody);
        comment.setPost(this.post);
        this.post.getComments().add(comment);
        em.merge(this.post);
        
        this.commentBody="";
    }
    
     public void saveWithJoinTransaction() {
        final Comment comment = new Comment(this.commentBody);
        comment.setPost(this.post);
        this.post.getComments().add(comment);
        
        em.joinTransaction();
        em.merge(this.post);
        
        this.commentBody="";
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

    public String getCommentBody() {
        return commentBody;
    }

    public void setCommentBody(String commentBody) {
        this.commentBody = commentBody;
    }

}
