package org.acme.service;

import static io.quarkus.arc.ComponentsProvider.LOG;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.acme.dto.CustomerDTO;
import org.acme.entity.Customer;
import org.acme.mapper.CustomerMapper;
import org.acme.repository.CustomerRepository;

/**
 *
 * @author stavroulabakogianni
 */
    
@ApplicationScoped
public class CustomerServiceImpl implements CustomerService {

    @Inject
    CustomerRepository customerRepository;

    @Inject
    CustomerMapper customerMapper;

    @Override
    public Optional<CustomerDTO> saveCustomerEntity(CustomerDTO customerDto) {
        try {
            Customer customer = customerMapper.customerDTOToEntity(customerDto);
            customerRepository.persist(customer);
            LOG.info("Customer saved successfully with VAT:" + customer.getVat()); // Logging
            return Optional.of(customerMapper.customerToDTO(customer));
        } catch (Exception e) {
            LOG.error("Error saving customer: {}", e.getMessage(), e); // Logging σφάλματος
            return Optional.empty();
        }
    }

    @Override
    public List<CustomerDTO> findAllCustomers() {
        try {
            return customerRepository.listAll().stream()
                    .map(customerMapper::customerToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error("Error retrieving customers: {}", e.getMessage(), e);
            return List.of(); 
        }
    }

    @Override
    public Optional<CustomerDTO> findCustomerByVat(String vat) {
        try {
            return customerRepository.findByVat(vat)
                    .map(customerMapper::customerToDTO);
        } catch (Exception e) {
            LOG.error("Error retrieving customer with VAT {}: {}"+ vat + e.getMessage(), e);
            return Optional.empty();
        }
    }
}
