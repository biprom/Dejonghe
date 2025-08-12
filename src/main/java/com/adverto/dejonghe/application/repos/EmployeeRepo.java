package com.adverto.dejonghe.application.repos;


import com.adverto.dejonghe.application.entities.employee.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EmployeeRepo extends MongoRepository<Employee, String> {
}
