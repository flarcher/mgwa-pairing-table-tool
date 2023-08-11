package org.mgwa.w40k.pairing.api.resource;

import org.mgwa.w40k.pairing.api.model.Matrix;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("matrix")
public class MatrixResource {

    public MatrixResource() {}

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Matrix getDefault() {
        return new Matrix();
    }
}
