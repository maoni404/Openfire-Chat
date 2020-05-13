package org.jivesoftware.openfire.plugin.rest.service;

import org.jivesoftware.openfire.plugin.rest.controller.MUCRoomController;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("restapi/v1/chatrooms/{roomName}/owners")
public class MUCRoomOwnersService {

	@POST
	@Path("/{jid}")
	public Response addMUCRoomOwner(@DefaultValue("conference") @QueryParam("servicename") String serviceName,
			@PathParam("jid") String jid, @PathParam("roomName") String roomName) throws ServiceException {
		MUCRoomController.getInstance().addOwner(serviceName, roomName, jid);
		return Response.status(Status.CREATED).build();
	}

	@POST
	@Path("/group/{groupname}")
	public Response addMUCRoomOwnerGroup(@DefaultValue("conference") @QueryParam("servicename") String serviceName,
			@PathParam("groupname") String groupname, @PathParam("roomName") String roomName) throws ServiceException {
		MUCRoomController.getInstance().addOwner(serviceName, roomName, groupname);
		return Response.status(Status.CREATED).build();
	}

	@DELETE
	@Path("/{jid}")
	public Response deleteMUCRoomOwner(@PathParam("jid") String jid,
			@DefaultValue("conference") @QueryParam("servicename") String serviceName,
			@PathParam("roomName") String roomName) throws ServiceException {
		MUCRoomController.getInstance().deleteAffiliation(serviceName, roomName, jid);
		return Response.status(Status.OK).build();
	}

	@DELETE
	@Path("/group/{groupname}")
	public Response deleteMUCRoomOwnerGroup(@PathParam("groupname") String groupname,
			@DefaultValue("conference") @QueryParam("servicename") String serviceName,
			@PathParam("roomName") String roomName) throws ServiceException {
		MUCRoomController.getInstance().deleteAffiliation(serviceName, roomName, groupname);
		return Response.status(Status.OK).build();
	}
}
