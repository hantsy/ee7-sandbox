# Test Java EE application

One of the mainly reasons that Java developers love Spring is testing Spring applications are very easy. In a Spring based application, most of Spring beans can be tested like test a POJO without a container, sometime we call it **test out of container**.

But most of Java EE specs are tied with a container which cause testing Java EE components are diffcult, untill [JBoss Aruqillian](https://arquillian.org) is born and overcome this barrier, which make Java EE testing become easy in container.

In this post, I will try to use Arquillian to the Java EE components created in the early posts. Go to [JBoss Arquillian official website](http://arquillian.org) for more info about Arquillian and read the [Arquillian Guide](http://arquillian.org/guides) if you are new to Arquillian.

I also use JUnit to test the POJOs, and use Mockito to mock the dependencies and test service class.

## Test POJO

A simple POJO can be test by JUnit directly.

Write a simple JUnit test case for the `Post` and make sure the generated codes are working.

	public class PostTest {

		private static final String TITLE = "test_title";
		private static final String CONTENT = "test_content";

		@BeforeClass
		public static void beforeClass() {
		}

		@AfterClass
		public static void afterClass() {
		}

		@Before
		public void setup() {
		}

		@After
		public void teardown() {
		}

		@Test
		public void testPost() {
			Post post = Fixtures.newPost(TITLE, CONTENT);
			assertNull(post.getId());
			assertEquals(TITLE, post.getTitle());
			assertEquals(CONTENT, post.getContent());
		}

	}

In the `assertEquals` statement, `getTitle` and `getContent` are used, which are not existed in the `Post` source code, but via Lombok's assistance, they are available in the compiled `Post` class. It is magic. 

## Test EJB

`PostRepository` is a stateless EJB which is in-container features. Utilize Arquillian, we can test it in a running container.

Write a test for `PostRepository`, named `PostRepositoryTest`.

	@RunWith(Arquillian.class)
	public class PostRepositoryTest {

		@Deployment(name = "test")
		public static Archive<?> createDeployment() {
			JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
				.addClasses(Post.class, Post_.class)
				.addClasses(PostRepository.class)
				.addClasses(Fixtures.class, StringUtils.class)
				.addAsManifestResource("META-INF/test-persistence.xml", "persistence.xml")
				.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
			// System.out.println(archive.toString(true));
			return archive;
		}

		@Inject
		PostRepository posts;

		private static final String TITLE = "test_title";
		private static final String CONTENT = "test_content";

		Post _saved;

		@Before
		public void setUp() throws Exception {
			Post post = Fixtures.newPost(TITLE, CONTENT);
			_saved = posts.save(post);
		}

		@After
		public void tearDown() throws Exception {
			posts.delete(_saved);
		}

		@Test
		public void testFindByKeyword() {
			List<Post> foundPosts = posts.findByKeyword("test");
			assertEquals(1, foundPosts.size());
		}

		@Test
		public void testFindByKeywordNotFound() {
			List<Post> foundPosts = posts.findByKeyword("test123");
			assertEquals(0, foundPosts.size());
		}

		@Test
		public void testFindById() {
			Post found = posts.findById(_saved.getId());
			assertNotNull(found);
			assertEquals(TITLE, found.getTitle());
			assertEquals(CONTENT, found.getContent());
			assertNotNull(found.getId());
			assertNotNull(found.getVersion());
		}

		@Test
		public void testCRUD() {
			Post post = Fixtures.newPost(TITLE + "1", CONTENT + "1");
			Post saved = posts.save(post);

			assertEquals(TITLE + "1", saved.getTitle());
			assertEquals(CONTENT + "1", saved.getContent());
			assertNotNull(saved.getId());
			assertNotNull(saved.getVersion());

			saved.setTitle(TITLE + "updated");
			saved.setContent(CONTENT + "updated");

			Post updated = posts.save(saved);

			assertEquals(TITLE + "updated", updated.getTitle());
			assertEquals(CONTENT + "updated", updated.getContent());

			Long id = updated.getId();
			assertNotNull(id);

			Post found = posts.findById(id);
			assertEquals(TITLE + "updated", found.getTitle());
			assertEquals(CONTENT + "updated", found.getContent());

			posts.delete(found);

			assertNull(posts.findById(id));
		}
	}	

This is a Arquillian based test which requires to be run in a Wildfly container.	

Compare to the general JUnit test, there are some specific items.

1. `RunWith(Arquillian.class)` annotation indicates is a Arquillian test and run with an Arquillian specific test runner.
2. Every Arquillian test class requires a `@Deployment`, which includes the files will be packaged and deployed into the container for all tests.

And you have to add the dependencies to test scope.

	<dependency>
		<groupId>org.jboss.arquillian.junit</groupId>
		<artifactId>arquillian-junit-container</artifactId>
		<scope>test</scope>
	</dependency>

	<dependency>
		<groupId>org.jboss.arquillian.protocol</groupId>
		<artifactId>arquillian-protocol-servlet</artifactId>
		<scope>test</scope>
	</dependency>
	
The first is the basic dependency when use JUnit, you can also use TestNG with Arquillian. The second is the protocol used when Aruqillian test runner communicates with containers. For Wildfly, there are two available, jmx and servlet.


	<dependency>
		<groupId>org.jboss.shrinkwrap.resolver</groupId>
		<artifactId>shrinkwrap-resolver-depchain</artifactId>
		<scope>test</scope>
		<type>pom</type>
	</dependency>
	
**shrinkwrap** provides fluent APIs for packaging deployment archives.	

Run this the test with `arq-wildfly-remote` and ``arq-wildfly-managed` profiles.

	mvn clean test -Dtest=PostRepositoryTest -Parq-wildfly-managed`
	
In JBoss Developer Studio, select **Maven**/**select Maven profiles** from project context menu.

![Select Maven Profiles](https://github.com/hantsy/angularjs-ee7-sample/wiki/mvn-select-profiles.png)

Select **Run as** in the test file context menu, and select **Arquillian JUnit Test**.

![Select Maven Profiles](https://github.com/hantsy/angularjs-ee7-sample/wiki/run-as-arquillian.png)

In NetBeans select config in the toolbar to switch to different Maven profiles directly.
	
When the test is running, it will perform the following steps.

1. Download a copy of Wildfly server and extract the files and start Wildfly.
2. Packaged the files specified in the `@Deployment` archive.
3. Deploy the archive into the running container.
4. Execute the test.
5. Arquillian will gather the test result and take back to JUnit runner.
6. Display the test result in JUnit view.

As you see, JBoss Arquillian is very attractive when test Java EE components.

1. The test will be run in a real world container.
2. You can use `@Inject` etc features in test codes directly like in general CDI, EJB beans.
3. You do not need to package all files to test, just select neccessary files to test somewhat.
	

	
## Isolate the dependencies via Mockito

`BlogService` is a CDI bean which depends on `BlogRepository`. Currently `BlogService` does not depend on others, with the help of Mockito, we can isolate and mock the dependencies and test the flow in the `BlogService`.

Write a test for `BlogService`.

	public class BlogServiceMockTest {

		@Mock
		private PostRepository posts;

		@InjectMocks
		private BlogService service;

		@Before
		public void setup() {
			initMocks(this);
		}

		@After
		public void teardown() {
			reset(posts);
		}

		private static final String TITLE = "test_title";
		private static final String CONTENT = "test_content";

		@Test
		public void testGetPostById() {
			Post returned = Fixtures.newPost(TITLE, CONTENT);
			returned.setId(1L);

			when(posts.findById(1L))
				.thenReturn(returned);

			PostDetail detail = service.findPostById(1L);

			assertNotNull(detail.getId());
			assertEquals(TITLE, detail.getTitle());
			assertEquals(CONTENT, detail.getContent());

			verify(posts, times(1)).findById(anyLong());
		}

		@Test
		public void testGetPostByIdBDD() {

			Post returned = Fixtures.newPost(TITLE, CONTENT);
			returned.setId(1L);

			given(posts.findById(1L))
				.willReturn(returned);

			PostDetail detail = service.findPostById(1L);

			assertNotNull(detail.getId());
			assertEquals(TITLE, detail.getTitle());
			assertEquals(CONTENT, detail.getContent());

			verify(posts, times(1)).findById(anyLong());
		}

		@Test
		public void testGetAllPostsByKeyword() {

			Post returned = Fixtures.newPost(TITLE, CONTENT);
			returned.setId(1L);

			Post returned2 = Fixtures.newPost(TITLE + "2", CONTENT + "2");
			returned2.setId(2L);

			given(posts.findByKeyword("test"))
				.willReturn(Arrays.asList(returned, returned2));

			List<PostDetail> detailList = service.findByKeyword("test");

			assertEquals(2, detailList.size());
			verify(posts, times(1)).findByKeyword(anyString());
		}

		@Test
		public void testSavePost() {

			Post newPost = Fixtures.newPost(TITLE, CONTENT);

			Post returned = Fixtures.newPost(TITLE, CONTENT);
			returned.setId(1L);

			PostForm form = Fixtures.newPostForm(TITLE, CONTENT);

			given(posts.save(newPost))
				.willReturn(returned);

			PostDetail detail = service.createPost(form);

			assertNotNull(detail.getId());
			assertEquals(TITLE, detail.getTitle());
			assertEquals(CONTENT, detail.getContent());

			verify(posts, times(1)).save(any(Post.class));
		}

		@Test
		public void testUpdatePost() {

			Post originalPost = Fixtures.newPost(TITLE, CONTENT);
			originalPost.setId(1L);

			Post toUpdate = Fixtures.newPost(TITLE + "updated", CONTENT + "updated");
			toUpdate.setId(1L);

			Post returnedPost = Fixtures.newPost(TITLE + "updated", CONTENT + "updated");
			returnedPost.setId(1L);

			PostForm form = Fixtures.newPostForm(TITLE + "updated", CONTENT + "updated");

			given(posts.findById(1L))
				.willReturn(originalPost);
			given(posts.save(toUpdate))
				.willReturn(returnedPost);

			PostDetail detail = service.updatePost(1L, form);

			assertNotNull(detail.getId());
			assertEquals(TITLE + "updated", detail.getTitle());
			assertEquals(CONTENT + "updated", detail.getContent());

			verify(posts, times(1)).findById(anyLong());
			verify(posts, times(1)).save(any(Post.class));
		}

		@Test
		public void testDeletePost() {

			Post originalPost = Fixtures.newPost(TITLE, CONTENT);
			originalPost.setId(1L);

			Post toDelete = Fixtures.newPost(TITLE + "updated", CONTENT + "updated");
			toDelete.setId(1L);

			given(posts.findById(1L))
				.willReturn(originalPost);
			doNothing().when(posts).delete(toDelete);

			service.deletePostById(1L);

			verify(posts, times(1)).findById(anyLong());
			verify(posts, times(1)).delete(any(Post.class));
		}

	}
 
It is a plain JUnit test, but we use Mockito to mock the dependency(PostRepository) of `BlogService`.

Add Mockito dependency into *pom.xml* file.

	<dependency>
		<groupId>org.mockito</groupId>
		<artifactId>mockito-core</artifactId>
		<version>${version.mockito}</version>
		<scope>test</scope>
	</dependency>

Mockito provides a BDD varient, I have demonstrated it in the `testGetPostByIdBDD()`.

Please see the [Mockito website](http://mockito.org) for more details.

Mock test just test logic in the `BlogService`, but it does not promise it is working in a real container. You can also write another test like the PostRepository to verify it works in the Wildfly container.

## Test REST API

Jaxrs 2.0 provides client API, and Aruqillian provides run test in client mode.

Create a Arquillian test to verify the APIs in container. 

	@RunWith(Arquillian.class)
	public class PostResourceTest {

		private static final Logger LOG = Logger.getLogger(PostResourceTest.class.getName());

		@Deployment(testable = false)
		public static WebArchive createDeployment() {

			File[] extraJars = Maven.resolver().loadPomFromFile("pom.xml")
				.resolve(
					"org.projectlombok:lombok:1.16.8",
					"org.modelmapper:modelmapper:0.7.5",
					"org.apache.commons:commons-lang3:3.4",
					"com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.6.3"
				)
				.withTransitivity()
				.asFile();

			final WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war")
				.addAsLibraries(extraJars)
				.addClasses(DTOUtils.class, Fixtures.class)
				.addClasses(
					Post.class,
					Post_.class,
					PostRepository.class
				)
				//add service classes
				.addClasses(
					BlogService.class,
					PostNotFoundException.class,
					PostForm.class,
					PostDetail.class
				)
				//Add JAXRS resources classes
				.addClasses(
					JaxrsActiviator.class,
					PostResource.class,
					JacksonConfig.class
				//                    ResourceNotFoundExceptionMapper.class,
				//                    CustomBeanParamProvider.class,
				//                    JacksonConfig.class,
				//                    RequestResource.class
				)
				// .addAsResource("test-log4j.properties", "log4j.properties")
				//Add JPA persistence configration.
				//WARN: In a war package, persistence.xml should be put into /WEB-INF/classes/META-INF/, not /META-INF
				.addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
				// Enable CDI
				//WARN: In a war package, persistence.xml should be put into /WEB-INF not /META-INF
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
			//  .addAsWebInfResource("test-jboss-deployment-structure.xml", "jboss-deployment-structure.xml");

			LOG.log(Level.INFO, "war to string @{0}", war.toString());
			return war;
		}

		@ArquillianResource
		private URL base;

		private Client client;

		@Before
		public void setup() throws MalformedURLException {
			client = ClientBuilder.newClient();
			client.register(JacksonConfig.class);
		}

		@After
		public void teardown() throws MalformedURLException {
			client.close();
		}

		private static final String TITLE = "test_title";
		private static final String CONTENT = "test_content";

		@Test
		@RunAsClient
		public void testPosts() throws MalformedURLException {

			LOG.log(Level.INFO, "base url @{0}", base);

			//get all posts
			final WebTarget targetGetAll = client.target(URI.create(new URL(base, "api/posts").toExternalForm()));
			final Response resGetAll = targetGetAll.request().accept(MediaType.APPLICATION_JSON_TYPE).get();
			assertEquals(resGetAll.getStatus(), 200);
			PostDetail[] results = resGetAll.readEntity(PostDetail[].class);
			assertTrue(results != null);
			assertTrue(results.length == 0);

			//You have to close the response manually... issue# RESTEASY-1120
			//see https://issues.jboss.org/browse/RESTEASY-1120
			resGetAll.close();

			//create a new post
			PostForm newPostForm = Fixtures.newPostForm(TITLE, CONTENT);
			final WebTarget targetPost = client.target(URI.create(new URL(base, "api/posts").toExternalForm()));
			final Response resPost = targetPost.request().post(Entity.json(newPostForm));
			assertEquals(resPost.getStatus(), 201);
			String location = resPost.getHeaderString("Location");
			LOG.log(Level.INFO, "saved post location @{0}", location);

			resPost.close();

			//verify new created post in the findAll result list.
			final WebTarget targetGetAll2 = client.target(URI.create(new URL(base, "api/posts").toExternalForm()));
			final Response resGetAll2 = targetGetAll2.request().accept(MediaType.APPLICATION_JSON_TYPE).get();
			assertEquals(resGetAll2.getStatus(), 200);
			PostDetail[] results2 = resGetAll2.readEntity(PostDetail[].class);
			assertTrue(results2 != null);
			assertTrue(results2.length == 1);

			resGetAll2.close();

			//get the created data
			final WebTarget targetGet = client.target(URI.create(new URL(location).toExternalForm()));
			Response responseGet = targetGet.request().accept(MediaType.APPLICATION_JSON_TYPE).get();
			assertEquals(responseGet.getStatus(), 200);
			LOG.log(Level.INFO, "get entity @{0}", responseGet);
			PostDetail result = responseGet.readEntity(PostDetail.class);
			assertNotNull(result.getId());
			assertEquals(TITLE, result.getTitle());
			assertEquals(CONTENT, result.getContent());

			responseGet.close();

			//update post form
			PostForm updatePostForm = Fixtures.newPostForm(TITLE + "updated", CONTENT + "updated");
			final WebTarget targetPut = client.target(URI.create(new URL(location).toExternalForm()));

			final Response responsePut = targetPut
				.request()
				.put(Entity.json(updatePostForm));

			assertEquals(responsePut.getStatus(), 204);

			responsePut.close();

			//verify updated result
			final WebTarget targetVerifyUpdatedGet = client.target(URI.create(new URL(location).toExternalForm()));
			final Response responseVerifyUpdatedGet = targetVerifyUpdatedGet.request().accept(MediaType.APPLICATION_JSON_TYPE).get();

			assertEquals(responseVerifyUpdatedGet.getStatus(), 200);
			LOG.log(Level.INFO, "verifyUpdateGet entity @{0}", responseVerifyUpdatedGet);
			PostDetail verifyUpdateResult = responseVerifyUpdatedGet.readEntity(PostDetail.class);
			assertNotNull(verifyUpdateResult.getId());
			assertEquals(TITLE + "updated", verifyUpdateResult.getTitle());
			assertEquals(CONTENT + "updated", verifyUpdateResult.getContent());

			responseVerifyUpdatedGet.close();

			//delete post
			final WebTarget targetDelete = client.target(URI.create(new URL(location).toExternalForm()));
			final Response responseDelete = targetDelete
				.request()
				.delete();

			assertEquals(responseDelete.getStatus(), 204);

			responseDelete.close();
		}
	}

Open *pom.xml*, and add Resteasy client to test scope.

	<dependency>
		<groupId>org.jboss.resteasy</groupId>
		<artifactId>resteasy-client</artifactId>
		<scope>test</scope>
	</dependency>

	<dependency>
		<groupId>org.jboss.resteasy</groupId>
		<artifactId>resteasy-jackson2-provider</artifactId>
		<scope>test</scope>
	</dependency>

	<!-- Now we declare the dependencies needed by the jaxrs-client -->
	<dependency>
		<groupId>org.apache.httpcomponents</groupId>
		<artifactId>httpclient</artifactId>
		<scope>test</scope>
	</dependency>

	<dependency>
		<groupId>commons-io</groupId>
		<artifactId>commons-io</artifactId>
		<scope>test</scope>
	</dependency>	
	
Most of case, I only use JSON as the API exchange format, so add `resteasy-jackson2-provider`, and `httpclient` is used by Resteasy client.	
	
It is similar with the former version, but a little different.

1. `@Deployment(testable = false)` indicates it does not run tests in containers.
2. `@ArquillianResource` will inject the deployed Uri of the test package to a `Uri` after the archived is deployed successfully.
3. `@RunAsClient` tell test will be run as a Jaxrs client consumer, just like you invoke the remote REST APIs which served by the test deployment archive in the container.

## Summary

In this post, I created a simple Post APIs for CRUD operations, and also introduced the test skills.

* Use JUnit to test POJOs.
* Use Mockito to isolate the dependency with mock and test business logic.
* Use Arquillian to test features in a real container.



