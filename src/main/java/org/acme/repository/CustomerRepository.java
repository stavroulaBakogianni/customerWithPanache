package org.acme.repository;

import org.acme.entity.Customer;
import io.quarkus.hibernate.orm.panache.PanacheRepository; //use PanacheRepository from Quarkus
import io.quarkus.logging.Log; //use Quarkus logging

import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CustomerRepository implements PanacheRepository<Customer> {

    public Optional<Customer> findByVat(String vat) {
        try {
            Optional<Customer> customer = find("vat", vat).firstResultOptional();

            if (customer.isPresent()) {
                Log.infof("Customer retrieved successfully with VAT: %s", vat);
            } else {
                Log.warnf("No customer found with VAT: %s", vat);
            }
            return customer;
        } catch (Exception e) {
            Log.errorf(e, "Error retrieving customer with VAT: %s", vat);
            return Optional.empty();
        }
    }
}
