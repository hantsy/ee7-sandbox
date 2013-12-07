/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hantsylabs.example.ee7;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Named;

/**
 *
 * @author hantsy
 */
@ApplicationScoped
public class Resources {

    @Produces
    public Logger getLogger(InjectionPoint p) {
        System.out.println("@@@produces logger...");
        return Logger.getLogger(p.getMember().getDeclaringClass().getName());
    }
    
    @Produces
    @Named(value="availableTags")
    public List<String> availableTags(){
        String[] tags={"Java EE 7"," JPA 2.1", "Converter"};
        return Arrays.<String>asList(tags);
    }

}
