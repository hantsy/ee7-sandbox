/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.hantsylabs.example.ee7.jpa;

import javax.persistence.Entity;

/**
 *
 * @author hantsy
 */
@Entity
public class VoteUp  extends Comment{

    public VoteUp() {
        super("Up");
    }
    
}
