# Authentication and Authorization:JWT and JAAS

HTTP Basic is simple and stupid, but it is considered as a unsecure solution even you are using HTTPS protocol. A base64 encoded string is too easy to be decoded by hackers.

In these days, an popular alternative is using [JWT][], which uses a signed token replaces the simple base64 encoded string. JWT based token is generated by server side and usually uses a security salt or key store to improve security.

1. Client send credentials and try to get token from server.
2. Put the token value into HTTP header and access the protected resources.
3. If the token is expired, he can refresh it and get a new one.

Explore the [JWT][] website, you will find lots of Java implementations. **jjwt** is one of the most popular libs, which is maintained by [Auth0][].

Add **jjwt** into project dependencies.

```xml
<dependency>
	<groupId>io.jsonwebtoken</groupId>
	<artifactId>jjwt</artifactId>
	<version>0.6.0</version>
</dependency>
```

Create a simple helper class to parse and generate JWT token.

```java
@ApplicationScoped
public class JwtHelper {

    private final String secret = "test123";

    private final long expirationInSeconds = 3600 * 24;//one day

    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + this.expirationInSeconds * 1000);
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", user.getUsername());
        claims.put("role", user.getRole().name());

        return generateTokenFromClaims(claims);
    }

    public UserPrincipal parseToken(String token) throws Exception {
        Claims claims = extractClaimsFromToken(token);
        if (claimsIsExpired(claims)) {
            throw new RuntimeException("token is expired");
        }
        return new JwtUser((String) claims.get("sub"), Arrays.asList(Role.valueOf((String) claims.get("role"))));
    }

    private boolean claimsIsExpired(Claims claims) throws Exception {
        boolean isExpired = claims.getExpiration().before(new Date());
        return isExpired;
    }

    public String refreshToken(String token) throws Exception {
        Claims claims = extractClaimsFromToken(token);

        if (claimsIsExpired(claims)) {
            return generateTokenFromClaims(claims);
        }
        return token;
    }

    private String generateTokenFromClaims(Map<String, Object> claims) {
        return Jwts.builder()
            .setClaims(claims)
            .setExpiration(generateExpirationDate())
            .signWith(SignatureAlgorithm.HS512, this.secret)
            .compact();
    }

    private Claims extractClaimsFromToken(String token) throws Exception {
        Claims claims;
        try {
            claims = Jwts.parser()
                .setSigningKey(this.secret)
                .parseClaimsJws(token)
                .getBody();
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            throw new RuntimeException("token:" + token + " is invalid");
        }
        return claims;
    }

}
```

**jjwt** provides a `Jwt` class which includes fluent APIs to create and parse JWT token.

We add `username` and `role` properties of a user as part of claims to build the token. In this sample, we use **HS512** algorithm and a custom secret to generate it.

Next, create REST APIs to generate token, generally it is a login operation.

```java
@RequestScoped
@Path("auth")
public class AuthResource {

    private static final Logger LOG = Logger.getLogger(AuthResource.class.getName());

    @Inject
    private UserService service;

    @Context
    UriInfo uriInfo;

    @POST
    @Path("login")
    @Consumes(value = MediaType.APPLICATION_JSON)
    public Response login(@Valid Credentials form) {
        LOG.log(Level.INFO, "login as@{0}", form);
        return Response.ok(service.authenticate(form)).build();
    }

    @POST
    @Path("signup")
    @Consumes(value = MediaType.APPLICATION_JSON)
    public Response signup(@Valid SignupForm form) {
        service.registerUser(form);
        return Response.ok().build();
    }

    @POST
    @Path("logout")
    public Response logout() {
        LOG.info("logging out...");
        return Response.ok().build();
    }

}
```

`AuthResource` includes a `login`, `signup` and `logout` methods. Currently logout does nothing, and `signup` is use for user registration, and `login` accept a username and password paired bean as paramter and return the token value if user is authenticated successfully.

When a user logged in successfully, and got the token value, set "Bearer " + token(do not forget the whitespce after "Bearer") in the HTTP header **Authorization**.

```
Authorization:Bearer  +(token)
```

When you access the protected resources with token, the access operation should be granted. You can use a Servlet Filter to verify the token, but Jaxrs has it own `ContainerRquestFilter` which is tight with Jaxrs, which could be better than implementing a generic servlet Filter.

```java
@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    @Inject
    JwtHelper jwtHelper;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        SecurityContext originalContext = requestContext.getSecurityContext();

        String token = extractTokenFromHeader(requestContext);
        try {
            // Validate the token
            UserPrincipal claims = jwtHelper.parseToken(token);
            requestContext.setSecurityContext(new Authorizer(claims, originalContext.isSecure()));

        } catch (Exception e) {
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }
    
    private String extractTokenFromHeader(ContainerRequestContext requestContext) throws NotAuthorizedException {
        // Get the HTTP Authorization header from the request
        String authorizationHeader
            = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        // Check if the HTTP Authorization header is present and formatted correctly
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new NotAuthorizedException("Authorization header must be provided");
        }
        // Extract the token from the HTTP Authorization header
        String token = authorizationHeader.substring("Bearer".length()).trim();
        return token;
    }

    static class Authorizer implements SecurityContext {

        UserPrincipal principal;
        boolean isSecure;

        public Authorizer(UserPrincipal principal, boolean isSecure) {
            this.principal = principal;
            this.isSecure = isSecure;
        }

        @Override
        public Principal getUserPrincipal() {
            return this.principal;
        }

        @Override
        public boolean isUserInRole(String role) {
            return this.principal.getRoles().contains(Role.valueOf(role));
        }

        @Override
        public boolean isSecure() {
            return this.isSecure;
        }

        @Override
        public String getAuthenticationScheme() {
            return "JWT";
        }
    }
}
```

`AuthenticationFilter` implements `ContainerRequestFilter` which has `filter` method declaration.

```java
public void filter(ContainerRequestContext requestContext) throws IOException {}
```

It is very similar with Servlet Filter.

`@Provider` annotation make Jaxrs can recognize it automaticially at runtime.

`@Priority(Priorities.AUTHENTICATION)` defines its order when interceptes HTTP request.

Have a look at `Priorities` class. It defines several value for Jaxrs. **AUTHENTICATION** is the highest priority.

```java
public final class Priorities {

    private Priorities() {
        // prevents construction
    }

    /**
     * Security authentication filter/interceptor priority.
     */
    public static final int AUTHENTICATION = 1000;
    /**
     * Security authorization filter/interceptor priority.
     */
    public static final int AUTHORIZATION = 2000;
    /**
     * Header decorator filter/interceptor priority.
     */
    public static final int HEADER_DECORATOR = 3000;
    /**
     * Message encoder or decoder filter/interceptor priority.
     */
    public static final int ENTITY_CODER = 4000;
    /**
     * User-level filter/interceptor priority.
     */
    public static final int USER = 5000;
}
```
In the `filter` method of `AuthenticationFilter`, we use `JwtHelper` parse the token in HTTP request header, and extract the *username* and *role* info from it. Wrap these into a `JwtUser`.

We also setup a custom `SecurityContext` with our authenticated user. Thus we can get the authenticated user principal from `SecurityContext` in Jaxrs.

```java
@Context
SecurityContext securityContext;

//...
Principal principal = securityContext.getUserPrincipal();
```	

If there is an exception occured, such as token parsing failed, it will send a `UNAUTHORIZED` status to client immediately.

You maybe have noticed there is a `Secured` annotation.

```java
@NameBinding
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface Secured {
    Role[] value() default {};
}
```

It is a `@NameBinding` which will be applied on the target Jaxrs resources to indicate it will be filtered by `AuthenticationFilter`. In contrast, Servlet Filter use a url pattern to determine which url will be filtered.

`@NameBinding` is very similar with CDI `@Qualifier`.

Lets add `Secured` to `PostResource` to describe how to use it. I add `@Secured({Role.USER})` at class level and `    @Secured({Role.ADMIN})` at method level. Ideally, the `Secured` configuration on delete method will omit the configuration on class.

```java
@RequestScoped
@Path("posts")
@Secured({Role.USER})
public class PostResource {
	
	@DELETE
    @Path("{id}")
    @Secured({Role.ADMIN})
    public Response delete(@PathParam("id") Long id) {
		//...
    }
}
```
	
Till now, if user is authenticated, posts resources will be allowed to access. But it does not check the role.

Add another filer to verify the role.

```java
@Secured
@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Context
    private SecurityContext securityContext;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        // Get the resource class which matches with the requested URL
        // Extract the roles declared by it
        Class<?> resourceClass = resourceInfo.getResourceClass();
        List<Role> classRoles = extractRoles(resourceClass);

        // Get the resource method which matches with the requested URL
        // Extract the roles declared by it
        Method resourceMethod = resourceInfo.getResourceMethod();
        List<Role> methodRoles = extractRoles(resourceMethod);

        try {

            // Check if the user is allowed to execute the method
            // The method annotations override the class annotations
            if (methodRoles.isEmpty()) {
                checkPermissions(classRoles);
            } else {
                checkPermissions(methodRoles);
            }

        } catch (Exception e) {
            requestContext.abortWith(
                Response.status(Response.Status.FORBIDDEN).build());
        }
    }

    // Extract the roles from the annotated element
    private List<Role> extractRoles(AnnotatedElement annotatedElement) {
        if (annotatedElement == null) {
            return new ArrayList<>();
        } else {
            Secured secured = annotatedElement.getAnnotation(Secured.class);
            if (secured == null) {
                return new ArrayList<>();
            } else {
                Role[] allowedRoles = secured.value();
                return Arrays.asList(allowedRoles);
            }
        }
    }

    private void checkPermissions(List<Role> allowedRoles) throws Exception {
        // Check if the user contains one of the allowed roles
        // Throw an Exception if the user has not permission to execute the method
        // get user info from SecurityContext directly.

        Principal principal = securityContext.getUserPrincipal();

        if (principal != null && principal instanceof UserPrincipal) {
            UserPrincipal up = (UserPrincipal) principal;

            List<Role> userRoles = up.getRoles();

            boolean allowed = false;
            for (Role r : userRoles) {
                for (Role checkRole : allowedRoles) {
                    if (r.equals(checkRole)) {
                        allowed = true;
                    }
                }
            }

            if (!allowed) {
                throw new ForbiddenException("roles:"+userRoles.toString()+" is not allowed");
            }
        }

    }
}
```	

Again, `AuthorizationFilter` also implements `ContainerRequestFilter`. 

A lower priority `@Priority(Priorities.AUTHORIZATION)` is applied on this filter, so it will be exeuted after `AuthenticationFilter`, and the custom `SecurityContext` is ready. In the `filter` method, it gets user principal from `SecurityContext`, check if the role of authenticated user is in the list of value of `@Secured`. If not found, it will raise a `ForbiddenException`.

Write a simple test to verify if it works as expected.

```java
@RunWith(Arquillian.class)
public class JwtAuthTest {

    private static final Logger LOG = Logger.getLogger(JwtAuthTest.class.getName());

    @Deployment(testable = false)
    public static WebArchive createDeployment() {

        File[] extraJars = Maven.resolver().loadPomFromFile("pom.xml")
            .resolve(
                "org.projectlombok:lombok:1.16.8",
                "org.modelmapper:modelmapper:0.7.5",
                "org.apache.commons:commons-lang3:3.4",
                "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.6.3",
                "io.jsonwebtoken:jjwt:0.6.0"
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
            .addClasses(
                Role.class,
                User.class,
                User_.class,
                UserRepository.class//,
            )
            //add service classes
            .addClasses(
                BlogService.class,
                ResourceNotFoundException.class,
                PostForm.class,
                PostDetail.class,
                CommentForm.class,
                CommentDetail.class
            )
            .addClasses(
                UserService.class,
                ResourceNotFoundException.class,
                UsernameWasTakenException.class,
                UserForm.class,
                UserDetail.class,
                Credentials.class,
                SignupForm.class,
                IdToken.class,
                Credentials.class
            )
            //Add JAXRS resources classes
            .addClasses(
                JaxrsActiviator.class,
                PostResource.class,
                UserResource.class,
                CommentResource.class,
                JacksonConfig.class,
                ResourceNotFoundExceptionMapper.class,
                ValidationExceptionMapper.class,
                ValidationError.class,
                CustomBeanParamProvider.class,
                AuthenticationException.class,
                AuthenticationExceptionMapper.class,
                AuthResource.class
            )
            .addPackage(PlainPasswordEncoder.class.getPackage())
            .addPackage(BCryptPasswordEncoder.class.getPackage())
            .addPackage(PasswordEncoder.class.getPackage())
            .addClasses(
                AuthenticationFilter.class,
                AuthorizationFilter.class,
                JwtHelper.class,
                JwtUser.class,
                UserPrincipal.class,
                Secured.class
            )
            .addClasses(
                Initializer.class
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
        client.register(ResourceNotFoundExceptionMapper.class);
        client.register(ValidationExceptionMapper.class);
    }

    @After
    public void teardown() throws MalformedURLException {
        client.close();
    }

    @Test
    @RunAsClient
    public void testGetPostsWithoutAuthentication() throws MalformedURLException {

        LOG.log(Level.INFO, "base url @{0}", base);

        //get all posts
        final WebTarget targetGetAll = client.target(URI.create(new URL(base, "api/posts").toExternalForm()));
        final Response resGetAll = targetGetAll.request().accept(MediaType.APPLICATION_JSON_TYPE).get();
        assertEquals(401, resGetAll.getStatus());

        //You have to close the response manually... issue# RESTEASY-1120
        //see https://issues.jboss.org/browse/RESTEASY-1120
        resGetAll.close();

    }

    @Test
    @RunAsClient
    public void testGetPostsWithAuthentication() throws MalformedURLException {
        LOG.log(Level.INFO, "base url @{0}", base);

        final WebTarget targetAuthGetAll = client.target(URI.create(new URL(base, "api/auth/login").toExternalForm()));
        final Response resAuthGetAll = targetAuthGetAll.request()
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.json(new Credentials("testuser", "test123", true)));
        assertEquals(200, resAuthGetAll.getStatus());
        IdToken token = resAuthGetAll.readEntity(IdToken.class);

        client.register(new JwtTokenAuthentication(token.getToken()));

        //get all posts
        final WebTarget targetGetAll = client.target(URI.create(new URL(base, "api/posts").toExternalForm()));
        final Response resGetAll = targetGetAll.request().accept(MediaType.APPLICATION_JSON_TYPE).get();
        assertEquals(200, resGetAll.getStatus());

        PostDetail[] posts = resGetAll.readEntity(PostDetail[].class);

        assertTrue(posts.length == 1);

        Long id = posts[0].getId();

        //You have to close the response manually... issue# RESTEASY-1120
        //see https://issues.jboss.org/browse/RESTEASY-1120
        resGetAll.close();

        //get all posts
        final WebTarget targetDelAll = client.target(URI.create(new URL(base, "api/posts/"+id).toExternalForm()));
        final Response resDelAll = targetDelAll.request().accept(MediaType.APPLICATION_JSON_TYPE).delete();
        assertEquals(403, resDelAll.getStatus());
    }

}
```

In the test `testGetPostsWithoutAuthentication()`, user is not authenticated, it will response a 401 status.

In the test `testGetPostsWithAuthentication()`, firstly it tries to input user credentials and get token.

Then use a `JwtTokenAuthentication` to add token to HTTP request header.

```java
public class JwtTokenAuthentication implements ClientRequestFilter {
    
    private final String authHeader;

    public JwtTokenAuthentication(String token) {
        this.authHeader = "Bearer " + token;
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        requestContext.getHeaders().putSingle(HttpHeaders.AUTHORIZATION, authHeader);
    }
    
}
```

It is a `ClientRequestFilter`, Jaxrs client register this filter, and it will be applied in the further steps.

```java
 client.register(new JwtTokenAuthentication(token.getToken()));
```

`/api/posts` is allowed now, but `/api/posts/{id}` requires a `ADMIN` role, it will fail and return a 403 status.
	
Get source codes from my Github account, and try it yourself.

https://github.com/hantsy/angularjs-ee7-sample

There is a **jwt** folder to demonstrate the JWT token based security.


[jwt]: https://jwt.io "JSON Web Token"
[auth0]: https://auth0.net "Auth0"








