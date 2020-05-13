package org.jivesoftware.openfire.plugin.rest;

import org.ifsoft.sso.Password;
import org.jivesoftware.openfire.admin.AdminManager;
import org.jivesoftware.openfire.auth.AuthFactory;
import org.jivesoftware.openfire.auth.ConnectionException;
import org.jivesoftware.openfire.auth.InternalUnauthenticatedException;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;

/**
 * The Class AuthFilter.
 */
public class AuthFilter implements ContainerRequestFilter {

    /** The log. */
    private static Logger LOG = LoggerFactory.getLogger(AuthFilter.class);

    /** The http request. */
    @Context
    private HttpServletRequest httpRequest;

    /** The plugin. */
    private RESTServicePlugin plugin = RESTServicePlugin.getInstance();

    @Override
    public void filter(ContainerRequestContext containerRequest) throws IOException {
        if (!plugin.isEnabled()) {
            LOG.error("weizisheng error2");
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        // Let the preflight request through the authentication
        if ("OPTIONS".equals(containerRequest.getMethod())) {
            LOG.error("weizisheng error1");
            return;
        }

        if (!plugin.getAllowedIPs().isEmpty()) {
            // Get client's IP address
            String ipAddress = httpRequest.getHeader("x-forwarded-for");
            if (ipAddress == null) {
                ipAddress = httpRequest.getHeader("X_FORWARDED_FOR");
                if (ipAddress == null) {
                    ipAddress = httpRequest.getHeader("X-Forward-For");
                    if (ipAddress == null) {
                        ipAddress = httpRequest.getRemoteAddr();
                    }
                }
            }
            if (!plugin.getAllowedIPs().contains(ipAddress)) {
                LOG.warn("REST API rejected service to IP address: " + ipAddress);
                throw new WebApplicationException(Status.UNAUTHORIZED);
            }
        }

        // To be backwards compatible to userservice 1.*

        if ("restapi/v1/userservice".equals(containerRequest.getUriInfo().getPath())) {
            LOG.error("weizisheng error3");
            return;
        }

        // Get the authentification passed in HTTP headers parameters
        String auth = containerRequest.getHeaderString("authorization");

        if (auth == null) {
            LOG.error("weizisheng error4");
            throw new WebApplicationException(Status.UNAUTHORIZED);
        }

        // if master secret used, allow everything

        if (auth.equals(plugin.getSecret())) {
            LOG.error("weizisheng error5");
            return;
        }

        // if permission code is used, allow everything except admin
        // TODO fix solo plugin that will be broken by this change

        if (!containerRequest.getUriInfo().getPath().startsWith("restapi/v1/admin"))
        {
            if (auth.equals(plugin.getPermission())) {
                LOG.error("weizisheng error6");
                return;
            }
        }

        // accept user based authentication if allowed
        LOG.error("weizisheng error7");
        if ("basic".equals(plugin.getHttpAuth()))
        {
            String[] usernameAndPassword = BasicAuth.decode(auth);

            // If username or password fail
            if (usernameAndPassword == null || usernameAndPassword.length != 2) {
                throw new WebApplicationException(Status.UNAUTHORIZED);
            }

            if (containerRequest.getUriInfo().getPath().startsWith("restapi/v1/admin"))
            {
                boolean userAdmin = AdminManager.getInstance().isUserAdmin(usernameAndPassword[0], true);

                if (!userAdmin) {
                    LOG.warn("REST authorization fail: not admin user " + usernameAndPassword[0]);
                    throw new WebApplicationException(Status.UNAUTHORIZED);
                }
            }

            if (Password.passwords.containsKey(usernameAndPassword[0]))     // SSO
            {
                String passkey = Password.passwords.get(usernameAndPassword[0]).trim();

                if (usernameAndPassword[1].trim().equals(passkey) == false)
                {
                    LOG.warn("REST SSO authorization fail: " + usernameAndPassword[0] + " " + passkey + " " + usernameAndPassword[1]);
                    throw new WebApplicationException(Status.UNAUTHORIZED);
                }
                else return;
            }

            try {
                AuthFactory.authenticate(usernameAndPassword[0], usernameAndPassword[1]);
            } catch (UnauthorizedException e) {
                throw new WebApplicationException(Status.UNAUTHORIZED);
            } catch (ConnectionException e) {
                throw new WebApplicationException(Status.UNAUTHORIZED);
            } catch (InternalUnauthenticatedException e) {
                throw new WebApplicationException(Status.UNAUTHORIZED);
            }
        } else {
            if (!auth.equals(plugin.getSecret())) {
                LOG.warn("Wrong secret key authorization. Provided key: " + auth);
                throw new WebApplicationException(Status.UNAUTHORIZED);
            }
        }
    }
}
