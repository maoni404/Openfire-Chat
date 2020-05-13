package org.jivesoftware.openfire.plugin.rest.service;

import org.jivesoftware.openfire.plugin.rest.RESTServicePlugin;
import org.jivesoftware.openfire.plugin.rest.entity.SystemProperties;
import org.jivesoftware.openfire.plugin.rest.entity.SystemProperty;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;

import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("restapi/v1/system/properties")
public class RestAPIService {

	private RESTServicePlugin plugin;

	@PostConstruct
	public void init() {
		plugin = RESTServicePlugin.getInstance();
	}

	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public SystemProperties getSystemProperties() {
		return plugin.getSystemProperties();
	}
	
	@GET
	@Path("/{propertyKey}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public SystemProperty getSystemProperty(@PathParam("propertyKey") String propertyKey) throws ServiceException {
		return plugin.getSystemProperty(propertyKey);
	}

	@POST
	public Response createSystemProperty(SystemProperty systemProperty) throws ServiceException {
		plugin.createSystemProperty(systemProperty);
		return Response.status(Response.Status.CREATED).build();
	}

	@PUT
	@Path("/{propertyKey}")
	public Response updateUser(@PathParam("propertyKey") String propertyKey, SystemProperty systemProperty) throws ServiceException {
		plugin.updateSystemProperty(propertyKey, systemProperty);
		return Response.status(Response.Status.OK).build();
	}

	@DELETE
	@Path("/{propertyKey}")
	public Response deleteUser(@PathParam("propertyKey") String propertyKey) throws ServiceException {
		plugin.deleteSystemProperty(propertyKey);
		return Response.status(Response.Status.OK).build();
	}
}
