package com.libria.rest;

import com.libria.domain.*;
import com.libria.exception.UserAlreadyExistException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/members")
@Produces(MediaType.APPLICATION_JSON)
public class MemberResource {

    private static final Library LIB = SharedLibrary.INSTANCE;

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(Member member) {
        try {
            LIB.registerUser(member);
            return Response.status(Response.Status.CREATED).entity(member).build();
        } catch (UserAlreadyExistException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }
}