package org.jivesoftware.openfire.plugin.rest;

import org.jivesoftware.openfire.http.HttpBindManager;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;

/**
 * The Class CORSFilter.
 */
public class CORSFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        final HttpBindManager boshManager = HttpBindManager.getInstance();

        containerResponseContext.getHeaders().add("Access-Control-Allow-Origin", boshManager.getCORSAllowOrigin());
        containerResponseContext.getHeaders().add("Access-Control-Allow-Headers", HttpBindManager.HTTP_BIND_CORS_ALLOW_HEADERS_DEFAULT + ", Authorization");
        containerResponseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
        containerResponseContext.getHeaders().add("Access-Control-Allow-Methods", HttpBindManager.HTTP_BIND_CORS_ALLOW_METHODS_DEFAULT);

    }

//    /* (non-Javadoc)
//     * @see com.sun.jersey.spi.container.ContainerResponseFilter#filter(com.sun.jersey.spi.container.ContainerRequest, com.sun.jersey.spi.container.ContainerResponse)
//     */
//    @Override
//    public ContainerResponse filter(ContainerRequest request, ContainerResponse response)
//    {
//        final HttpBindManager boshManager = HttpBindManager.getInstance();
//
//        response.getHttpHeaders().add("Access-Control-Allow-Origin", boshManager.getCORSAllowOrigin());
//        response.getHttpHeaders().add("Access-Control-Allow-Headers", HttpBindManager.HTTP_BIND_CORS_ALLOW_HEADERS_DEFAULT + ", Authorization");
//        response.getHttpHeaders().add("Access-Control-Allow-Credentials", "true");
//        response.getHttpHeaders().add("Access-Control-Allow-Methods", HttpBindManager.HTTP_BIND_CORS_ALLOW_METHODS_DEFAULT);
//
//        return response;
//    }
}
