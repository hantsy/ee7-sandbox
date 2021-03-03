# Building RESTful APIs

Jaxrs, part of Java EE 7, is the standard specification for building RESTful API. 

In Jaxrs 2.0, a new Client API was introduced which it is easy to consume the third party RESTful APIs, also provides an approach to test RESTful API.

## Overview

The word **REST** was firstly introduced in [the dissertation from Thomas Fielding](https://www.ics.uci.edu/~fielding/pubs/dissertation/top.htm).

In this sample, the purpose is demostrating how to use Jaxrs to implements REST APIs. 

The following features are expected to be implemented.

1. create new post.
2. update existing post.
3. query posts by inputing keyword.


According to the convention of REST and HTTP protocol specification, the post APIs can be designed as the following table.

|Uri|Http Method|Request|Response|Description|
|---|---|---|---|---|
|/posts|GET||200, [{'id':1, 'title'},{}]|Get all posts|
|/posts|POST|{'title':'test title','content':'test content'}|201|Create a new post|
|/posts/{id}|GET||200, {'id':1, 'title'}|Get a post by id|
|/posts/{id}|PUT|{'title':'test title','content':'test content'}|204|Update a post|
|/posts/{id}|DELETE||204|Delete a post|

Follow the Domain Driven Design and Java EE specifications, we will build the core domain objects via JPA Entity, and a Stateless EJB bean as Repository, a plain CDI bean acts as Application Service, and some POJOs implements Value Object.

Next, we begin the project at creating the domain object.

## Create domain objects

I do not want to make things complex, in this sample, only one domain object will be created. It is the `Post` entity. In the DDD concept, entity is a persistent object with an identity. In Java EE world, JPA entity satify the requirement.


	@Entity
	@Table(name = "posts")
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public class Post implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		@Column(name="id")
		private Long id;
		
		@Version
		@Column(name="version")
		private Long version;
		
		@Column(name="title")
		@NotBlank
		private String title;
		
		@Column(name="content")
		@NotBlank
		private String content;

	}

If you have used JPA before, all of these fields in `Post` are easy to understand.

An JPA entity class must be annotated with `@Entity` annotation, and implements `Serializable` interface, and has a none argument constructor, and an identity field or property annotated with `@Id` annotation.

`@Table(name = "posts")` specifies the table will be mapped to. And `@Column` is the column name in the table mapped to the annotated fields.

`@NotBlank` is from Hibernate Validator, which will add some constraints to data when saving data into database.

`Data`, `Builder`, `NoArgsConstructor`, and `AllArgsConstructor` annotatotions are from [Lombok project](http://lombokproject.org), which will generate extra codes for the hosted class via Java complier annotation processor(a feature introduced in Java 5 in 2004) when compile the codes. 

`Data` will generate getters, setters, equals and hashCode, toString for `Post`. `Builder` will generate a `Builder` class in the 
`Post` to build Post object in fluent API. `NoArgsConstructor` will generate a none argument constructor for `Post`, `AllArgsConstructor` will generate a constructor includs all fields of `Post` as arguments. 

Include lombok in the project classpath, edit the *pom.xml* and add dependency.

	<dependency>
		<groupId>org.projectlombok</groupId>
		<artifactId>lombok</artifactId>
		<version>1.16.8</version>
	</dependency>

**NOTE**: If you are using NetBeans IDE, till now the lombok is supported seamless. For the users of JBoss Developer Studio(and other Eclipse based IDEs) and Intellij IDEA, there are extra steps required, please see [Lombok project](http://lombokproject.org) to add Lombok support to your IDEs.


## Create Repository

Create a repository for Post, named `PostRepository` and use the a RDBMS database for the repository storage and provide features described in the first section:

1. retrieve data from database.
2. save Post entity into database.
3. update Post in database.
4. delete post from database.

The following is the content of `PostRepository`.

	@Stateless
	public class PostRepository {

		@PersistenceContext
		private EntityManager em;

		public List<Post> findByKeyword(String keyword) {
			CriteriaBuilder cb = this.em.getCriteriaBuilder();

			CriteriaQuery<Post> q = cb.createQuery(Post.class);
			Root<Post> c = q.from(Post.class);

			List<Predicate> predicates = new ArrayList<>();

			if (StringUtils.isNotBlank(keyword)) {
				predicates.add(
					cb.or(
						cb.like(c.get(Post_.title), '%' + keyword + '%'),
						cb.like(c.get(Post_.content), '%' + keyword + '%')
					)
				);
			}

			q.where(predicates.toArray(new Predicate[predicates.size()]));

			TypedQuery<Post> query = em.createQuery(q);

			return query.getResultList();
		}
		
		public Post findById(Long id) {
			return em.find(Post.class, id);
		}

		public Post save(Post entity) {
			if (entity.getId() == null) {
				em.persist(entity);

				return entity;
			} else {
				return em.merge(entity);
			}
		}
		
		public void delete(Post entity) {
			Post _post = em.merge(entity);
			em.remove(_post);
		}

	}

The code is simple and stupid. Yeah, it is just a simple stateless EJB bean.

A`@Stateless` annotated on the `PostRepository` is to get JTA transaction support automaticially. 

You maybe notice there is a `Post_`  used in the `findByKeyword` method, which is the JPA entity metadata and use for typesafe criteria query, it is a feature introduced in JPA 2.0. In this project it is generated by Hibernate metadata generator. 

Open `pom.xml`, search **hibernate-jpamodelgen** and you can find it.

	<dependency>
		<groupId>org.hibernate</groupId>
		<artifactId>hibernate-jpamodelgen</artifactId>
		<scope>provided</scope>
	</dependency>

Be careful about the annotation processing, Lombok modifies the targer class directly, and Hibernate JPA metadata generator generates new class `Post_` in the  **target/generated-sources/annotations** folder. 

Configure maven compiler plugin to specify the annotation processors will be used.

	<plugin>
		<groupId>org.apache.maven.plugins</groupId>
		<artifactId>maven-compiler-plugin</artifactId>
		<version>3.5.1</version>
		<configuration>
			<compilerArgument>-Xlint</compilerArgument>
			<annotationProcessors>
				<annotationProcessor>lombok.launch.AnnotationProcessorHider$AnnotationProcessor</annotationProcessor>
				<annotationProcessor>org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor</annotationProcessor>
			</annotationProcessors>
		</configuration>
	</plugin>

**NOTE**: NetBeans users do not need any extra steps for annotation processor support. Unfortunately, there will be some errors displayed in **Errors View** of Eclipse based IDEs. 

If you are using JBoss Developer Studio, open project perference, select **Java compiler**/**Annotation processing**, enable Java annotation processor and choose the location of generated sources.

![compiler annotation processing config](https://github.com/hantsy/angularjs-ee7-sample/wiki/compiler-apt.png)

And find **Maven**/**Annotation processing**, enable annotation processing support for Maven.

![maven annotation processing config](https://github.com/hantsy/angularjs-ee7-sample/wiki/mvn-apt.png)

	
## Create Service

Create a service to hanlding incoming request data, shake hands with the PostRepository. 

It could be add more features in future, such as raise some events to notify external service.

	@ApplicationScoped
	public class BlogService {

		@Inject
		private PostRepository posts;

		public PostDetail findPostById(Long id) {
			Post post = fetchPostById(id);

			return DTOUtils.map(post, PostDetail.class);
		}

		public List<PostDetail> findByKeyword(String q) {
			List<Post> postlist=posts.findByKeyword(q);
			
			return DTOUtils.mapList(postlist, PostDetail.class);
		}

		public PostDetail createPost(PostForm form) {
			Post post = Post.builder()
					.title(form.getTitle())
					.content(form.getContent())
					.build();
			Post saved = posts.save(post);

			return DTOUtils.map(saved, PostDetail.class);
		}

		public PostDetail updatePost(Long id, PostForm form) {
			Post post = fetchPostById(id);

			post.setTitle(form.getTitle());
			post.setContent(form.getContent());

			Post saved = posts.save(post);

			return DTOUtils.map(saved, PostDetail.class);
		}

		public void deletePostById(Long id) {
			Post post = fetchPostById(id);
			posts.delete(post);
		}

		private Post fetchPostById(Long id) throws PostNotFoundException {
			Post post = posts.findById(id);
			if (post == null) {
				throw new PostNotFoundException("post:" + id + " was not found");
			}
			return post;
		}

	}

It is just a plain CDI bean which wraps the CRUD and make it more friendly for client calling.

`PostForm` and `PostDetail` are immutable object, it could be called DTO(Data Transfer Object) or Value Object, these objects are never persisted in repository, just represent some transient state transfered between different layers.

`PostForm` is responsible for gathering the input request data, in some case, even for the same resource, it has different representations, and the data validation rule could be different.

	@Data
	@Builder
	@NoArgsConstructor
	@RequiredArgsConstructor
	public class PostForm implements Serializable {

		private static final long serialVersionUID = 1L;

		@NotBlank
		@NonNull
		private String title;

		@NotBlank
		@NonNull
		private String content;

	}
	
`PostDetail` represents the query result, sometime it is could be flexible according to the query criteria and client principle role.	

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public class PostDetail implements Serializable {

		private static final long serialVersionUID = 1L;

		private Long id;
		
		private String title;
		
		private String content;
		
		private LocalDateTime createdAt;
		
		private LocalDateTime updatedAt;

	}
 
Another class `DTOUtils` is use for copying data between source and target claes, it supports deep copy. It is very flexible and extensible, you can write your rules for it. Go to [ModelMapper website](http://modelmapper.org) for more details.


## Create REST APIs

[Resteasy](http://www.resteasy.org) is the Jaxrs implementation in Wildfly application server. Most of case, I do not need the Resteasy specific APIs, and we are trying to use standard Jaxrs APIs as possible.

Firstly, activiate Jaxrs in the application.

	@ApplicationPath("api")
	public class JaxrsActiviator extends Application {

	}
	
All RESTful APIs are serving under prefix **/api**.	

Create RESTful APIs for Post, named `PostResource`.

	@RequestScoped
	@Path("posts")
	public class PostResource {

		@Inject
		private BlogService service;

		@Context
		UriInfo uriInfo;

		@GET()
		@Produces(value = MediaType.APPLICATION_JSON)
		public Response findAll(@QueryParam("q") String keyworkd) {
			return Response.ok(service.findByKeyword(keyworkd)).build();
		}

		@GET
		@Path("{id}")
		@Produces(value = MediaType.APPLICATION_JSON)
		public Response get(@PathParam("id") Long id) {
			return Response.ok(service.findPostById(id)).build();
		}

		@POST
		@Consumes(value = MediaType.APPLICATION_JSON)
		public Response save(@Valid PostForm post) {
			PostDetail saved = service.createPost(post);
			return Response.created(uriInfo.getBaseUriBuilder().path("posts/{id}").build(saved.getId())).build();
		}

		@PUT
		@Path("{id}")
		@Consumes(value = MediaType.APPLICATION_JSON)
		public Response update(@PathParam("id") Long id, @Valid PostForm post) {
			PostDetail saved = service.updatePost(id, post);
			return Response.noContent().build();
		}

		@DELETE
		@Path("{id}")
		public Response delete(@PathParam("id") Long id) {
			service.deletePostById(id);
			return Response.noContent().build();
		}

	}


## Summary

In this post, I created a simple Post APIs for CRUD operations, and used some of Java EE specs, such as JPA, EJB, CDI, JAXRS etc.


