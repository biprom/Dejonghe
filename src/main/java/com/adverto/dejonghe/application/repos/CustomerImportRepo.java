package com.adverto.dejonghe.application.repos;

import com.adverto.dejonghe.application.entities.customers.Customer;
import com.adverto.dejonghe.application.entities.customers.CustomerImport;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomerImportRepo extends MongoRepository<CustomerImport, String> {
}
