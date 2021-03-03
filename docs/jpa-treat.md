# JPA 2.1: Treat


JPA 2.1 introduces a new keyword **treat** in JPQL which allow path expressions to be treated as a subclass, giving access to subclass specific state. 

Create two specified `Comment`s,  `VoteUp` and `VoteDown`. 

<pre>
@Entity
public class VoteUp  extends Comment{

    public VoteUp() {
        super("Up");
    }
    
}
</pre>

<pre>
@Entity
public class VoteDown  extends Comment{

    public VoteDown() {
        super("Down");
    }
    
}
</pre>

Add an `@Inheritance` annotation on `Comment` entity.

<pre>
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Comment implements Serializable {
}
</pre>

In the `ViewPostBean`, fetch the counts of *voteUp* comments and *voteDown* comments.

<pre>
private void fetchVoteDown() {
	this.voteDown = ((Long) (em.createQuery("select count(vu)from Post p join treat(p.comments  as VoteDown) vu where p.id=:id")
		.setParameter("id", this.id)
		.getSingleResult())).intValue();
}

private void fetchVoteUp() {
	this.voteUp = ((Long) (em.createQuery("select count(vu)from Post p join treat(p.comments  as VoteUp) vu where p.id=:id")
		.setParameter("id", this.id)
		.getSingleResult())).intValue();
}
</pre>

Also create a *voteUp()* and *voteDown()* methods to create `VoteUp` and `VoteDown` objects.

<pre>
public void voteUp() {
	final VoteUp comment = new VoteUp();
	comment.setPost(this.post);
	this.post.getComments().add(comment);

	em.merge(this.post);
	em.flush();

	fetchVoteUp();
}

public void voteDown() {
	final VoteDown comment = new VoteDown();
	comment.setPost(this.post);
	this.post.getComments().add(comment);

	em.merge(this.post);
	em.flush();

	fetchVoteDown();
}
</pre>


The sample codes are hosted on my github.com account, check out and play it yourself.

[https://github.com/hantsy/ee7-sandbox](https://github.com/hantsy/ee7-sandbox)