# Authentication and Authorization:JWT and CDI

As Spring competier, CDI was introduced as part of Java EE 6 which provides a standard IOC container for Java EE, and also provides lots of new features that are not included in Spring, such as a simple Event processing mechanism, Decorate, Alternative etc.

In [our before example](https://github.com/hantsy/angularjs-ee7-sample/wiki/5a-security-jwt), we can use CDI to expose a **current user** into `RequestScoped` in the `AuthenticationFilter`, and use it anywhere within the request.

Inject a `@AuthenticatedUser` CDI Event in `AuthenticationFilter`.

```java
@Inject
@AuthenticatedUser
Event<String> userAuthenticatedEvent;
```

`AuthenticatedUser` is standard CDI `@Qualifier` to identify CDI beans.

After extracted user information from JWT token, fire `userAuthenticatedEvent`.

```java
String username = jwtHelper.parseToken(token);
userAuthenticatedEvent.fire(username);
```

We do not setup the `SecurityContext` in this sample, so JAAS annotations is not activiated.

`AuthenticatedUserProducer` is responsible for handling this event.

```java
@RequestScoped
public class AuthenticatedUserProducer {

    @Produces
    @RequestScoped
    @AuthenticatedUser
    private User authenticatedUser;

    @Inject
    UserRepository users;

    public void handleAuthenticationEvent(@Observes @AuthenticatedUser String username) {
        this.authenticatedUser = users.findByUsername(username);
    }

}
```

In the above codes, `handleAuthenticationEvent` method accepts the event payload as arguments. In this method, it queries user by username and exposes the result in `RequestScoped`.

```java
@Produces
@RequestScoped
@AuthenticatedUser
private User authenticatedUser;
```

Now there is a `User` bean available in `@RequestScoped`.

In `AuthorizationFilter`, you can inject it and use it to process authorization.

```java
@Inject
@AuthenticatedUser
User currentUser;
```

You can also get the current user in business logic codes. 

For example, add auditor automaticially in the entities. Use a JPA `EntityListener` to archive this purpose.

```java
public class AuditEntityListener {

    private static final Logger LOG = Logger.getLogger(AuditEntityListener.class.getName());

//    @Inject
//    @AuthenticatedUser
//    User user;
//    @PersistenceContext
//    EntityManager em;
    @PrePersist
    public void beforePersist(Object entity) {
        if (entity instanceof AbstractAuditableEntity) {
            AbstractAuditableEntity o = (AbstractAuditableEntity) entity;
            final OffsetDateTime now = OffsetDateTime.now();
            o.setCreatedAt(now);
            o.setUpdatedAt(now);
            o.setCreatedBy(currentUser());
        }
    }

    @PreUpdate
    public void beforeUpdate(Object entity) {
        if (entity instanceof AbstractAuditableEntity) {
            AbstractAuditableEntity o = (AbstractAuditableEntity) entity;
            o.setUpdatedAt(OffsetDateTime.now());
            o.setUpdatedBy(currentUser());
        }
    }

    private String currentUser() {
        User user = CDI.current().select(User.class, new AuthenticatedUserLiteral()).get();
        LOG.log(Level.FINEST, "get current user form EntityListener@{0}", user);
        return user.getUsername();
    }
}
```

NOTE: JPA 2.1 should support `@Inject` in EntityListener class, I have written a post to describe the [JPA CDI support feature](https://github.com/hantsy/ee7-sandbox/wiki/jpa-cdi) before. But unfortunately it does not work in our case(Hibernate and Wildfly).

*The JWT token based authentication and authorization is heavily inspired by [Best practice for REST token-based authentication with JAX-RS and Jersey](http://stackoverflow.com/questions/26777083/best-practice-for-rest-token-based-authentication-with-jax-rs-and-jersey/26778123?noredirect=1#comment72345930_26778123) on stackoverflow, thank [Cássio Mazzochi Molin](http://stackoverflow.com/users/1426227/c%c3%a1ssio-mazzochi-molin) for providing detailed steps to implement this solution.*

Get source codes from my Github account, and play it yourself.

https://github.com/hantsy/angularjs-ee7-sample

There is a **cdi** folder to demonstrate the JWT token based security.