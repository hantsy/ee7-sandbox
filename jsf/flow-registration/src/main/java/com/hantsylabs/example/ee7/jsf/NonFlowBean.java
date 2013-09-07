/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hantsylabs.example.ee7.jsf;

import javax.enterprise.inject.Model;

/**
 *
 * @author hantsy
 */
@Model
public class NonFlowBean {
    
    public String go(){
        return "/nonFlow?faces-redirect=true";
    }
    
}
