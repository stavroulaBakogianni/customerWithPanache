package org.acme.service;

import java.util.List;
import java.util.Optional;
import org.acme.dto.CustomerDTO;

public interface CustomerService {
    
    Optional<CustomerDTO> saveCustomerEntity(CustomerDTO customerDto);
    List<CustomerDTO> findAllCustomers();
    Optional<CustomerDTO> findCustomerByVat(String vat);
}
