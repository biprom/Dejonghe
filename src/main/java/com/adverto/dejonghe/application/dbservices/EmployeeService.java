package com.adverto.dejonghe.application.dbservices;

import com.adverto.dejonghe.application.entities.employee.Employee;
import com.adverto.dejonghe.application.entities.product.product.Supplier;
import com.adverto.dejonghe.application.repos.EmployeeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {
    @Autowired
    EmployeeRepo employeeRepo;

    public Optional<List<Employee>> getAll() {
        List<Employee> employees = employeeRepo.findAll();
        if (!employees.isEmpty()) {
            return Optional.of(employees);
        }
        else{
            return Optional.empty();
        }
    }

    public void delete(Employee employee) {
        employeeRepo.delete(employee);
    }

    public void save(Employee employee) {
        if (employee.getAbbreviation() != null) {
            employeeRepo.save(employee);
        }
    }
}
