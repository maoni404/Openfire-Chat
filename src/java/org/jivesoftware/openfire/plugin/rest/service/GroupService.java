package org.jivesoftware.openfire.plugin.rest.service;

import org.jivesoftware.openfire.plugin.rest.controller.GroupController;
import org.jivesoftware.openfire.plugin.rest.entity.GroupEntities;
import org.jivesoftware.openfire.plugin.rest.entity.GroupEntity;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;

import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("restapi/v1/groups")
public class GroupService {

	private GroupController groupController;

	@PostConstruct
	public void init() {
		groupController = GroupController.getInstance();
	}

	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public GroupEntities getGroups() throws ServiceException {
		return new GroupEntities(groupController.getGroups());
	}

	@POST
	public Response createGroup(GroupEntity groupEntity) throws ServiceException {
		groupController.createGroup(groupEntity);
		return Response.status(Response.Status.CREATED).build();
	}
	
	@GET
	@Path("/{groupName}")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public GroupEntity getGroup(@PathParam("groupName") String groupName) throws ServiceException {
		return groupController.getGroup(groupName);
	}
	
	@PUT
	@Path("/{groupName}")
	public Response updateGroup(@PathParam("groupName") String groupName, GroupEntity groupEntity) throws ServiceException {
		groupController.updateGroup(groupName, groupEntity);
		return Response.status(Response.Status.OK).build();
	}

	@DELETE
	@Path("/{groupName}")
	public Response deleteGroup(@PathParam("groupName") String groupName) throws ServiceException {
		groupController.deleteGroup(groupName);
		return Response.status(Response.Status.OK).build();
	}
}
