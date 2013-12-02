/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hantsylabs.example.ee7.jpa;

import java.util.Date;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

/**
 *
 * @author hantsy
 */
public class PostListener {

    @Inject
    Logger log;

    @PrePersist
    public void prePresist(Object o) {
        log.info("call prePresist");
        if (o instanceof Post) {
            Post post = (Post) o;
            final Date created = new Date();
            post.setCreated(created);
            post.setLastModified(created);
        }
    }

    @PreUpdate
    public void preUpdate(Object o) {
        log.info("call preUpdate");
        if (o instanceof Post) {
            Post post = (Post) o;
            post.setLastModified(new Date());
        }
    }
}
