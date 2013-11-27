/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hantsylabs.example.ee7.jsf;

import com.hantsylabs.example.ee7.jpa.Post;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author hantsy
 */
@Model
public class PostsBean {

    @Inject
    Logger log;
    
    @PersistenceContext
    EntityManager em;

    private List<Post> posts = new ArrayList<>();

    public void init() {
        log.info("call init");    
        this.posts=em.createQuery("select p from Post p order by p.created desc", Post.class).getResultList(); 
        log.info(this.posts.size()+" posts found");
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    
}
