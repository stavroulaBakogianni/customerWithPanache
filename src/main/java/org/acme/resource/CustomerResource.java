package org.acme.resource;

import io.quarkus.logging.Log;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
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
public class CustomerResource {

    @Inject
    CustomerService customerService;

    @Inject
    @RestClient
    ValidateService validateService;

    @POST
    @Path("/test/customer")
    public Response saveTestCustomer(@Valid CustomerDTO customerDto) {

        if (customerDto.getVat() == null) {
            Log.error("VAT is null in the incoming CustomerDTO");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "VAT cannot be null"))
                    .build();
        }

        Optional<CustomerDTO> savedCustomer = customerService.saveCustomerEntity(customerDto);

        if (savedCustomer.isPresent()) {
            Log.infof("Customer saved successfully with VAT: %s", customerDto.getVat());
            return Response.ok(Map.of("message", "Customer saved successfully", "customer", savedCustomer.get()))
                    .build();
        } else {
            Log.errorf("Failed to save customer with VAT: %s", customerDto.getVat());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error saving customer"))
                    .build();
        }

    }

    @GET
    @Path("/customers")
    public Response getAllCustomers() {

        List<CustomerDTO> customers = customerService.findAllCustomers();

        if (customers.isEmpty()) {
            Log.warn("No customers found.");
            return Response.ok(Map.of("message", "No customers found", "customers", customers))
                    .build();
        } else {
            Log.infof("Successfully retrieved %d customers.", customers.size());
            return Response.ok(Map.of("message", "Successfully retrieved customers", "customers", customers))
                    .build();
        }
    }

    @PUT
    @Path("/customers/{vat}")
    public Response updateCustomer(@PathParam("vat") String vat, CustomerDTO updatedCustomerDto) {
        try {
            Optional<CustomerDTO> updatedCustomerOpt = customerService.updateCustomer(vat, updatedCustomerDto);

            if (updatedCustomerOpt.isPresent()) {
                Log.infof("Customer with VAT %s updated successfully.", vat);
                return Response.ok(Map.of("message", "Customer updated successfully", "customer", updatedCustomerOpt.get()))
                        .build();
            } else {
                Log.warnf("Customer with VAT %s not found.", vat);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Customer with VAT " + vat + " not found."))
                        .build();
            }
        } catch (Exception e) {
            Log.errorf(e, "Error updating customer with VAT: %s", vat);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "An error occurred while updating the customer."))
                    .build();
        }
    }

    @GET
    @Path("/{vat}")
    public Response getCustomerByVat(@PathParam("vat") String vat) {
        Optional<CustomerDTO> customer = customerService.findCustomerByVat(vat);

        if (customer.isPresent()) {
            Log.infof("Customer found with VAT: %s", vat);
            return Response.ok(Map.of("message", "Customer found", "customer", customer.get()))
                    .build();
        } else {
            Log.warnf("No customer found with VAT: %s", vat);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Customer not found with VAT: " + vat))
                    .build();
        }
    }

    @DELETE
    @Path("/{vat}")
    public Response deleteCustomer(@PathParam("vat") String vat) {
        try {
            boolean deleted = customerService.deleteCustomer(vat);

            if (deleted) {
                String successMessage = String.format("Customer with VAT %s deleted successfully.", vat);
                Log.info(successMessage);
                return Response.ok(Map.of("message", successMessage))
                        .build();
            } else {
                String notFoundMessage = String.format("Customer with VAT %s not found.", vat);
                Log.warn(notFoundMessage);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", notFoundMessage))
                        .build();
            }
        } catch (Exception e) {
            String errorMessage = String.format("Error deleting customer with VAT: %s", vat);
            Log.errorf(e, errorMessage);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "An error occurred while deleting the customer."))
                    .build();
        }
    }

    @POST
    @Path("/customer")
    public Response saveCustomer(CustomerDTO customerDTO) {
        Log.info("Trying to create new customer");

        if (customerDTO == null || customerDTO.getVat() == null) {
            Log.warn("Customer data or VAT number is missing.");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Customer data or VAT number is missing."))
                    .build();
        }

        try {
            ValidateVatResponse vatResponse = validateService.checkVat("EL", customerDTO.getVat());

            if (customerService.findCustomerByVat(customerDTO.getVat()).isPresent()) {
                Log.infof("Customer with VAT {} already exists.", customerDTO.getVat());
                return Response.status(Response.Status.CONFLICT)
                        .entity(Map.of("error", "Customer with this VAT already exists."))
                        .build();
            }

            if (!vatResponse.isValid()) {
                Log.errorf("Invalid VAT number: {}", customerDTO.getVat());
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Invalid VAT number."))
                        .build();
            }
            customerService.saveCustomerEntity(customerDTO);
            Log.infof("Customer created successfully with VAT: {}", customerDTO.getVat());
            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("message", "Customer saved successfully."))
                    .build();

        } catch (Exception e) {
            Log.error("Error while saving customer: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "An error occurred while saving the customer."))
                    .build();
        }
    }

}
