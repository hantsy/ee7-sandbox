# JPA 2.1: Unsynchronized Persistence Contexts


In JPA 2.0 and the early version, any change of the data will be synchronized into database at the transaction is committed.

JPA 2.1 introduced a **synchronization** attribute in `@PersistenceContext` annotation when it's value is *SynchronizationType.UNSYNCHRONIZED*, you have to call **joinTransaction** to synchronize the data into database manually.


<pre>
@PersistenceContext(synchronization = SynchronizationType.UNSYNCHRONIZED)
EntityManager em;
</pre>

Compare the two methods. 

<pre>
public void save() {
	final Comment comment = new Comment(this.commentBody);
	comment.setPost(this.post);
	this.post.getComments().add(comment);
	em.merge(this.post);

	this.commentBody="";
}

public void saveWithJoinTransaction() {
	final Comment comment = new Comment(this.commentBody);
	comment.setPost(this.post);
	this.post.getComments().add(comment);

	em.joinTransaction();
	em.merge(this.post);

	this.commentBody="";
}
</pre>

Conceptually, this feature is a little similar with Hibernate's `FlushMode`. If you have used Seam 2, you could be impressed of the Hibernate FlushMode feature. In Seam 2, it allow you flush data manually in some case. For example, perform a series of steps in a flow, the data change will be cached and be committed in the last step, and allow you give up any change if exit the flow.

The sample codes are hosted on my github.com account, check out and play it yourself.

[https://github.com/hantsy/ee7-sandbox](https://github.com/hantsy/ee7-sandbox)