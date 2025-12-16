package com.adverto.dejonghe.application.tests;

import com.adverto.dejonghe.application.Controllers.GoogleRestController;
import com.adverto.dejonghe.application.dbservices.CustomerService;
import com.adverto.dejonghe.application.dbservices.ProductService;
import com.adverto.dejonghe.application.entities.customers.Address;
import com.adverto.dejonghe.application.entities.customers.Coordinates;
import com.adverto.dejonghe.application.entities.customers.Customer;
import com.adverto.dejonghe.application.entities.customers.CustomerImport;
import com.adverto.dejonghe.application.entities.product.product.Product;
import com.adverto.dejonghe.application.repos.CustomerImportRepo;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootTest
class Tests{

    CustomerImportRepo customerImportRepo;
    CustomerService customerService;
    GoogleRestController googleRestController;
    @Autowired
    private ProductService productService;

    @Autowired
    public Tests(CustomerImportRepo customerImportRepo,
                 CustomerService customerService,
                 GoogleRestController googleRestController) {
        this.customerImportRepo = customerImportRepo;
        this.customerService = customerService;
        this.googleRestController = googleRestController;
    }

    @Test
    void copyCommenArtNumbersPurchasePrice() {
        List<Product>productList = productService.getAllProducts().get();
        int i = 0;
        for(Product product:productList){
            if((product.getProductCode() != null) && (product.getProductCode().length() > 0)){
                Optional<List<Product>>commonProductList = productService.findByProductCodeEqualCaseInsensitive(product.getProductCode().toLowerCase());
                if((commonProductList.get() != null) && (commonProductList.get().size() > 0)){
                    Optional<Double> maxPurchasePrice = commonProductList.get().stream().filter(item -> item.getPurchasePrice() != null).map(item -> item.getPurchasePrice()).max(Double::compareTo);
                    Optional<Product> optInternalName = commonProductList.get().stream().filter(item -> (item.getInternalName() != null) && (item.getInternalName().length() > 0)).findFirst();
                    Optional<Product> optComment = commonProductList.get().stream().filter(item -> (item.getComment() != null) && (item.getComment().length() > 0)).findFirst();
                    if(maxPurchasePrice.isPresent()){
                        for(Product commonProduct:commonProductList.get()){
                            commonProduct.setPurchasePrice(maxPurchasePrice.get());
                            if(commonProduct.getSellMargin() != null){
                                commonProduct.setSellPrice(commonProduct.getSellMargin()*commonProduct.getPurchasePrice());
                            }
                            if(optInternalName.isPresent()){
                                commonProduct.setInternalName(optInternalName.get().getInternalName());
                            }
                            if(optComment.isPresent()){
                                commonProduct.setComment(optComment.get().getComment());
                            }
                            productService.save(commonProduct);
                            System.out.println(i + " " +commonProduct.getProductCode() + " " + commonProduct.getPurchasePrice() + " " + commonProduct.getSellMargin() + " " + commonProduct.getSellPrice());
                        }
                    }
                }
                }
            }
        }


    //@Test
    void addCustomersImportToCustomers() {
        List<CustomerImport> customerImportList = customerImportRepo.findAll();
        for (CustomerImport customerImport : customerImportList) {
            Customer customer = new Customer();
            customer.setName(customerImport.getName());
            customer.setVatNumber(customerImport.getVatNumber());
            customer.setAlert(false);
            customer.setBAgro(false);
            customer.setBIndustry(false);

            Address address = new Address();
            address.setInvoiceAddress(true);
            address.setZip(customerImport.getZip());
            address.setCity(customerImport.getCity());
            address.setCountry(customerImport.getCountry());
            address.setStreet(customerImport.getStreet());
            List<Address> addressList = new ArrayList<>();
            addressList.add(address);

            customer.setAddresses(addressList);

            customerService.save(customer);

        }
    }

    //@Test
    void setAllDistancesToCustomers() throws JSONException {
        Optional<List<Customer>> optCustomerList = customerService.getAllCustomers();
        if (optCustomerList.isPresent()) {
            for (Customer customer : optCustomerList.get()) {
                for (Address address : customer.getAddresses()) {
                    //add Distance x2
                    Optional<Double> optDistance = googleRestController.getOptDistanceforAdres(customer.getAddresses().get(0));
                    if (optDistance.isPresent()) {
                        address.setDistance(2*optDistance.get());
                    }
                    //add Coordinates
                    Optional<Coordinates> optCoordinates = googleRestController.getOptCoordinatesforAdres(customer.getAddresses().get(0));
                    if (optCoordinates.isPresent()) {
                        address.setCoordinates(optCoordinates.get());
                    }
                }
                customerService.save(customer);
            }
        }
    }
}