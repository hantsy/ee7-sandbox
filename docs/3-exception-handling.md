# Exception handling and validation

In this post, I will demonstrate how to handle exceptions and process input data validation in Jaxrs.

## Handles exceptions

When you design your software, you have to consider the exceptional path in the business logic. Every exception path can be described as an exception.

For example, in a registeration case, when you fill a registeration form, user was taken by others, it should display the info when you try to submit the form. 

In the software design view, you can create a `UsernameWasTakenException` runtime exception to describe this case, and throw it in the exception block in your business code. And you have to handle this exception and response friendly message to the client in your APIs.

In this blog sample, assume when a user access a none-existing blog entry, it should notify the user the blog entry is not found. 

Create a `ResourceNotFoundException` class for general case.

	public class ResourceNotFoundException extends RuntimeException {
		
		private static final long serialVersionUID = 1L;

		public ResourceNotFoundException() {
		}

		public ResourceNotFoundException(String message) {
			super(message);
		}
		
	}
	
Throw the exception in the `BlogService`.

	Post post = posts.findById(id);
	if (post == null) {
		throw new ResourceNotFoundException("post:" + id + " was not found");
	}
	return post;
	
When you get a post by id, update a post by id, or delete a post by id, it should check the existence of the Post according to the given id.

Now write test codes to verify a `ResourceNotFoundException` is thrown when a blog entry(Post) is not found.

Add three test cases for the above case, verify a `ResourceNotFoundException` is caught in the executing phase.

	@Test(expected = ResourceNotFoundException.class)
    public void testGetPostByIdNotFound() {

        given(posts.findById(1L))
            .willThrow(ResourceNotFoundException.class);

        PostDetail detail = service.findPostById(1L);

        verify(posts, times(1)).findById(anyLong());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testUpdatePostNotFound() {

        PostForm form = Fixtures.newPostForm(TITLE + "updated", CONTENT + "updated");

        given(posts.findById(1L))
            .willThrow(ResourceNotFoundException.class);

        PostDetail detail = service.updatePost(1L, form);

        verify(posts, times(1)).findById(anyLong());

    }

    @Test(expected = ResourceNotFoundException.class)
    public void testDeletePostNotFound() {

        given(posts.findById(1L))
            .willThrow(ResourceNotFoundException.class);

        service.deletePostById(1L);

        verify(posts, times(1)).findById(anyLong());
    }

In the `PostResource`, the `BlogService` is injected and invoked to handle client request, it could throw `ResourceNotFoundException` at runtime. Jaxrs provides `ExceptionMapper` to process the exception and tranform it to `Response` to client.

	@Provider
	public class ResourceNotFoundExceptionMapper implements ExceptionMapper<ResourceNotFoundException> {

		@Override
		public Response toResponse(ResourceNotFoundException exception) {
			return Response.status(Response.Status.NOT_FOUND)
				.entity(exception.getMessage())
				.build();
		}

	}
	
When `ResourceNotFoundException` is caught, wrap the result as a `Response` to the client, return 404 status code and the exception message as body.	

Add test codes in the `PostResourceTest` to verify if it happens.

	@Test
    @RunAsClient
    public void testGetPostNotFound() throws MalformedURLException {

        //get the created data
        String location = "api/posts/1000";
        final WebTarget targetGet = client.target(URI.create(new URL(base, location).toExternalForm()));
        Response responseGet = targetGet.request().accept(MediaType.APPLICATION_JSON_TYPE).get();
        assertEquals(404, responseGet.getStatus());
    }

    @Test
    @RunAsClient
    public void testUpdatePostNotFound() throws MalformedURLException {
        String location = "api/posts/1000";

        PostForm updatePostForm = Fixtures.newPostForm(TITLE + "updated", CONTENT + "updated");
        final WebTarget targetPut = client.target(URI.create(new URL(base, location).toExternalForm()));

        final Response responsePut = targetPut
            .request()
            .put(Entity.json(updatePostForm));

        assertEquals(404, responsePut.getStatus());
    }

    @Test
    @RunAsClient
    public void testDeletePostNotFound() throws MalformedURLException {
        String location = "api/posts/1000";

        final WebTarget targetPut = client.target(URI.create(new URL(base, location).toExternalForm()));

        final Response responsePut = targetPut
            .request()
            .delete();

        assertEquals(404, responsePut.getStatus());
    }

Same as the service test, in these three cases, it should return 404 status code.

**NOTE**: Do not forget to add the related classes in the `@Deployment` archive.

	@Deployment(testable = false)
		public static WebArchive createDeployment() {
		
		//...
		final WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war")
			//...
			.addClasses(
			//...
			ResourceNotFoundExceptionMapper.class,
			//...
			

And register it in the Jaxrs client, in the test code.

	client.register(ResourceNotFoundExceptionMapper.class);

## Validate request data			

In the Jaxrs 2.0, Bean validation works well with Jaxrs. It can validate method arguments and return result of a method.

	* For request data, it should return a 400(Bad request) when it is invalid.
	* For the return result of a method, it should return a 500(server error) whend the validation failed.
	
Every subclass of `ValditaionExcpetion` esp the `ConstraintViolationException` should be caught and trigger the handler to handle it.

Resteasy has a built-in `ExceptionMapper`(org.jboss.resteasy.api.validation.ResteasyViolationExceptionMapper) for the `ResteasyViolationException` which is a subclass of `ConstraintViolationException`. 

**NOTE**: Befor Resteasy 3.0.12, `ResteasyViolationException` just implemented interface `ValidationException`, but did not extend `ConstraintViolationException`. You have to handle the Resteasy specific `ResteasyViolationException` to process the validation exceptions. Luckily, Resteasy 3.0.14 is shipped wtih Wildfly 10, in which `ResteasyViolationException` is refactored and subclassed from `ConstraintViolationException`. Thus it allow you process the exceptions as in other containers, such as Glassfish.

When a `ConstraintViolationException` caught, it will be converted to the Resteasy specific `ResteasyViolationException` and generate a report to the client.

In this sample, I will create a `ExceptionMapper` to handle  `ConstraintViolationException` and wrap the exception into friendly messages to client.

	public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

		private static final Logger LOGGER = Logger.getLogger(ValidationExceptionMapper.class.getName());
	 
		@Context
		private Request request;
	 
		@Override
		public Response toResponse(final ValidationException exception) {
			if (exception instanceof ConstraintViolationException) {
				LOGGER.log(Level.FINER, "Following ConstraintViolations has been encountered.", exception);
				final ConstraintViolationException cve = (ConstraintViolationException) exception;
				final Response.ResponseBuilder response = Response.status(getStatus(cve));
	 
				// Entity
				final List<Variant> variants = Variant.mediaTypes(MediaType.APPLICATION_JSON_TYPE,MediaType.APPLICATION_XML_TYPE).build();
				final Variant variant = request.selectVariant(variants);
				if (variant != null) {
					response.type(variant.getMediaType());
				}
				response.entity(
						new GenericEntity<>(
								getEntity(cve.getConstraintViolations()),
								new GenericType<List<ValidationError>>() {}.getType()
						)
				);
	 
				return response.build();
			} else {
				LOGGER.log(Level.WARNING, "Unexpected Bean Validation problem.", exception);
	 
				return Response.serverError().entity(exception.getMessage()).build();
			}
		}
	 
		private List<ValidationError> getEntity(final Set<ConstraintViolation<?>> violations) {
			final List<ValidationError> errors = new ArrayList<>();
	 
			violations.stream().forEach((violation) -> {
				errors.add(new ValidationError(getInvalidValue(violation.getInvalidValue()), violation.getMessage(),
					violation.getMessageTemplate(), getPath(violation)));
			});
	 
			return errors;
		}
	 
		private String getInvalidValue(final Object invalidValue) {
			if (invalidValue == null) {
				return null;
			}
	 
			if (invalidValue.getClass().isArray()) {
				return Arrays.toString((Object[]) invalidValue);
			}
	 
			return invalidValue.toString();
		}
	 
		private Response.Status getStatus(final ConstraintViolationException exception) {
			return getResponseStatus(exception.getConstraintViolations());
		}
	 
		private Response.Status getResponseStatus(final Set<ConstraintViolation<?>> constraintViolations) {
			final Iterator<ConstraintViolation<?>> iterator = constraintViolations.iterator();
	 
			if (iterator.hasNext()) {
				return getResponseStatus(iterator.next());
			} else {
				return Response.Status.BAD_REQUEST;
			}
		}
	 
		private Response.Status getResponseStatus(final ConstraintViolation<?> constraintViolation) {
			for (final Path.Node node : constraintViolation.getPropertyPath()) {
				final ElementKind kind = node.getKind();
	 
				if (ElementKind.RETURN_VALUE.equals(kind)) {
					return Response.Status.INTERNAL_SERVER_ERROR;
				}
			}
	 
			return Response.Status.BAD_REQUEST;
		}
	 
		private String getPath(final ConstraintViolation<?> violation) {
			final String leafBeanName = violation.getLeafBean().getClass().getSimpleName();
			final String leafBeanCleanName = (leafBeanName.contains("$")) ? leafBeanName.substring(0,
					leafBeanName.indexOf("$")) : leafBeanName;
			final String propertyPath = violation.getPropertyPath().toString();
	 
			return leafBeanCleanName + (!"".equals(propertyPath) ? '.' + propertyPath : "");
		}

	}

In this sample, use a `ValidationError` to wrap the exception messages.

	@Getter
	@Setter
	@ToString
	public class ValidationError {

		private String invalidValue;

		private String message;

		private String messageTemplate;

		private String path;

		public ValidationError() {

		}

		public ValidationError(
			final String invalidValue,
			final String message,
			final String messageTemplate,
			final String path) {
			this.invalidValue = invalidValue;
			this.message = message;
			this.messageTemplate = messageTemplate;
			this.path = path;
		}
	}

Add a `@Valid` annoatation to the method arguments, eg. Add `@Valid` to `save` method, which will validate the request body when saving the `PostForm`.

    @POST
    @Consumes(value = MediaType.APPLICATION_JSON)
    public Response save(@Valid PostForm post) {
		//...
    }
	
And in the `PostForm`, add some Bean validation constraint annotations on the fields.

	public class PostForm implements Serializable {

		private static final long serialVersionUID = 1L;

		@NotBlank
		@NonNull
		private String title;

		@NotBlank
		@NonNull
		private String content;

	}

Add a new test case in the `PostResourceTest` to verify it.

    @Test
    @RunAsClient
    public void testCreatePostFormIsInvalid() throws MalformedURLException {
        //create a new post
        PostForm newPostForm = new PostForm();
        final WebTarget targetPost = client.target(URI.create(new URL(base, "api/posts").toExternalForm()));
        final Response resPost = targetPost.request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(newPostForm));

        LOG.info("response content string @\n" + resPost.readEntity(String.class));
        assertEquals(400, resPost.getStatus());

        resPost.close();
    }
	
## Summary

In this post, we use `ExceptionMapper` to process two general cases for the exceptions, custom exceptions and input data validation.


