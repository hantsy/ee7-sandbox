# JPA 2.1: Programmatic Named Queries

In JPA 2.0 or the early version, you can define a named query by add a `@NamedQuery` annotation on the entity class.

JPA 2.1 brings a new programmatic approach to create a named query dynamically.


<pre>
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
</pre>

In the above the codes, use a `@Singleton` EJB to create the named queries at EJB `@Startup` stage.

Replace the query string in the voteUp method with the following content.

<pre>
    private void fetchVoteDown() {

        this.voteDown = ((Long) (em.createNamedQuery(Constants.NQ_COUNT_VOTE_UP)
                .setParameter("id", this.id)
                .getSingleResult())).intValue();
    }
</pre>

The sample codes are hosted on my github.com account, check out and play it yourself.

[https://github.com/hantsy/ee7-sandbox](https://github.com/hantsy/ee7-sandbox)
