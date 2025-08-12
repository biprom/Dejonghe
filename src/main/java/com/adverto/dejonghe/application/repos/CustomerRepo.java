package com.adverto.dejonghe.application.repos;

import com.adverto.dejonghe.application.entities.customers.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CustomerRepo extends MongoRepository<Customer, String> {
    List<Customer> findByNameContainingIgnoreCaseOrVatNumberContainingIgnoreCase(String nameFilter,String vatFilter);
    List<Customer> findByAddresses_streetContainingIgnoreCase(String street);
}
