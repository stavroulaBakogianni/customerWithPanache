package org.acme.service;

import java.util.List;
import java.util.Optional;
import org.acme.dto.CustomerDTO;

public interface CustomerService {
    
    Optional<CustomerDTO> saveCustomerEntity(CustomerDTO customerDto);
    Optional<CustomerDTO> updateCustomer(String vat, CustomerDTO updatedCustomerDto);
    boolean deleteCustomer(String vat);
    List<CustomerDTO> findAllCustomers();
    Optional<CustomerDTO> findCustomerByVat(String vat);
}
