package no.capraconsulting.exception;

import com.google.gson.stream.MalformedJsonException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class MalformedJsonMapper implements ExceptionMapper<MalformedJsonException> {
    @Override
    public Response toResponse(MalformedJsonException e) {
        return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
    }
}
