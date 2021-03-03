# JPA 2.1: Bulk Update and Delete

In the JPA 2.0 and early version, if you want to execute a bulk updating query, you have to use *update* or *delete* clause in JPQL directly.  JPA 2.1 introduce new Criteria API for updating and deleting.

## Post entity

Reuse the `Post` entity class as example. Add an extra **boolean** *approved* property.  

In this example, the bulk update operation will set the *approved* value to **true**.

<pre>
@Entity
@Table(name="POSTS")
public class Post implements Serializable {
	
     private boolean approved = false;
}
</pre>



## Criteria Update and Delete API

In JPA 2.1, new *CriteriaUpdate* and *CriteriaDelete* are introduced in Criteria API for updating and deleting. The usage is very simple.

The *CriteriaUpdate* example.
  
<pre>
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaUpdate&lt;Post> q = cb.createCriteriaUpdate(Post.class);
Root&lt;Post> root = q.from(Post.class);
q.set(root.get("approved"), true)
	.where(root.get("id").in(getCheckedList()));

int result = em.createQuery(q).executeUpdate();
log.info("update @" + result);
</pre>

The *CriteriaDelete* example.

<pre>
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaDelete&lt;Post> q = cb.createCriteriaDelete(Post.class);
Root&lt;Post> root = q.from(Post.class);
q.where(root.get("id").in(checkedList));

int result = em.createQuery(q).executeUpdate();
log.info("delete @" + result);
</pre>

The *checkedList* value is from checkbox values from JSF UI.

## Summary

This feature is a small improvement to JPA API. If you are stick on the type-safe JPA metadata API for JPA programming, it is a big step.

The sample codes are hosted on my github.com account, check out and play it yourself.

[https://github.com/hantsy/ee7-sandbox](https://github.com/hantsy/ee7-sandbox)

When you run the project(jpa-bulk) on Glassfish 4.0 and you could get an exception.

<pre>
java.lang.RuntimeException: unable to create policy context directory.
</pre>

There is a known issue in Glassfish 4.0, the fix should be included in the next release. I am using a [Nightly version](http://dlc.sun.com.edgesuite.net/glassfish/4.0.1/) to overcome this barrier temporarily.
