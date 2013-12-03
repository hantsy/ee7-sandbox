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

    @PersistenceContext
    EntityManager em;

    private Post post;

    private Long id;

    private String commentBody;

    public void init() {
        log.info("call init, id @" + id);
        if (id != null) {
            //this.post = em.find(Post.class, this.id);
            EntityGraph postEntityGraph=em.getEntityGraph("post");
//            EntityGraph postEntityGraph=em.createEntityGraph(Post.class);
//            postEntityGraph.addAttributeNodes("title");
//            postEntityGraph.addSubgraph("comments").addAttributeNodes("content");
            
            this.post=em
                    .createQuery("select p from Post p where p.id=:id", Post.class)
                    .setHint("javax.persistence.loadgraph", postEntityGraph)
                    .setParameter("id", this.id)
                    .getResultList()
                    .get(0);
            
            PersistenceUnitUtil util=em.getEntityManagerFactory().getPersistenceUnitUtil();
            
            log.info("title is loadded@"+util.isLoaded(this.post, "title"));
            log.info("body is loadded@"+util.isLoaded(this.post, "body"));
            log.info("comments is loadded@"+util.isLoaded(this.post, "comments"));
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
