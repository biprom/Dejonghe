package com.adverto.dejonghe.application.services.invoice;

import com.adverto.dejonghe.application.dbservices.CustomerService;
import com.adverto.dejonghe.application.dbservices.InvoiceService;
import com.adverto.dejonghe.application.dbservices.ProductService;
import com.adverto.dejonghe.application.entities.WorkOrder.WorkOrder;
import com.adverto.dejonghe.application.entities.WorkOrder.WorkOrderHeader;
import com.adverto.dejonghe.application.entities.WorkOrder.WorkOrderTime;
import com.adverto.dejonghe.application.entities.customers.Address;
import com.adverto.dejonghe.application.entities.customers.Customer;
import com.adverto.dejonghe.application.entities.enums.fleet.Fleet;
import com.adverto.dejonghe.application.entities.enums.fleet.FleetWorkType;
import com.adverto.dejonghe.application.entities.enums.invoice.InvoiceStatus;
import com.adverto.dejonghe.application.entities.enums.product.VAT;
import com.adverto.dejonghe.application.entities.enums.workorder.WorkLocation;
import com.adverto.dejonghe.application.entities.enums.workorder.WorkType;
import com.adverto.dejonghe.application.entities.invoice.Invoice;
import com.adverto.dejonghe.application.entities.product.product.Product;
import com.adverto.dejonghe.application.implementations.DataImplementation;
import com.vaadin.flow.component.notification.Notification;
import net.sf.jasperreports.engine.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InvoiceServices {

    @Autowired
    InvoiceService invoiceService;
    @Autowired
    CustomerService customerService;
    @Autowired
    ProductService productService;

    @Value("${rootTemplateInvoice}")
    FileSystemResource invoiceResourceJRXML;
    @Value( "${rootFolder}" )
    private String rootFolder;

    Map<String, Object> parameters = new HashMap<>();

    public Integer getNewInvoiceNumber() {
        Optional<Invoice> optionalInvoice = invoiceService.getLastInvoice();
        if(!optionalInvoice.isEmpty()){
            return Integer.valueOf(optionalInvoice.get().getInvoiceNumber()+1);
        }
        else{
            return 250001;
        }
    }

    public Invoice generateMergedInvoice(Set<WorkOrder> workOrderSet){
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(getNewInvoiceNumber());
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setInvoiceStatus(InvoiceStatus.PROFORMA);

        Address workAddress = workOrderSet.stream().findFirst().get().getWorkAddress();
        invoice.setWorkAddress(workAddress);

        Optional<List<Customer>> optCustomer = customerService.getCustomerByWorkAddress(workAddress);
        if(!optCustomer.isEmpty()){
            if(optCustomer.get().size() >= 2){
                Notification.show("Er zijn meerdere klanten met hetzelfde Werkadres");
            }
            invoice.setCustomer(optCustomer.get().get(0));

            List<String> allFotoIds = workOrderSet.stream()
                    .map(WorkOrder::getImageList)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            invoice.setImageList(allFotoIds);

            invoice.setWorkOrderList(workOrderSet);

            List<Product> allProducts = workOrderSet.stream()
                    .map(WorkOrder::getProductList)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            long generalHoursOnTheMove = 0L;
            long programHoursOnTheMove = 0L;
            long centrifugeHoursOnTheMove = 0L;

            long generalHoursLocal = 0L;
            long programHoursLocal = 0L;
            long centrifugeHoursLocal = 0L;


            //Calculate workhours
            for(WorkOrder workOrder : workOrderSet){
                int i = 0;
                for(WorkOrderHeader workOrderHeader : workOrder.getWorkOrderHeaderList()){

                    int numberOfTechnicians = 0;

                    if((workOrder.getWorkOrderHeaderList() != null) && (workOrder.getWorkOrderHeaderList().size() > 0) && (workOrderHeader.getWorkOrderTimeList() != null)){
                        if(i == 0){
                            numberOfTechnicians = numberOfTechnicians + 1 + workOrder.getExtraEmployeesTeam1().size();
                        }
                        if(i == 1){
                            numberOfTechnicians = numberOfTechnicians + 1 + workOrder.getExtraEmployeesTeam2().size();
                        }
                        if(i == 2){
                            numberOfTechnicians = numberOfTechnicians + 1+ workOrder.getExtraEmployeesTeam3().size();
                        }
                        if(i == 3){
                            numberOfTechnicians = numberOfTechnicians+ 1 + workOrder.getExtraEmployeesTeam4().size();
                        }
                        if((workOrder.getWorkLocation().equals(WorkLocation.ON_THE_MOVE)) && (workOrderHeader.getWorkType() == WorkType.GENERAL)){
                            for(WorkOrderTime workOrderTime : workOrderHeader.getWorkOrderTimeList()){
                                generalHoursOnTheMove = generalHoursOnTheMove + numberOfTechnicians * (Duration.between(workOrderTime.getTimeUp(), workOrderTime.getTimeDown()).toHours());
                            }
                        }
                        if((workOrder.getWorkLocation().equals(WorkLocation.ON_THE_MOVE) && (workOrderHeader.getWorkType()) == WorkType.CENTRIFUGE)){
                            for(WorkOrderTime workOrderTime : workOrderHeader.getWorkOrderTimeList()){
                                centrifugeHoursOnTheMove = centrifugeHoursOnTheMove + numberOfTechnicians * (Duration.between(workOrderTime.getTimeUp(), workOrderTime.getTimeDown()).toHours());
                            }
                        }
                        if((workOrder.getWorkLocation().equals(WorkLocation.ON_THE_MOVE) && (workOrderHeader.getWorkType()) == WorkType.PROGRAMMATIC)){
                            for(WorkOrderTime workOrderTime : workOrderHeader.getWorkOrderTimeList()){
                                programHoursOnTheMove = programHoursOnTheMove + numberOfTechnicians * (Duration.between(workOrderTime.getTimeUp(), workOrderTime.getTimeDown()).toHours());
                            }
                        }
                        if((workOrder.getWorkLocation().equals(WorkLocation.WORKPLACE)) && (workOrderHeader.getWorkType() == WorkType.GENERAL)){
                            for(WorkOrderTime workOrderTime : workOrderHeader.getWorkOrderTimeList()){
                                generalHoursLocal = generalHoursLocal + (numberOfTechnicians * (Duration.between(workOrderTime.getTimeStart(), workOrderTime.getTimeStop()).toHours()));
                            }
                        }
                        if((workOrder.getWorkLocation().equals(WorkLocation.WORKPLACE) && (workOrderHeader.getWorkType()) == WorkType.CENTRIFUGE)){
                            for(WorkOrderTime workOrderTime : workOrderHeader.getWorkOrderTimeList()){
                                centrifugeHoursLocal = centrifugeHoursLocal + numberOfTechnicians * (Duration.between(workOrderTime.getTimeStart(), workOrderTime.getTimeStop()).toHours());
                            }
                        }
                        if((workOrder.getWorkLocation().equals(WorkLocation.WORKPLACE) && (workOrderHeader.getWorkType()) == WorkType.PROGRAMMATIC)){
                            for(WorkOrderTime workOrderTime : workOrderHeader.getWorkOrderTimeList()){
                                programHoursLocal = programHoursLocal + numberOfTechnicians * (Duration.between(workOrderTime.getTimeStart(), workOrderTime.getTimeStop()).toHours());
                            }
                        }
                        i++;
                    }
                }
            }


            if(optCustomer.get().getFirst().getBIndustry() == true){

                if(generalHoursLocal > 0){
                    Product workHourRegularLocalIndustrieProduct = productService.getWorkhourForRegularLocalIndustrie().get();
                    workHourRegularLocalIndustrieProduct.setSelectedAmount(Double.valueOf(generalHoursLocal));
                    allProducts.add(workHourRegularLocalIndustrieProduct);
                }

                if(centrifugeHoursLocal > 0){
                    Product workHourCentrifugeLocalIndustrieProduct = productService.getWorkhourForCentrifugeLocalIndustrie().get();
                    workHourCentrifugeLocalIndustrieProduct.setSelectedAmount(Double.valueOf(centrifugeHoursLocal));
                    allProducts.add(workHourCentrifugeLocalIndustrieProduct);
                }

                if(programHoursLocal > 0){
                    Product workHourProgramLocalIndustrieProduct = productService.getWorkhourForProgammationLocalIndustrie().get();
                    workHourProgramLocalIndustrieProduct.setSelectedAmount(Double.valueOf(programHoursLocal));
                    allProducts.add(workHourProgramLocalIndustrieProduct);
                }

                if(generalHoursOnTheMove > 0){
                    Product workHourRegularOnTheMoveIndustrieProduct = productService.getWorkhourForOnRegularTheMoveIndustrie().get();
                    workHourRegularOnTheMoveIndustrieProduct.setSelectedAmount(Double.valueOf(generalHoursOnTheMove));
                    allProducts.add(workHourRegularOnTheMoveIndustrieProduct);
                }

                if(centrifugeHoursOnTheMove > 0){
                    Product workHourCentrifugeOnTheMoveIndustrieProduct = productService.getWorkhourForOnCentrifugeTheMoveIndustrie().get();
                    workHourCentrifugeOnTheMoveIndustrieProduct.setSelectedAmount(Double.valueOf(centrifugeHoursOnTheMove));
                    allProducts.add(workHourCentrifugeOnTheMoveIndustrieProduct);
                }

                if(programHoursOnTheMove > 0){
                    Product workHourProgramOnTheMoveIndustrieProduct = productService.getWorkhourForOnProgammationTheMoveIndustrie().get();
                    workHourProgramOnTheMoveIndustrieProduct.setSelectedAmount(Double.valueOf(programHoursOnTheMove));
                    allProducts.add(workHourProgramOnTheMoveIndustrieProduct);
                }

            }

            else{
                if(generalHoursLocal > 0){
                    Product workHourRegularLocalAgroProduct = productService.getWorkhourForRegularLocalAgro().get();
                    workHourRegularLocalAgroProduct.setSelectedAmount(Double.valueOf(generalHoursLocal));
                    allProducts.add(workHourRegularLocalAgroProduct);
                }

                if(centrifugeHoursLocal > 0){
                    Product workHourCentrifugeLocalAgroProduct = productService.getWorkhourForCentrifugeLocalAgro().get();
                    workHourCentrifugeLocalAgroProduct.setSelectedAmount(Double.valueOf(centrifugeHoursLocal));
                    allProducts.add(workHourCentrifugeLocalAgroProduct);
                }

                if(programHoursLocal > 0){
                    Product workHourProgramLocalAgroProduct = productService.getWorkhourForProgammationLocalAgro().get();
                    workHourProgramLocalAgroProduct.setSelectedAmount(Double.valueOf(programHoursLocal));
                    allProducts.add(workHourProgramLocalAgroProduct);
                }
                if(generalHoursOnTheMove > 0){
                    Product workHourRegularOnTheMoveAgroProduct = productService.getWorkhourForRegularOnTheMoveAgro().get();
                    workHourRegularOnTheMoveAgroProduct.setSelectedAmount(Double.valueOf(generalHoursOnTheMove));
                    allProducts.add(workHourRegularOnTheMoveAgroProduct);
                }

                if(centrifugeHoursOnTheMove > 0){
                    Product workHourCentrifugeOnTheMoveAgroProduct = productService.getWorkhourForCentrifugeOnTheMoveAgro().get();
                    workHourCentrifugeOnTheMoveAgroProduct.setSelectedAmount(Double.valueOf(centrifugeHoursOnTheMove));
                    allProducts.add(workHourCentrifugeOnTheMoveAgroProduct);
                }

                if(programHoursOnTheMove > 0){
                    Product workHourProgramOnTheMoveAgroProduct = productService.getWorkhourForProgammationOnTheMoveAgro().get();
                    workHourProgramOnTheMoveAgroProduct.setSelectedAmount(Double.valueOf(programHoursOnTheMove));
                    allProducts.add(workHourProgramOnTheMoveAgroProduct);
                }
            }

            // add movement to Proforma
            Double amountKmRegular = 0.0;
            Double amountKmTrailer = 0.0;
            Double amountKmCrane = 0.0;
            Double amountHoursCraneRegular = 0.0;
            Double amountHoursCraneIntens = 0.0;
            Integer amountForfait = 0;

            for(WorkOrder workOrder : workOrderSet) {
                for (WorkOrderHeader workOrderHeader : workOrder.getWorkOrderHeaderList()) {
                    if(((workOrderHeader.getFleet() != null) && (workOrderHeader.getFleet().equals(Fleet.VAN))) ||
                            ((workOrderHeader.getFleet() != null) && (workOrderHeader.getFleet().equals(Fleet.ATEGO)))){
                        amountKmRegular = amountKmRegular + (workAddress.getDistance());
                    }
                    if((workOrderHeader.getFleet() != null) && (workOrderHeader.getFleet().equals(Fleet.TRUCK_TRAILER))){
                        amountKmTrailer = amountKmTrailer + (workAddress.getDistance());
                    }
                    if((workOrderHeader.getFleet() != null) && (workOrderHeader.getFleet().equals(Fleet.TRUCK_CRANE))){
                        amountKmCrane = amountKmCrane + (workAddress.getDistance());
                        if(workOrderHeader.getFleetWorkType().equals(FleetWorkType.DELIVERY)){
                            amountForfait = ++amountForfait;
                        }
                    }
                    if((workOrderHeader.getFleetHours() != null) && (workOrderHeader.getFleetHours() >= 0.0)){
                        if(workOrderHeader.getFleetWorkType().equals(FleetWorkType.REGULAR)){
                            amountHoursCraneRegular = amountHoursCraneRegular + workOrderHeader.getFleetHours();
                        }
                        if(workOrderHeader.getFleetWorkType().equals(FleetWorkType.INTENS)){
                            amountHoursCraneIntens = amountHoursCraneIntens + workOrderHeader.getFleetHours();
                        }
                    }
                }
            }

            if(optCustomer.get().getFirst().getBIndustry() == true){
                if(amountKmRegular > 0.0){
                    Product regularKm = productService.getRegularKmIndustry().get();
                    regularKm.setSelectedAmount(amountKmRegular);
                    allProducts.add(regularKm);
                }
                if(amountKmTrailer > 0.0){
                    Product trailerKm = productService.getRegularTrailerIndustry().get();
                    trailerKm.setSelectedAmount(amountKmTrailer);
                    allProducts.add(trailerKm);
                }
                if(amountKmCrane > 0.0){
                    Product craneKm = productService.getRegularCraneIndustry().get();
                    craneKm.setSelectedAmount(amountKmCrane);
                    allProducts.add(craneKm);
                }
                if(amountHoursCraneRegular > 0.0){
                    Product hoursCraneRegularIndustry = productService.getWorkHoursCraneRegularIndustry().get();
                    hoursCraneRegularIndustry.setSelectedAmount(amountHoursCraneRegular);
                    allProducts.add(hoursCraneRegularIndustry);
                }
                if(amountHoursCraneIntens > 0.0){
                    Product hoursCraneIntenseIndustry = productService.getWorkHoursCraneIntenseIndustry().get();
                    hoursCraneIntenseIndustry.setSelectedAmount(amountHoursCraneIntens);
                    allProducts.add(hoursCraneIntenseIndustry);
                }
                if(amountForfait > 0){
                    Product hoursCraneForfaitIndustry = productService.getWorkHoursCraneForfaitIndustry().get();
                    hoursCraneForfaitIndustry.setSelectedAmount(Double.valueOf(amountForfait));
                    allProducts.add(hoursCraneForfaitIndustry);
                }
            }
            else{
                if(amountKmRegular > 0.0){
                    Product regularKm = productService.getRegularKmAgro().get();
                    regularKm.setSelectedAmount(amountKmRegular);
                    allProducts.add(regularKm);
                }
                if(amountKmTrailer > 0.0){
                    Product trailerKm = productService.getRegularTrailerAgro().get();
                    trailerKm.setSelectedAmount(amountKmTrailer);
                    allProducts.add(trailerKm);
                }
                if(amountKmCrane > 0.0){
                    Product craneKm = productService.getRegularCraneAgro().get();
                    craneKm.setSelectedAmount(amountKmCrane);
                    allProducts.add(craneKm);
                }
                if(amountHoursCraneRegular > 0.0){
                    Product hoursCraneRegularIndustry = productService.getWorkHoursCraneRegularAgro().get();
                    hoursCraneRegularIndustry.setSelectedAmount(amountHoursCraneRegular);
                    allProducts.add(hoursCraneRegularIndustry);
                }
                if(amountHoursCraneIntens > 0.0){
                    Product hoursCraneIntenseIndustry = productService.getWorkHoursCraneIntenseAgro().get();
                    hoursCraneIntenseIndustry.setSelectedAmount(amountHoursCraneIntens);
                    allProducts.add(hoursCraneIntenseIndustry);
                }
                if(amountForfait > 0){
                    Product hoursCraneForfaitIndustry = productService.getWorkHoursCraneForfaitAgro().get();
                    hoursCraneForfaitIndustry.setSelectedAmount(Double.valueOf(amountForfait));
                    allProducts.add(hoursCraneForfaitIndustry);
                }
            }

            //add roadTax / Tunneltax to workorder

            Double totalTax = 0.0;
            Double totalTunnelTax = 0.0;
            for(WorkOrder workOrder : workOrderSet){
                for(WorkOrderHeader workOrderHeader : workOrder.getWorkOrderHeaderList()){
                    Double tax = 0.0;
                    Double tunnelTax = 0.0;
                    if((workOrderHeader.getFleet() != null) && (workOrderHeader.getFleet().equals(Fleet.VAN))){
                        if(workOrderHeader.getRoadTax() != null){
                            tax = workOrderHeader.getRoadTax();
                        }
                        else{
                            tax = 0.0;
                        }
                        if(workOrderHeader.getTunnelTax() != null){
                            tunnelTax = workOrderHeader.getTunnelTax();
                        }
                        else{
                            tunnelTax = 0.0;
                        }
                    }
                    if((workOrderHeader.getFleet() != null) && (workOrderHeader.getFleet().equals(Fleet.ATEGO))){
                        if(workOrderHeader.getRoadTax() != null){
                            if((workAddress.getRoadTaxAtego() != null) && (workAddress.getRoadTaxAtego() > workOrderHeader.getRoadTax())){
                                tax = workAddress.getRoadTaxAtego();
                            }
                            else{
                                tax = workOrderHeader.getRoadTax();
                            }
                        }
                        else{
                            tax = 0.0;
                        }
                        if(workOrderHeader.getTunnelTax() != null){
                            tunnelTax = workOrderHeader.getTunnelTax();
                        }
                        else{
                            tunnelTax = 0.0;
                        }
                    }
                    if((workOrderHeader.getFleet() != null) && (workOrderHeader.getFleet().equals(Fleet.TRUCK_TRAILER))){
                        if(workOrderHeader.getRoadTax() != null){
                            if((workAddress.getRoadTaxActros() != null) && (workAddress.getRoadTaxActros() > workOrderHeader.getRoadTax())){
                                tax = workAddress.getRoadTaxActros();
                            }
                            else{
                                tax = workOrderHeader.getRoadTax();
                            }
                        }
                        else{
                            tax = 0.0;
                        }
                        if(workOrderHeader.getTunnelTax() != null){
                            tunnelTax = workOrderHeader.getTunnelTax();
                        }
                        else{
                            tunnelTax = 0.0;
                        }
                    }
                    if((workOrderHeader.getFleet() != null) && (workOrderHeader.getFleet().equals(Fleet.TRUCK_CRANE))){
                        if(workOrderHeader.getRoadTax() != null){
                            if((workAddress.getRoadTaxArocs() != null) && (workAddress.getRoadTaxArocs() > workOrderHeader.getRoadTax())){
                                tax = workAddress.getRoadTaxArocs();
                            }
                            else{
                                tax = workOrderHeader.getRoadTax();
                            }
                        }
                        else{
                            tax = 0.0;
                        }
                        if(workOrderHeader.getTunnelTax() != null){
                            tunnelTax = workOrderHeader.getTunnelTax();
                        }
                        else{
                            tunnelTax = 0.0;
                        }
                    }
                    totalTax += tax;
                    totalTunnelTax += tunnelTax;
                }
            }

            Product roadTaxProduct = new Product();
            roadTaxProduct.setSelectedAmount(1.0);
            roadTaxProduct.setInternalName("Wegentaks");
            roadTaxProduct.setSellPrice(totalTax);
            roadTaxProduct.setVat(VAT.EENENTWINTIG);
            allProducts.add(roadTaxProduct);

            Product tunnelTaxProduct = new Product();
            tunnelTaxProduct.setSelectedAmount(1.0);
            tunnelTaxProduct.setInternalName("tunneltaks");
            tunnelTaxProduct.setSellPrice(totalTunnelTax);
            tunnelTaxProduct.setVat(VAT.EENENTWINTIG);
            allProducts.add(tunnelTaxProduct);

            invoice.setProductList(allProducts);

        }

        return invoice;
    }

    public Invoice getnerateInvoicePerDay(Set<WorkOrder> workOrderSet){
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(getNewInvoiceNumber());
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setInvoiceStatus(InvoiceStatus.AANGEMAAKT);

        Address workAddress = workOrderSet.stream().findFirst().get().getWorkAddress();
        invoice.setWorkAddress(workAddress);

        Optional<List<Customer>> optCustomer = customerService.getCustomerByWorkAddress(workAddress);
        if(!optCustomer.isEmpty()){
            if(optCustomer.get().size() >= 2){
                Notification.show("Er zijn meerdere klanten met hetzelfde Werkadres");
            }
            invoice.setCustomer(optCustomer.get().get(0));

            List<String> allFotoIds = workOrderSet.stream()
                    .map(WorkOrder::getImageList)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            invoice.setImageList(allFotoIds);

            invoice.setWorkOrderList(workOrderSet);

            List<Product> allProducts = workOrderSet.stream()
                    .map(WorkOrder::getProductList)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            long generalHoursOnTheMove = 0L;
            long programHoursOnTheMove = 0L;
            long centrifugeHoursOnTheMove = 0L;

            for(WorkOrder workOrder : workOrderSet){
                int i = 0;
                int numberOfTechnicians = 0;
                for(WorkOrderHeader workOrderHeader : workOrder.getWorkOrderHeaderList()){
                    if(i == 0){
                        numberOfTechnicians = 1+ workOrder.getExtraEmployeesTeam1().size();
                    }
                    if(i == 1){
                        numberOfTechnicians = 1+ workOrder.getExtraEmployeesTeam2().size();
                    }
                    if(i == 2){
                        numberOfTechnicians = 1+ workOrder.getExtraEmployeesTeam3().size();
                    }
                    if(i == 3){
                        numberOfTechnicians = 1+ workOrder.getExtraEmployeesTeam4().size();
                    }
                    if(workOrderHeader.getWorkType() == WorkType.GENERAL){
                        for(WorkOrderTime workOrderTime : workOrderHeader.getWorkOrderTimeList()){
                            generalHoursOnTheMove = generalHoursOnTheMove + numberOfTechnicians * (Duration.between(workOrderTime.getTimeUp(), workOrderTime.getTimeDown()).toHours());
                        }
                    }
                    if(workOrderHeader.getWorkType() == WorkType.CENTRIFUGE){
                        for(WorkOrderTime workOrderTime : workOrderHeader.getWorkOrderTimeList()){
                            centrifugeHoursOnTheMove = centrifugeHoursOnTheMove + numberOfTechnicians * (Duration.between(workOrderTime.getTimeUp(), workOrderTime.getTimeDown()).toHours());
                        }
                    }
                    if(workOrderHeader.getWorkType() == WorkType.PROGRAMMATIC){
                        for(WorkOrderTime workOrderTime : workOrderHeader.getWorkOrderTimeList()){
                            programHoursOnTheMove = programHoursOnTheMove + numberOfTechnicians * (Duration.between(workOrderTime.getTimeUp(), workOrderTime.getTimeDown()).toHours());
                        }
                    }
                }
            }

            Product generalWorkHoursProduct = new Product();
            generalWorkHoursProduct.setComment("Algemeen werkuren op verplaatsing met laden en lossen");
            generalWorkHoursProduct.setSelectedAmount(Double.valueOf(generalHoursOnTheMove));
            generalWorkHoursProduct.setSellPrice(getSellPriceOfWorkHour(optCustomer.get(), WorkType.GENERAL, WorkLocation.ON_THE_MOVE));
            allProducts.add(generalWorkHoursProduct);

            Product centrifugeWorkHoursProduct = new Product();
            centrifugeWorkHoursProduct.setComment("Centrifuge werkuren op verplaatsing met laden en lossen");
            centrifugeWorkHoursProduct.setSelectedAmount(Double.valueOf(centrifugeHoursOnTheMove));
            centrifugeWorkHoursProduct.setSellPrice(getSellPriceOfWorkHour(optCustomer.get(), WorkType.CENTRIFUGE, WorkLocation.ON_THE_MOVE));
            allProducts.add(centrifugeWorkHoursProduct);

            Product programWorkHoursProduct = new Product();
            programWorkHoursProduct.setComment("Programmatie werkuren op verplaatsing met laden en lossen");
            programWorkHoursProduct.setSelectedAmount(Double.valueOf(generalHoursOnTheMove));
            programWorkHoursProduct.setSellPrice(getSellPriceOfWorkHour(optCustomer.get(), WorkType.GENERAL, WorkLocation.ON_THE_MOVE));
            allProducts.add(programWorkHoursProduct);

            invoice.setProductList(allProducts);
        }

        return invoice;
    }

    private Double getSellPriceOfWorkHour(List<Customer> customers, WorkType workType, WorkLocation workLocation) {
        if(customers.get(0).getBAgro()){
            if(workType == WorkType.CENTRIFUGE){
                Optional<Product> first = productService.getAllProductsByCategory("Agro", "Werkuren").get().stream().filter(item -> item.getInternalName().contains("Werkuren - centrifuge")).findFirst();
                if(first.isPresent()){
                    return first.get().getSellPrice();
                }
                return 0.0;
            }
            if(workType == WorkType.PROGRAMMATIC){
                Optional<Product> first = productService.getAllProductsByCategory("Agro", "Werkuren").get().stream().filter(item -> item.getInternalName().contains("Werkuren - programmatie")).findFirst();
                if(first.isPresent()){
                    return first.get().getSellPrice();
                }
                return 0.0;
            }
            if(workType == WorkType.GENERAL){
                Optional<Product> first = productService.getAllProductsByCategory("Agro", "Werkuren").get().stream().filter(item -> item.getInternalName().contains("Werkuren - verplaatsing")).findFirst();
                if(first.isPresent()){
                    return first.get().getSellPrice();
                }
                return 0.0;
            }
        }
        if(customers.get(1).getBIndustry()){
            if(workType == WorkType.CENTRIFUGE){
                Optional<Product> first = productService.getAllProductsByCategory("Industrie", "Werkuren").get().stream().filter(item -> item.getInternalName().contains("Werkuren - centrifuge")).findFirst();
                if(first.isPresent()){
                    return first.get().getSellPrice();
                }
                return 0.0;
            }
            if(workType == WorkType.PROGRAMMATIC){
                Optional<Product> first = productService.getAllProductsByCategory("Industrie", "Werkuren").get().stream().filter(item -> item.getInternalName().contains("Werkuren - programmatie")).findFirst();
                if(first.isPresent()){
                    return first.get().getSellPrice();
                }
                return 0.0;
            }
            if(workType == WorkType.GENERAL){
                Optional<Product> first = productService.getAllProductsByCategory("Industrie", "Werkuren").get().stream().filter(item -> item.getInternalName().contains("Werkuren - verplaatsing")).findFirst();
                if(first.isPresent()){
                    return first.get().getSellPrice();
                }
                return 0.0;
            }
        }
        return 0.0;
    }

    public Invoice generateInvoiceByDay(Set<WorkOrder> workOrderSet){
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(getNewInvoiceNumber());
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setInvoiceStatus(InvoiceStatus.AANGEMAAKT);

        Address workAddress = workOrderSet.stream().findFirst().get().getWorkAddress();
        invoice.setWorkAddress(workAddress);

        Optional<List<Customer>> optCustomer = customerService.getCustomerByWorkAddress(workAddress);
        if(!optCustomer.isEmpty()){
            if(optCustomer.get().size() >= 2){
                Notification.show("Er zijn meerdere klanten met hetzelfde Werkadres");
            }
            invoice.setCustomer(optCustomer.get().get(0));

            List<String> allFotoIds = workOrderSet.stream()
                    .map(WorkOrder::getImageList)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            invoice.setImageList(allFotoIds);

            invoice.setWorkOrderList(workOrderSet);


        }
        return invoice;
    }

    public void generateInvoicePDF(Invoice invoice){

        JasperReport jasperReport = null;
        JasperPrint jasperPrint = null;

        try {
            jasperReport = JasperCompileManager.compileReport( invoiceResourceJRXML.getInputStream() );
        } catch (JRException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            Notification.show("Kan de JRXML template niet vinden op de server!");
        }

        DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        parameters.clear();

        try {
            parameters.put("werfAdres", "Werfadres : " + "\n" +
                    invoice.getWorkAddress().getAddressName() + "\n" +
                    invoice.getWorkAddress().getStreet() + "\n" +
                    invoice.getWorkAddress().getZip() + " " +
                    invoice.getWorkAddress().getCity());
        }
        catch (Exception e){
            Notification.show("Gelieve voor een volledige servicelocatie te zorgen aub");
        }

        try {
            Address invoiceAddress = invoice.getCustomer().getAddresses().stream().filter(x -> x.getInvoiceAddress() == true).findFirst().get();
            parameters.put("facturatieAdres", invoice.getCustomer().getName() + "\n" +
                    invoiceAddress.getStreet() + "\n" +
                    invoiceAddress.getZip() + " " +
                    invoiceAddress.getCity());
        }
        catch (Exception e){
            //get first Address in list
            Address invoiceAddress = invoice.getCustomer().getAddresses().stream().findFirst().get();
            parameters.put("facturatieAdres", invoice.getCustomer().getName() + "\n" +
                    invoiceAddress.getStreet() + "\n" +
                    invoiceAddress.getZip() + " " +
                    invoiceAddress.getCity());
        }

        try {
            parameters.put("btwNummer", invoice.getCustomer().getVatNumber());
        }
        catch (Exception e){
            Notification.show("Gelieve voor een volledige BTW nummer te zorgen aub");
        }

        try {
            parameters.put("factuurNummer", invoice.getInvoiceNumber().toString());
        }
        catch (Exception e){
            Notification.show("Gelieve voor een volledige BTW nummer te zorgen aub");
        }

        try {
            parameters.put("datum", invoice.getInvoiceDate().format(FORMATTER));
        }
        catch (Exception e){
            Notification.show("Gelieve voor een volledige BTW nummer te zorgen aub");
        }

        DataImplementation dataImplementation = new DataImplementation( invoice.getProductList());

        parameters.put( "ItemDataSource", dataImplementation );

        try {
            jasperPrint  = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource(  ));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        String exportName = rootFolder + "invoice_"+ invoice.getInvoiceNumber()+".pdf";

        try {
            JasperExportManager.exportReportToPdfFile( jasperPrint, exportName );
        } catch (JRException e) {
            throw new RuntimeException(e);
        }

    }
}
