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
    @Transactional
    public Optional<CustomerDTO> updateCustomer(String vat, CustomerDTO updatedCustomerDto) {
        try {
            Optional<Customer> existingCustomerOpt = customerRepository.findByVat(vat);

            if (existingCustomerOpt.isEmpty()) {
                Log.warnf("Customer with VAT %s not found. Update aborted.", vat);
                return Optional.empty();
            }

            Customer existingCustomer = existingCustomerOpt.get();

            existingCustomer.setFirstName(updatedCustomerDto.getFirstName());
            existingCustomer.setLastName(updatedCustomerDto.getLastName());
            existingCustomer.setEmail(updatedCustomerDto.getEmail());
            existingCustomer.setMobilePhone(updatedCustomerDto.getMobilePhone());

            customerRepository.persist(existingCustomer);

            Log.infof("Customer updated successfully with VAT: %s", vat);
            return Optional.of(customerMapper.customerToDTO(existingCustomer));
        } catch (Exception e) {
            Log.errorf(e, "Error updating customer with VAT: %s", vat);
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public boolean deleteCustomer(String vat) {
        try {
            Optional<Customer> customerOpt = customerRepository.findByVat(vat);

            if (customerOpt.isPresent()) {
                customerRepository.delete(customerOpt.get());
                Log.infof("Customer deleted successfully with VAT: %s", vat);
                return true;
            } else {
                Log.warnf("Customer with VAT %s not found. Delete aborted.", vat);
                return false;
            }
        } catch (Exception e) {
            Log.errorf(e, "Error deleting customer with VAT: %s", vat);
            return false;
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
