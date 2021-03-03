#Authentication and Authorization

Before your REST API are published to public, you have to protect it and control the access permissions. Java EE has built-in JAAS specification to process authentication and authorization and archive this purpose. 

But JAAS is very dependent on containers, most of containers such as Wildfly, Glassfish, Weblogic provides friendly UI to administrate users and roles/groups.

There are several authentication strategies provided in JAAS, such as BASIC, FORM, X509 etc.

BASIC satisfies the stateless requirements of REST API, it is usually used in REST API development stage.

Assume there are two roles will consume these APIs.

* ROLE_USER
* ROLE_ADMIN

1. Declare the resource restrictions in *WEB-INF/web.xml*.

	```
	<security-constraint>
        <display-name>REST API Resources</display-name>
        <web-resource-collection>
            <web-resource-name>Protected API</web-resource-name>
            <url-pattern>/api/*</url-pattern>
        </web-resource-collection>    
        <auth-constraint>
            <role-name>ROLE_USER</role-name>
            <role-name>ROLE_ADMIN</role-name>
        </auth-constraint>
    </security-constraint>
    <login-config>
        <auth-method>BASIC</auth-method>
        <!--         <realm-name>RealmUsersRoles</realm-name>-->
        <realm-name>ApplicationRealm</realm-name>
    </login-config>
    <security-role>
        <role-name>ROLE_USER</role-name>
    </security-role>
    <security-role>
        <role-name>ROLE_ADMIN</role-name>
    </security-role>
	```
	
	We declare two roles as expected. And url pattern */api/\** are protected. You can define it more fine-grained. 
	
	**auth-method** specifies the authentication method is **BASIC**.
	
2. Add users to Wildfly container.

	Make sure the wildfly is running. 
	
	Open terminal, and enter the *wildfly/bin* folder, execute `add-user.bat` command.

	```
	#add-user.bat

	What type of user do you wish to add?
	 a) Management User (mgmt-users.properties)
	 b) Application User (application-users.properties)
	(a): b

	Enter the details of the new user to add.
	Using realm 'ApplicationRealm' as discovered from the existing property files.
	Username : testuser
	Password recommendations are listed below. To modify these restrictions edit the add-user.properties configuration file.
	 - The password should be different from the username
	 - The password should not be one of the following restricted values {root, admin, administrator}
	 - The password should contain at least 8 characters, 1 alphabetic character(s), 1 digit(s), 1 non-alphanumeric symbol(s)
	Password :
	WFLYDM0102: Password should have at least 1 non-alphanumeric symbol.
	Are you sure you want to use the password entered yes/no? yes
	Re-enter Password :
	What groups do you want this user to belong to? (Please enter a comma separated list, or leave blank for none)[  ]: ROLE_USER
	About to add user 'testuser' for realm 'ApplicationRealm'
	Is this correct yes/no? yes
	Added user 'testuser' to file 'D:\appsvr\wildfly-10.0.0.Final\standalone\configuration\application-users.properties'
	Added user 'testuser' to file 'D:\appsvr\wildfly-10.0.0.Final\domain\configuration\application-users.properties'
	Added user 'testuser' with groups ROLE_USER to file 'D:\appsvr\wildfly-10.0.0.Final\standalone\configuration\application-roles.properties'
	Added user 'testuser' with groups ROLE_USER to file 'D:\appsvr\wildfly-10.0.0.Final\domain\configuration\application-roles.properties'
	Is this new user going to be used for one AS process to connect to another AS process?
	e.g. for a slave host controller connecting to the master or for a Remoting connection for server to server EJB calls.
	yes/no? no
	Press any key to continue . . .
	```
	
	**NOTE**: Createa an application user for test. The resule will be written into *application-users.properties* and *application-roles.properties* under *wildfly/standalone/configuration* and *wildfly/domain/configuration*.
	
	The content of `application-users.properties`.
	
	```
	testuser=c4ae795ffa8c6342017e5a49d1ba03d6
	```
	
	The key is the username of the user we just created, the password is HEX(MD5(password)).
	
	The content of `application-roles.properties`.
	
	```
	testuser=ROLE_USER
	```
	
	The key is the username, and value is the roles belongs to. 

3. Deploy project into the running Wildfly.

	* Run `mvn clean wildfly:deploy` in the project folder to deploy the application by maven wildfly plugin automaticially.
	* Or run `mvn clean package` to build the project and package it as a war in the *target* folde. Then copy this war to *wildfly/standalone* folder manually.
	
4. Open your favorite browser, navigate to *http://localhost:8080/blog-api/api*. You will see prompt shown in brower window which requires username and password to access the typed url.

	![auth-basic](https://github.com/hantsy/angularjs-ee7-sample/wiki/auth-basic.png)


Once users are authenticated, the user principal can be fected from SecurityConext in JAXRS resource.

		@Context SecurityConext sc;
		
		//sc.getUserPrincipal
	
The security config will be stranfered into EJB context. So you can use JAAS annotations on EJB beans.

		@RolesAllowed("ROLE_USER")
		@Stateless
		public class EJBSample{
		
			@RolesAllowed("ROLE_USER")
			public void simpleMethod(){...}
		}
		
Write a simple test to verify if the authentication works.

```java
@RunWith(Arquillian.class)
public class BasicAuthTest {

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
            //domain.support package.
            .addPackage(AbstractEntity.class.getPackage())
            //domain.convert package.
            .addPackage(LocalDateConverter.class.getPackage())
            .addClasses(
                Post.class,
                Post_.class,
                PostRepository.class,
                Comment.class,
                Comment_.class,
                CommentRepository.class
            )
            //add service classes
            .addClasses(BlogService.class,
                ResourceNotFoundException.class,
                PostForm.class,
                PostDetail.class,
                CommentForm.class,
                CommentDetail.class
            )
            //Add JAXRS resources classes
            .addClasses(
                JaxrsActiviator.class,
                PostResource.class,
                CommentResource.class,
                JacksonConfig.class,
                ResourceNotFoundExceptionMapper.class,
                ValidationExceptionMapper.class,
                ValidationError.class,
                CustomBeanParamProvider.class//,
               // BasicAuthentication.class
            )
            // .addAsResource("test-log4j.properties", "log4j.properties")
            //Add JPA persistence configration.
            //WARN: In a war package, persistence.xml should be put into /WEB-INF/classes/META-INF/, not /META-INF
            .addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
            // Enable CDI
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            
 //           .addAsWebInfResource("jboss-web.xml", "jboss-web.xml")
            
            .addAsWebInfResource("test-web.xml", "web.xml");
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
        client.register(ResourceNotFoundExceptionMapper.class);
        client.register(ValidationExceptionMapper.class);
    }

    @After
    public void teardown() throws MalformedURLException {
        client.close();
    }

    private static final String TITLE = "test_title";
    private static final String CONTENT = "test_content";

    @Test
    @RunAsClient
    public void getPostsWithWrongPasswordShouldReturn401() throws MalformedURLException {

        LOG.log(Level.INFO, "base url @{0}", base);

        client.register(new BasicAuthentication("testuser", "testuser1234"));

        //get all posts
        final WebTarget targetGetAll = client.target(URI.create(new URL(base, "api/posts").toExternalForm()));
        final Response resGetAll = targetGetAll.request().accept(MediaType.APPLICATION_JSON_TYPE).get();
        assertEquals(401, resGetAll.getStatus());
    }

    @Test
    @RunAsClient
    public void getPostsWithCorrectPasswordShouldReturn200() throws MalformedURLException {

        client.register(new BasicAuthentication("testuser", "testuser123"));

        //get all posts
        final WebTarget targetGetAll = client.target(URI.create(new URL(base, "api/posts").toExternalForm()));
        final Response resGetAll = targetGetAll.request().accept(MediaType.APPLICATION_JSON_TYPE).get();
        assertEquals(200, resGetAll.getStatus());
    }

}
``` 		

In the test `getPostsWithCorrectPasswordShouldReturn200()`, register a `BasicAuthentication` to build HTTP Basic authentication and add to HTTP request header.
	
*HTTP BASIC* is a very simple authentication, it just adds an authorization header to HTTP request. The secure part is consist of a "username:password" pair base64 encoded string.

	Authorization:Basic base64(username:password)
	
In the above steps, the user info are stored in properties files, they also can be stored in RDBMS etc, thus in our application a client can register a user. I have described the steps in [my another sample](https://github.com/hantsy/signup-app). You can also refer [this blog entry](http://blog.eisele.net/2015/01/jdbc-realm-wildfly820-primefaces51.html).

Get source codes from my Github account, and try it yourself.

https://github.com/hantsy/angularjs-ee7-sample













