/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hantsylabs.example.ee7.jsf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author hantsy
 */

@ApplicationScoped
@Named
public class UserService implements Serializable{
    
    @Inject transient Logger log;
   
    private Map<String, User> users=new HashMap<String, User>();
    
    public void save(User user){
        log.info("saving user...");
        users.put(user.getEmail(), user);
    }
    
    public User findByEmail(String email){
        log.info("find by email...@"+email);
        return users.get(email);
    }
    
    public List<User> getUserList(){
        log.info("user list size@"+users.size());
        return new ArrayList(this.users.values());
    }
}
