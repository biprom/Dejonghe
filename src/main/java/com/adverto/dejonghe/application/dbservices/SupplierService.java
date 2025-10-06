package com.adverto.dejonghe.application.dbservices;

import com.adverto.dejonghe.application.entities.customers.Customer;
import com.adverto.dejonghe.application.entities.product.product.Product;
import com.adverto.dejonghe.application.entities.product.product.Supplier;
import com.adverto.dejonghe.application.repos.ProductRepo;
import com.adverto.dejonghe.application.repos.SupplierRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SupplierService {
    @Autowired
    SupplierRepo supplierRepo;

    public Optional<List<Supplier>> getAllSuppliers() {
        List<Supplier> suppliers = supplierRepo.findAll();
        if (!suppliers.isEmpty()) {
            return Optional.of(suppliers);
        }
        else{
            return Optional.empty();
        }
    }

    public void delete(Supplier supplier) {
        supplierRepo.delete(supplier);
    }

    public void save(Supplier newSupplier) {
        if (newSupplier.getName() != null) {
            supplierRepo.save(newSupplier);
        }
    }
}
