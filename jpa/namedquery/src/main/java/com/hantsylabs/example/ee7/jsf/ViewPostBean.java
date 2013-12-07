/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hantsylabs.example.ee7.jsf;

import com.hantsylabs.example.ee7.Constants;
import com.hantsylabs.example.ee7.jpa.Comment;
import com.hantsylabs.example.ee7.jpa.Post;
import com.hantsylabs.example.ee7.jpa.VoteDown;
import com.hantsylabs.example.ee7.jpa.VoteUp;
import java.util.logging.Logger;
import javax.ejb.Stateful;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

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

    private int voteUp = 0;
    private int voteDown = 0;

    public void init() {
        log.info("call init, id @" + id);
        if (id != null) {
            this.post = em.find(Post.class, this.id);
            fetchVoteUp();
            fetchVoteDown();
        } else {
            throw new RuntimeException("id is required");
        }
    }

    private void fetchVoteDown() {
        
//        Query query = em.createQuery("select count(vu)from Post p join treat(p.comments  as VoteDown) vu where p.id=:id");     
//        em.getEntityManagerFactory().addNamedQuery("NQ_COUNT_VOTE_UP", query);
        this.voteDown = ((Long) (em.createNamedQuery(Constants.NQ_COUNT_VOTE_UP)
                .setParameter("id", this.id)
                .getSingleResult())).intValue();
    }

    private void fetchVoteUp() {
        this.voteUp = ((Long) (em.createQuery("select count(vu)from Post p join treat(p.comments  as VoteUp) vu where p.id=:id")
                .setParameter("id", this.id)
                .getSingleResult())).intValue();
    }

    public void save() {
        final Comment comment = new Comment(this.commentBody);
        comment.setPost(this.post);
        this.post.getComments().add(comment);
        em.merge(this.post);

        this.commentBody = "";
    }

    public void voteUp() {
        final VoteUp comment = new VoteUp();
        comment.setPost(this.post);
        this.post.getComments().add(comment);

        em.merge(this.post);
        em.flush();

        fetchVoteUp();
        this.commentBody = "";
    }

    public void voteDown() {
        final VoteDown comment = new VoteDown();
        comment.setPost(this.post);
        this.post.getComments().add(comment);

        em.merge(this.post);
        em.flush();

        fetchVoteDown();
        this.commentBody = "";
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

    public int getVoteUp() {
        return voteUp;
    }

    public int getVoteDown() {
        return voteDown;
    }

}
