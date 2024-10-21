package org.acme.mapper;

import org.acme.dto.CustomerDTO;
import org.acme.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 *
 * @author stavroulabakogianni
 */

@Mapper(componentModel = "cdi")
public interface CustomerMapper {

    CustomerDTO customerToDTO(Customer customer);

    Customer customerDTOToEntity(CustomerDTO customerDTO);
}
