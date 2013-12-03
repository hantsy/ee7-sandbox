/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hantsylabs.example.ee7.jsf;

import com.hantsylabs.example.ee7.jpa.Post;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.ejb.Stateful;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;

/**
 *
 * @author hantsy
 */
@Stateful
@ViewScoped
@Named
public class PostsBean {

    @Inject
    Logger log;

    @PersistenceContext
    EntityManager em;

    private Map<Long, Boolean> checked = new HashMap<>();

    private List<Post> posts = new ArrayList<>();

    public void init() {
        log.info("call init");
        load();
        initCheckedMap();
    }

    private void load() {
        this.posts = em.createQuery("select p from Post p order by p.created desc", Post.class).getResultList();
        log.info(this.posts.size() + " posts found");      
    }

    private void initCheckedMap() {
        for (Post p : posts) {
            if (!checked.containsKey(p.getId())) {
                checked.put(p.getId(), false);
            }
        }
        log.info("checked map size@"+checked.size());
    }

    public List<Long> getCheckedList() {
        List<Long> result = new ArrayList<Long>();
        for (Long id : checked.keySet()) {
            if (checked.get(id).booleanValue()) {
                result.add(id);
            }
        }
        log.info("checked list size @"+result.size());
        return result;
    }

    public void update() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaUpdate<Post> q = cb.createCriteriaUpdate(Post.class);
        Root<Post> root = q.from(Post.class);
        q.set(root.get("approved"), true)
                .where(root.get("id").in(getCheckedList()));

        int result = em.createQuery(q).executeUpdate();
        log.info("update @" + result);
        load();
    }

    public void delete() {
        final List<Long> checkedList = getCheckedList();
        for (Long id : checkedList) {
            checked.remove(id);
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<Post> q = cb.createCriteriaDelete(Post.class);
        Root<Post> root = q.from(Post.class);
        q.where(root.get("id").in(checkedList));

        int result = em.createQuery(q).executeUpdate();
        log.info("delete @" + result);
        load();
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public Map<Long, Boolean> getChecked() {
        return checked;
    }

    public void setChecked(Map<Long, Boolean> checked) {
        this.checked = checked;
    }

}
