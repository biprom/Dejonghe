package com.adverto.dejonghe.application.repos;

import com.adverto.dejonghe.application.entities.customers.Address;
import com.adverto.dejonghe.application.entities.customers.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CustomerRepo extends MongoRepository<Customer, String> {
    List<Customer> findByNameContainingIgnoreCaseOrVatNumberContainingIgnoreCaseOrAddresses_addressNameContainingIgnoreCase(String nameFilter,String vatFilter,String addressFilter);
    List<Customer> findByAddresses_streetContainingIgnoreCase(String street);
    List<Customer> findByAddresses_CityAndAddresses_StreetAndAddresses_Number(String city, String street, String number);
}
