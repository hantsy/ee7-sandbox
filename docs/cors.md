# CORS

In the real world applications, our APIs could be exposed to the public and be allowed to access by other resources in a different domain.

There are some approaches to access remote APIs.

1. JSONP return format on HTTP GET method. I do use JSONP in any case. I think it is an anti-pattern.
2. Use a proxy server in the client, which make you invoke remote APIs as local APIs. I like this approache in NodeJS applications. All http servers, such as Nginx, Apache 2 have rich proxy settings.
3. Enable CORS filter in server side. This post will CORS support in server side.

For example, our apis is hosted on https://foo.com/api. And another website hosted on http://bar.com want to access https://foo.com/api. In the modern browsers, when a client navigates the remote apis(aka https://foo.com/apis), it will try to send a `OPTIONS` request firstly, and waiting for the server response, then decide the next steps. This extra request is called preflight request.

If the server response return 200, it means the generic request is allowed, then send generic request. If the server denied, it could return authorization failure.

Create a `ContainerResponseFilter` to process the preflight request, and essential headers to response context.

```java
@Provider
public class CORSResponseFilter implements ContainerResponseFilter {

    private final static Logger LOG = Logger.getLogger(CORSFilter.class.getName());

    final static String DEFAULT_ALLOW_METHODS = "GET,POST,PUT,DELETE,OPTIONS,HEAD";
    final static String DEFAULT_ALLOW_HEADERS = "origin,content-type,accept,authorization";
    final static int MAX_AGE = 24 * 60 * 60;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        LOG.log(Level.FINEST, "...entering CORSFilter.");

        if (isPreflightRequest(requestContext)) {
            LOG.log(Level.FINEST, "...handling preflight request.");
            responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");

            responseContext.getHeaders().add("Access-Control-Allow-Headers", createRequestedHeaders(requestContext));
            responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");

            responseContext.getHeaders().add("Access-Control-Max-Age", MAX_AGE);
            responseContext.getHeaders().add("Access-Control-Allow-Methods", DEFAULT_ALLOW_METHODS);
        }

    }

    private boolean isPreflightRequest(ContainerRequestContext requestContext) {
        return requestContext.getHeaderString("Origin") != null && HttpMethod.OPTIONS.equals(requestContext.getMethod());
    }

    private String createRequestedHeaders(ContainerRequestContext requestContext) {
        String headers = requestContext.getHeaderString("Access-Control-Request-Headers");
        return headers != null ? headers : DEFAULT_ALLOW_HEADERS;
    }

//    private String createRequestedMethods(ContainerRequestContext requestContext) {
//         String method = requestContext.getHeaderString("Access-Control-Request-Method");
//         return DEFAULT_ALLOW_METHODS;
//    }
}
```

Add another `CORSRequestFilter` which implements `ContainerRequestFilter`.

```java
@Provider
@PreMatching
public class CORSRequestFilter implements ContainerRequestFilter {

    private final static Logger LOG = Logger.getLogger(CORSRequestFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestCtx) throws IOException {
        LOG.info("Executing REST request filter");

        // When HttpMethod comes as OPTIONS, just acknowledge that it accepts...
        if (requestCtx.getRequest().getMethod().equals("OPTIONS")) {
            LOG.info("HTTP Method (OPTIONS) - Detected!");

            // Just send a OK signal back to the browser
            requestCtx.abortWith(Response.status(Response.Status.OK).build());
        }
    }
}
```

If the incoming request is an `OPTIONS` request, it aborts the request and sends a 200 status to client directly. It bypasses the processing the whole request and saves some time more or less.

If you need fine-grained control for CORS settings, use Resteasy built-in `CorsFitler` instead.

```java
@Provider
public class CorsFeature implements Feature {

    @Override
    public boolean configure(FeatureContext context) {
        CorsFilter corsFilter = new CorsFilter();
        corsFilter.getAllowedOrigins().add("http://localhost:3000");

        context.register(corsFilter);
        return true;
    }

}
```

Check [Mozilla Developer Network for more CORS](https://developer.mozilla.org/en-US/docs/HTTP/Access_control_CORS).





