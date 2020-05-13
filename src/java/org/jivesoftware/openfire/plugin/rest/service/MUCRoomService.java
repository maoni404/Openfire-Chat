package org.jivesoftware.openfire.plugin.rest.service;

import org.jivesoftware.openfire.plugin.rest.controller.MUCRoomController;
import org.jivesoftware.openfire.plugin.rest.entity.*;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("restapi/v1/chatrooms")
public class MUCRoomService {

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public MUCRoomEntities getMUCRooms(@DefaultValue("conference") @QueryParam("servicename") String serviceName,
            @DefaultValue(MUCChannelType.PUBLIC) @QueryParam("type") String channelType,
            @QueryParam("search") String roomSearch,
            @DefaultValue("false") @QueryParam("expandGroups") Boolean expand) {
        return MUCRoomController.getInstance().getChatRooms(serviceName, channelType, roomSearch, expand);
    }

    @GET
    @Path("/{roomName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public MUCRoomEntity getMUCRoomJSON2(@PathParam("roomName") String roomName,
            @DefaultValue("conference") @QueryParam("servicename") String serviceName,
            @DefaultValue("false") @QueryParam("expandGroups") Boolean expand) throws ServiceException {
        return MUCRoomController.getInstance().getChatRoom(roomName, serviceName, expand);
    }


    @DELETE
    @Path("/{roomName}")
    public Response deleteMUCRoom(@PathParam("roomName") String roomName,
            @DefaultValue("conference") @QueryParam("servicename") String serviceName) throws ServiceException {
        MUCRoomController.getInstance().deleteChatRoom(roomName, serviceName);
        return Response.status(Status.OK).build();
    }

    @POST
    public Response createMUCRoom(@DefaultValue("conference") @QueryParam("servicename") String serviceName,
            MUCRoomEntity mucRoomEntity) throws ServiceException {
        MUCRoomController.getInstance().createChatRoom(serviceName, mucRoomEntity);
        return Response.status(Status.CREATED).build();
    }

    @PUT
    @Path("/{roomName}")
    public Response udpateMUCRoom(@PathParam("roomName") String roomName,
            @DefaultValue("conference") @QueryParam("servicename") String serviceName, MUCRoomEntity mucRoomEntity)
            throws ServiceException {
        MUCRoomController.getInstance().updateChatRoom(roomName, serviceName, mucRoomEntity);
        return Response.status(Status.OK).build();
    }

    @GET
    @Path("/{roomName}/participants")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public ParticipantEntities getMUCRoomParticipants(@PathParam("roomName") String roomName,
            @DefaultValue("conference") @QueryParam("servicename") String serviceName) {
        return MUCRoomController.getInstance().getRoomParticipants(roomName, serviceName);
    }

    @GET
    @Path("/{roomName}/occupants")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OccupantEntities getMUCRoomOccupants(@PathParam("roomName") String roomName,
            @DefaultValue("conference") @QueryParam("servicename") String serviceName) {
        return MUCRoomController.getInstance().getRoomOccupants(roomName, serviceName);
    }

    @GET
    @Path("/{roomName}/chathistory")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public MUCRoomMessageEntities getMUCRoomHistory(@PathParam("roomName") String roomName,
                                                    @DefaultValue("conference") @QueryParam("servicename") String serviceName) throws ServiceException {
        return MUCRoomController.getInstance().getRoomHistory(roomName, serviceName);
    }

}
