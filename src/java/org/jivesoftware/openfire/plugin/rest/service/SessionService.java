package org.jivesoftware.openfire.plugin.rest.service;

import org.jivesoftware.openfire.plugin.rest.controller.SessionController;
import org.jivesoftware.openfire.plugin.rest.entity.SessionEntities;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;

import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("restapi/v1/sessions")
public class SessionService {

    private SessionController sessionController;

    @PostConstruct
    public void init() {
        sessionController = SessionController.getInstance();
    }

    @POST
    public Response notifySessions(String payload) throws ServiceException {
        sessionController.notifySessions(payload);
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SessionEntities getAllSessions() throws ServiceException {
        return sessionController.getAllSessions();
    }

    @GET
    @Path("/{username}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SessionEntities getUserSessions(@PathParam("username") String username) throws ServiceException {
        return sessionController.getUserSessions(username);
    }

    @POST
    @Path("/{username}")
    public Response notifySession(@PathParam("username") String username, String payload) throws ServiceException {
        sessionController.notifySession(username, payload);
        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Path("/{username}")
    public Response kickSession(@PathParam("username") String username) throws ServiceException {
        sessionController.removeUserSessions(username);
        return Response.status(Response.Status.OK).build();
    }

}
