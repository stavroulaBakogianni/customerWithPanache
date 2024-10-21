package org.acme.resource;

import io.quarkus.logging.Log;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.acme.dto.CustomerDTO;
import org.acme.dto.ValidateVatResponse;
import org.acme.restClient.ValidateService;
import org.acme.service.CustomerService;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class CustomeResource {

    @Inject
    CustomerService customerService;

    @Inject
    @RestClient
    ValidateService validateService;

    @Path("/test/customer")
    @POST
    public Response saveTestCustomer(@Valid CustomerDTO customerDto) {
        // Logging για να δούμε αν το VAT είναι null πριν το mapping
        if (customerDto.getVat() == null) {
            Log.error("VAT is null in the incoming CustomerDTO");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("VAT cannot be null")
                    .build();
        }

        Optional<CustomerDTO> savedCustomer = customerService.saveCustomerEntity(customerDto);

        if (savedCustomer.isPresent()) {
            Log.infof("Customer saved successfully with VAT: %s", customerDto.getVat());
            return Response.ok(savedCustomer.get()).build();
        } else {
            Log.errorf("Failed to save customer with VAT: %s", customerDto.getVat());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error saving customer").build();
        }

    }

    @Path("/customers")
    @GET
    public Response getAllCustomers() {

        List<CustomerDTO> customers = customerService.findAllCustomers();

        if (customers.isEmpty()) {
            Log.warn("No customers found.");
            return Response.ok(customers).build();
        } else {
            Log.infof("Successfully retrieved %d customers.", customers.size());
            return Response.ok(customers).build();
        }
    }

    @Path("/{vat}")
    @GET
    public Response getCustomerByVat(@PathParam("vat") String vat) {
        Optional<CustomerDTO> customer = customerService.findCustomerByVat(vat);

        if (customer.isPresent()) {
            Log.infof("Customer found with VAT: %s", vat);
            return Response.ok(customer.get()).build();
        } else {
            Log.warnf("No customer found with VAT: %s", vat);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Customer not found with VAT: " + vat).build();
        }
    }

    @Path("/customer")
    @POST
    public Response saveCustomer(CustomerDTO customerDTO) {
        Log.info("Trying to create new customer");

        if (customerDTO == null || customerDTO.getVat() == null) {
            Log.warn("Customer data or VAT number is missing.");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Customer data or VAT number is missing.")
                    .build();
        }

        try {
            ValidateVatResponse vatResponse = validateService.checkVat("EL", customerDTO.getVat());

            if (customerService.findCustomerByVat(customerDTO.getVat()).isPresent()) {
                Log.infof("Customer with VAT {} already exists.", customerDTO.getVat());
                return Response.status(Response.Status.CONFLICT)
                        .entity("Customer with this VAT already exists.")
                        .build();
            }

            if (!vatResponse.isValid()) {
                Log.errorf("Invalid VAT number: {}", customerDTO.getVat());
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Invalid VAT number.")
                        .build();
            }
            customerService.saveCustomerEntity(customerDTO);
            Log.infof("Customer created successfully with VAT: {}", customerDTO.getVat());
            return Response.status(Response.Status.CREATED)
                    .entity("Customer saved successfully.")
                    .build();

        } catch (Exception e) {
            Log.error("Error while saving customer: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while saving the customer.")
                    .build();
        }
    }

}
