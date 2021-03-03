# JPA 2.1: Entity Graph

Entity Graph is a means to specify the structure of a graph of entities, it defines the return path and boundaries when the entity is loaded.

## Define Entity Graph

You can define an Entity graph via `@NamedEntityGraph` annotation.

<pre>
@NamedEntityGraph(
        name = "post",
        attributeNodes = {
            @NamedAttributeNode("title"),
            @NamedAttributeNode(value = "comments", subgraph = "comments")
        },
        subgraphs = {
            @NamedSubgraph(
                    name = "comments",
                    attributeNodes = {
                        @NamedAttributeNode("content")}
            )
        }
)
</pre>

or create a EntityGraph dynamically via `createEntitGraph` method of  `EntityManager`.

<pre>
EntityGraph postEntityGraph=em.createEntityGraph(Post.class);
postEntityGraph.addAttributeNodes("title");
postEntityGraph.addSubgraph("comments").addAttributeNodes("content");
</pre>

## Apply the Entity Graph

Get the EntityGraph.

<pre>
EntityGraph postGraph=em.getEntityGraph("post");
</pre>

Set the value of `javax.persistence.fetchgraph`

<pre>
em.createQuery("select p from Post p where p.id=:id", Post.class)
  .setHint("javax.persistence.fetchgraph", postGraph)
  .setParameter("id", this.id)
  .getResultList()
  .get(0);
</pre>

There are two hints available in JPA 2.1 for configuring the Entity Graph loading strategy.

**javax.persistence.fetchgraph** will load all attributes defined in the EntityGraph, and all unlisted attributes will use LAZY to load.

**javax.persistence.loadgraph** will load all attributes defined in the EntityGraph, and all unlisted attributes will apply it's fetch settings.

## What problem it resolved

If you are using Hibernate, you have to encounter the famous *LazyInitializedExcpetion* when you try to fetch values from association attributes of an entity outside of an active Session.

There are some existed solutions to resolve this problem. In a web application, most of case, this exception could be thrown in the view layer, one solution is introduce Open Session in View pattern, in Spring application, an OpenInView AOP Interceptor(for none web application) and an OpenInView web Filter(for web application) are provided.

Now, the Entity Graph could be another robust solution for resolving this issue. For those cases which require to hint the lazy association of an Entity, create a Entity Graph to load them when the query is executed.

Some JPA providers, such as [OpenJPA](http://openjpa.apache.org), provide a *Fetch Plan* feature, which is similar with the Entity Graph.

## Summary

The Entity Graph overrides the default loading strategy, and provides flexibility of loading the association attributes of an Entity.

The sample codes are hosted on my github.com account, check out and play it yourself.

[https://github.com/hantsy/ee7-sandbox](https://github.com/hantsy/ee7-sandbox)