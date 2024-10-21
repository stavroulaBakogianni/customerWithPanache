package org.acme.service;

import io.quarkus.logging.Log;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
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
    @Transactional
    public Optional<CustomerDTO> saveCustomerEntity(CustomerDTO customerDto) {

        if (customerDto.getVat() == null || customerDto.getVat().isEmpty()) {
            Log.error("VAT is null or empty. Cannot save customer.");
            return Optional.empty();
        }

        try {

            Customer customer = customerMapper.customerDTOToEntity(customerDto);

            customerRepository.persist(customer);

            Log.infof("Customer saved successfully with VAT: %s", customer.getVat());
            return Optional.of(customerMapper.customerToDTO(customer));
        } catch (Exception e) {
            Log.errorf(e, "Error saving customer with VAT: %s", customerDto.getVat());
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
            Log.errorf(e, "Error retrieving customers");
            return List.of();
        }
    }

    @Override
    public Optional<CustomerDTO> findCustomerByVat(String vat) {
        try {
            return customerRepository.findByVat(vat)
                    .map(customerMapper::customerToDTO);
        } catch (Exception e) {
            Log.errorf(e, "Error retrieving customer with VAT: %s", vat);
            return Optional.empty();
        }
    }
}
