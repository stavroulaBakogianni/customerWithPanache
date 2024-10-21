package org.acme.restClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.acme.dto.ValidateVatResponse;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 *
 * @author stavroulabakogianni
 */
@Path("/checkVat")
@RegisterRestClient
public interface ValidateService {

    @GET
    @Path("/{countryCode}/{vatNumber}")
    ValidateVatResponse checkVat(@PathParam("countryCode") String countryCode, @PathParam("vatNumber") String vatNumber);
}

