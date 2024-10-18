
package org.acme.repository;

import static io.quarkus.arc.ComponentsProvider.LOG;
import org.acme.entity.Customer;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CustomerRepository implements PanacheRepository<Customer> {
    
     public Optional<Customer> findByVat(String vat) {
        try {
            Optional<Customer> customer = find("vat", vat).firstResultOptional();

            if (customer.isPresent()) {
                LOG.info(String.format("Customer retrieved successfully with VAT: %s", vat));
            } else {
                LOG.warn(String.format("No customer found with VAT: %s", vat));
            }
            return customer;
        } catch (Exception e) {
            LOG.error(String.format("Error retrieving customer with VAT: %s - %s", vat, e.getMessage()), e);
            return Optional.empty();
        }
    }
}

   
