package com.adverto.dejonghe.application.dbservices;

import com.adverto.dejonghe.application.entities.customers.Address;
import com.adverto.dejonghe.application.entities.customers.Customer;
import com.adverto.dejonghe.application.entities.product.product.Product;
import com.adverto.dejonghe.application.entities.product.product.Supplier;
import com.adverto.dejonghe.application.repos.CustomerRepo;
import com.adverto.dejonghe.application.repos.SupplierRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    @Autowired
    CustomerRepo customerRepo;

    List<Address>addressList = new ArrayList<>();

    public void save(Customer sampleCustomer) {
        if (sampleCustomer.getName() != null) {
            customerRepo.save(sampleCustomer);
        }
    }

    public Optional<List<Customer>> getAllCustomers() {
        List<Customer> customers = customerRepo.findAll();
        if (!customers.isEmpty()) {
            return Optional.of(customers);
        }
        else{
            return Optional.empty();
        }
    }

    public Optional<List<Customer>> getCustomerByNameOrVat(String filter) {
       return Optional.of(customerRepo.findByNameContainingIgnoreCaseOrVatNumberContainingIgnoreCase(filter,filter));
    }

    public void delete(Customer customer) {
        customerRepo.delete(customer);
    }

    public Optional<List<Address>> getAllCustomerAdresses() {
        List<Customer> customers = customerRepo.findAll();
        addressList.clear();
        if (!customers.isEmpty()) {
            for(Customer customer : customers) {
                customer.getAddresses().stream().filter(address -> (address.getInvoiceAddress() == null) ||  (address.getInvoiceAddress() != true)).forEach(address -> {
                    if((address.getAddressName() != null) && (address.getAddressName().length() > 0)) {
                        addressList.add(address);
                    }
                    else{
                        address.setAddressName(customer.getName());
                        addressList.add(address);
                    }
                });
            }
            return Optional.of(addressList);
        }
        else{
            return Optional.empty();
        }
    }

    public Optional<List<Customer>> getCustomersByStreet(String street) {
        return Optional.of(customerRepo.findByAddresses_streetContainingIgnoreCase(street));
    }
}
