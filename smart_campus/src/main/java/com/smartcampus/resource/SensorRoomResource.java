package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * SensorRoom Resource - Part 2
 *
 * Manages the /api/v1/rooms collection.
 * Handles all CRUD operations for campus rooms.
 *
 * Endpoints:
 *   GET    /api/v1/rooms          - List all rooms
 *   POST   /api/v1/rooms          - Create a new room
 *   GET    /api/v1/rooms/{roomId} - Get a specific room by ID
 *   DELETE /api/v1/rooms/{roomId} - Delete a room (blocked if sensors exist)
 */
@Path("rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorRoomResource {

    private final DataStore store = DataStore.getInstance();

    // ─── GET /api/v1/rooms ───────────────────────────────────────────────────────
    /**
     * Part 2.1 - Returns a comprehensive list of all rooms (full objects).
     * Returning full objects avoids extra client-side lookups and reduces
     * the number of API calls needed (bandwidth vs. processing trade-off).
     */
    @GET
    public Response getAllRooms() {
        List<Room> roomList = new ArrayList<>(store.getRooms().values());
        return Response.ok(roomList).build();
    }

    // ─── POST /api/v1/rooms ──────────────────────────────────────────────────────
    /**
     * Part 2.1 - Creates a new room.
     * Returns HTTP 201 Created with the newly created room in the response body.
     */
    @POST
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Room ID is required\"}")
                    .build();
        }
        if (store.roomExists(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"Room with ID '" + room.getId() + "' already exists\"}")
                    .build();
        }
        store.addRoom(room);
        URI location = URI.create("http://localhost:8080/api/v1/rooms/" + room.getId());
        return Response.created(location).entity(room).build();
    }

    // ─── GET /api/v1/rooms/{roomId} ──────────────────────────────────────────────
    /**
     * Part 2.1 - Fetches detailed metadata for a specific room by ID.
     * Returns HTTP 404 Not Found if the room does not exist.
     */
    @GET
    @Path("{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Room not found with ID: " + roomId + "\"}")
                    .build();
        }
        return Response.ok(room).build();
    }

    // ─── DELETE /api/v1/rooms/{roomId} ───────────────────────────────────────────
    /**
     * Part 2.2 - Deletes a room by ID.
     *
     * Business Logic Constraint:
     * A room CANNOT be deleted if it still has sensors assigned to it.
     * Throws RoomNotEmptyException → mapped to HTTP 409 Conflict.
     *
     * Idempotency:
     * DELETE is idempotent for existing rooms. If the same room is deleted
     * a second time, a 404 Not Found is returned since the resource no
     * longer exists - the server state remains consistent.
     */
    @DELETE
    @Path("{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);

        // 404 if room does not exist
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Room not found with ID: " + roomId + "\"}")
                    .build();
        }

        // 409 Conflict if the room still has sensors - prevent data orphans
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                "Room '" + roomId + "' cannot be deleted. " +
                "It still has " + room.getSensorIds().size() + " active sensor(s) assigned: "
                + room.getSensorIds()
            );
        }

        store.deleteRoom(roomId);
        return Response.noContent().build(); // 204 No Content on success
    }
}
