/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.hantsylabs.example.ee7;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author hantsy
 */
@Startup
@Singleton
public class ApplicationInitializer {
    
    @PersistenceContext
    EntityManager em;
    
    @PostConstruct
    public void postConstruct(){
        System.out.println("@@@application is iniitlized...");
        Query query = em.createQuery("select count(vu)from Post p join treat(p.comments  as VoteDown) vu where p.id=:id");     
        em.getEntityManagerFactory().addNamedQuery(Constants.NQ_COUNT_VOTE_UP, query);
    }
    
}
