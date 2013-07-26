/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hantsylabs.example.ee7.jsf;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author hantsy
 */
@Named
@ViewScoped
public class Html5Bean {

    @Inject
    Logger log;

    private String text;
    private Integer number;
    private Integer range;
    private String email;
    private Date date;
    private String url;

    private Map<String, String> attrs = new HashMap<String, String>();

    @PostConstruct
    public void init() {
        log.info(" call init@");
        this.attrs.put("type", "range");
        this.attrs.put("min", "1");
        this.attrs.put("max", "10");
        this.attrs.put("step", "2");
    }

    public String submit() {
        log.info(" call submit@");
        return null;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Logger getLog() {
        return log;
    }

    public void setLog(Logger log) {
        this.log = log;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getRange() {
        return range;
    }

    public void setRange(Integer range) {
        this.range = range;
    }

    public Map<String, String> getAttrs() {
        return attrs;
    }

}
