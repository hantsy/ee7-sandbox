# JPA 2.1: CDI Support

In JPA 2.0 or the early version, you can not `@Inject` a CDI bean into JPA facilities. 

Utilize Apache CODI or JBoss Seam 3, `@Inject` could be supported in  **Entity Listener** classes.

Luckily, JPA 2.1 bring native CDI support.

Have a look at the `PostListener` in the *jpa-converter* example.

<pre>
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
</pre>

In this example, a CDI managed bean `Logger` is injected into `PostListener` and is use for tracking the persisting and updating of the `Post`entity.

<pre>
@EntityListeners(PostListener.class)
public class Post implements Serializable {...}
</pre>

In the `Post` entity class, apply this EntityListener. When run this project on Glassfish 4, the related log will be displayed in the NetBeans Console view.

But unfortunately, when you try to add the same lifecycle callbacks in the `@Entity` class, the `@Inject` annotation does not work, neither in the new introduced `@Converter`.

The sample codes are hosted on my github.com account, check out and play it yourself.

[https://github.com/hantsy/ee7-sandbox](https://github.com/hantsy/ee7-sandbox)
