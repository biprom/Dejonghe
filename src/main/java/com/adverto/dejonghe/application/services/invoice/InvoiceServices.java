package com.adverto.dejonghe.application.services.invoice;

import com.adverto.dejonghe.application.Controllers.PdfController;
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
import com.adverto.dejonghe.application.entities.enums.product.VAT;
import com.adverto.dejonghe.application.entities.enums.workorder.WorkLocation;
import com.adverto.dejonghe.application.entities.enums.workorder.WorkType;
import com.adverto.dejonghe.application.entities.invoice.Invoice;
import com.adverto.dejonghe.application.entities.product.product.Product;
import com.adverto.dejonghe.application.implementations.DataImplementation;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import net.sf.jasperreports.engine.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class InvoiceServices {

    @Autowired
    InvoiceService invoiceService;
    @Autowired
    CustomerService customerService;
    @Autowired
    ProductService productService;
    @Autowired
    PdfController pdfController;

    @Value("${rootTemplateInvoice}")
    FileSystemResource invoiceResourceJRXML;
    @Value("${rootTemplateAttachement}")
    FileSystemResource attachementResourceJRXML;
    @Value( "${rootFolder}" )
    private String rootFolder;

    Map<String, Object> parameters = new HashMap<>();
    Integer attachementNumber = 0;

    List<String>attachementNames = new ArrayList<>();

    public Integer getNewProFormaInvoiceNumber() {
        Optional<Invoice> optionalInvoice = invoiceService.getLastProFormaInvoice();
        if(!optionalInvoice.isEmpty()){
            return Integer.valueOf(optionalInvoice.get().getInvoiceNumber()+1);
        }
        else{
            return 260001;
        }
    }

    public Integer getNewFinalInvoiceNumber() {
        Optional<Invoice> optionalInvoice = invoiceService.getLastFinalInvoice();
        if(!optionalInvoice.isEmpty()){
            return Integer.valueOf(optionalInvoice.get().getFinalInvoiceNumber()+1);
        }
        else{
            return 260001;
        }
    }

    private int compareOnderdeel(String s1, String s2) {
        List<Object> parts1 = splitAlphaNumeric(s1);
        List<Object> parts2 = splitAlphaNumeric(s2);

        int len = Math.min(parts1.size(), parts2.size());

        for (int i = 0; i < len; i++) {
            Object p1 = parts1.get(i);
            Object p2 = parts2.get(i);

            int cmp;
            if (p1 instanceof String && p2 instanceof String) {
                cmp = ((String) p1).compareToIgnoreCase((String) p2);
            } else if (p1 instanceof Number && p2 instanceof Number) {
                cmp = Double.compare(((Number) p1).doubleValue(), ((Number) p2).doubleValue());
            } else {
                // String vs Number → String komt altijd eerst
                cmp = (p1 instanceof String) ? -1 : 1;
            }

            if (cmp != 0) return cmp;
        }

        // Als alles gelijk is, kortere string komt eerst
        return Integer.compare(parts1.size(), parts2.size());
    }

    private List<Object> splitAlphaNumeric(String input) {
        List<Object> parts = new ArrayList<>();
        if(input == null){
            input = "";
        }
        Matcher matcher = Pattern.compile("(\\d+[\\.,]?\\d*|\\D+)").matcher(input);
        while (matcher.find()) {
            String part = matcher.group(1).trim();
            if (part.matches("\\d+[\\.,]?\\d*")) {
                part = part.replace(",", "."); // vervang komma door punt
                try {
                    parts.add(Double.parseDouble(part));
                } catch (NumberFormatException e) {
                    parts.add(part); // fallback: behandel als string
                }
            } else {
                parts.add(part);
            }
        }
        return parts;
    }

    public Invoice generateMergedInvoice(Set<WorkOrder> workOrderSet){
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(getNewProFormaInvoiceNumber());
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setExpiryDate(LocalDate.now().plusDays(14));
        invoice.setBFinalInvoice(false);

        Address workAddress = workOrderSet.stream().findFirst().get().getWorkAddress();
        invoice.setWorkAddress(workAddress);

        Optional<List<Customer>> optCustomer = customerService.getCustomerByWorkAddress(workAddress);
        if(optCustomer.isEmpty()){
            Customer customer = new Customer();
            customer.setId(LocalDateTime.now().toString());
            customer.setName(workAddress.getAddressName());
            customer.setVatNumber("");
            customer.setComment("");
            customer.setAlertMessage("");
            customer.setAlert(false);
            List<Address>addressList = new ArrayList<>();
            Address address = new Address();
            addressList.add(address);
            customer.setAddresses(addressList);
            optCustomer = Optional.of(List.of(customer));
        }
        else{
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

            List<Product> allProducts = new ArrayList<>();

            //get comment of first WorkOrder and add it as comment
            workOrderSet.stream().forEach(workOrder -> {
                try{
                    String comment = workOrder.getWorkOrderHeaderList().getFirst().getDiscription();
                    if (comment != null && !comment.isEmpty()){
                        List<String> commentRowList = Arrays.stream(comment.split("\\R")).toList();
                        for (int i = 0; i < commentRowList.size(); i++){
                            Product newProduct = new Product();
                            newProduct.setDate(workOrder.getWorkDateTime().toLocalDate());
                            newProduct.setInternalName(commentRowList.get(i));
                            newProduct.setTeamNumber(0);
                            newProduct.setBComment(true);
                            allProducts.add(newProduct);
                        }
                    }
                }
                catch (Exception e){
                    Notification.show("De starter bevat geen commentaar voor op de proforma! ");
                }
            });

            //retrieve selected Products
            List<Product> selectedProducts = workOrderSet.stream()
                    .map(WorkOrder::getProductList)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            //sort selected Products
            Comparator<Product> productComparator = (o1, o2) -> compareOnderdeel(o1.getInternalName(), o2.getInternalName());
            selectedProducts.sort(productComparator);

            //add sorted Products to list
            allProducts.addAll(selectedProducts.stream().filter(product->(product.getInternalName() != null) && (product.getInternalName().length() > 0)).collect(Collectors.toList()));

            //place all options at the bottom of the list
            allProducts.sort(Comparator.comparing(
                    p -> (p.getProductLevel1() != null)&&("Extra".equalsIgnoreCase(p.getProductLevel1().getName()))
            ));


            Double generalHoursOnTheMove = 0.0;
            Double programHoursOnTheMove = 0.0;
            Double centrifugeHoursOnTheMove = 0.0;

            Double generalHoursLocal = 0.0;
            Double programHoursLocal = 0.0;
            Double centrifugeHoursLocal = 0.0;


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
                                generalHoursOnTheMove = generalHoursOnTheMove + numberOfTechnicians * (((Duration.between(workOrderTime.getTimeUp(), workOrderTime.getTimeDown()).toMinutes())-workOrderTime.getPauze())/60.0);
                            }
                        }
                        if((workOrder.getWorkLocation().equals(WorkLocation.ON_THE_MOVE) && (workOrderHeader.getWorkType()) == WorkType.CENTRIFUGE)){
                            for(WorkOrderTime workOrderTime : workOrderHeader.getWorkOrderTimeList()){
                                centrifugeHoursOnTheMove = centrifugeHoursOnTheMove + numberOfTechnicians * (((Duration.between(workOrderTime.getTimeUp(), workOrderTime.getTimeDown()).toMinutes())-workOrderTime.getPauze())/60.0);
                            }
                        }
                        if((workOrder.getWorkLocation().equals(WorkLocation.ON_THE_MOVE) && (workOrderHeader.getWorkType()) == WorkType.PROGRAMMATIC)){
                            for(WorkOrderTime workOrderTime : workOrderHeader.getWorkOrderTimeList()){
                                programHoursOnTheMove = programHoursOnTheMove + numberOfTechnicians * (((Duration.between(workOrderTime.getTimeUp(), workOrderTime.getTimeDown()).toMinutes())-workOrderTime.getPauze())/60.0);
                            }
                        }
                        if((workOrder.getWorkLocation().equals(WorkLocation.WORKPLACE)) && (workOrderHeader.getWorkType() == WorkType.GENERAL)){
                            for(WorkOrderTime workOrderTime : workOrderHeader.getWorkOrderTimeList()){
                                generalHoursLocal = generalHoursLocal + (numberOfTechnicians * (((Duration.between(workOrderTime.getTimeStart(), workOrderTime.getTimeStop()).toMinutes())-workOrderTime.getPauze())/60.0));
                            }
                        }
                        if((workOrder.getWorkLocation().equals(WorkLocation.WORKPLACE) && (workOrderHeader.getWorkType()) == WorkType.CENTRIFUGE)){
                            for(WorkOrderTime workOrderTime : workOrderHeader.getWorkOrderTimeList()){
                                centrifugeHoursLocal = centrifugeHoursLocal + numberOfTechnicians * (((Duration.between(workOrderTime.getTimeStart(), workOrderTime.getTimeStop()).toMinutes())-workOrderTime.getPauze())/60.0);
                            }
                        }
                        if((workOrder.getWorkLocation().equals(WorkLocation.WORKPLACE) && (workOrderHeader.getWorkType()) == WorkType.PROGRAMMATIC)){
                            for(WorkOrderTime workOrderTime : workOrderHeader.getWorkOrderTimeList()){
                                programHoursLocal = programHoursLocal + numberOfTechnicians * (((Duration.between(workOrderTime.getTimeStart(), workOrderTime.getTimeStop()).toMinutes())-workOrderTime.getPauze())/60.0);
                            }
                        }
                        i++;
                    }
                }
            }


            if(optCustomer.get().getFirst().getBIndustry() == true){

                if(generalHoursLocal > 0){
                    Product workHourRegularLocalIndustrieProduct = productService.getWorkhourForRegularLocal().get();
                    workHourRegularLocalIndustrieProduct.setSelectedAmount(Double.valueOf(generalHoursLocal));
                    workHourRegularLocalIndustrieProduct.setTotalPrice(workHourRegularLocalIndustrieProduct.getSellPriceIndustry()*Double.valueOf(generalHoursLocal));
                    workHourRegularLocalIndustrieProduct.setBWorkHour(true);
                    workHourRegularLocalIndustrieProduct.setTeamNumber(0);
                    allProducts.add(workHourRegularLocalIndustrieProduct);
                }

                if(centrifugeHoursLocal > 0){
                    Product workHourCentrifugeLocalIndustrieProduct = productService.getWorkhourForCentrifugeLocal().get();
                    workHourCentrifugeLocalIndustrieProduct.setSelectedAmount(Double.valueOf(centrifugeHoursLocal));
                    workHourCentrifugeLocalIndustrieProduct.setTotalPrice(workHourCentrifugeLocalIndustrieProduct.getSellPriceIndustry()*Double.valueOf(centrifugeHoursLocal));
                    workHourCentrifugeLocalIndustrieProduct.setBWorkHour(true);
                    workHourCentrifugeLocalIndustrieProduct.setTeamNumber(0);
                    allProducts.add(workHourCentrifugeLocalIndustrieProduct);
                }

                if(programHoursLocal > 0){
                    Product workHourProgramLocalIndustrieProduct = productService.getWorkhourForProgammationLocal().get();
                    workHourProgramLocalIndustrieProduct.setSelectedAmount(Double.valueOf(programHoursLocal));
                    workHourProgramLocalIndustrieProduct.setTotalPrice(workHourProgramLocalIndustrieProduct.getSellPriceIndustry()*Double.valueOf(programHoursLocal));
                    workHourProgramLocalIndustrieProduct.setBWorkHour(true);
                    workHourProgramLocalIndustrieProduct.setTeamNumber(0);
                    allProducts.add(workHourProgramLocalIndustrieProduct);
                }

                if(generalHoursOnTheMove > 0){
                    Product workHourRegularOnTheMoveIndustrieProduct = productService.getWorkhourForRegularOnTheMove().get();
                    workHourRegularOnTheMoveIndustrieProduct.setSelectedAmount(Double.valueOf(generalHoursOnTheMove));
                    workHourRegularOnTheMoveIndustrieProduct.setTotalPrice(workHourRegularOnTheMoveIndustrieProduct.getSellPriceIndustry()*Double.valueOf(generalHoursOnTheMove));
                    workHourRegularOnTheMoveIndustrieProduct.setBWorkHour(true);
                    workHourRegularOnTheMoveIndustrieProduct.setTeamNumber(0);
                    allProducts.add(workHourRegularOnTheMoveIndustrieProduct);
                }

                if(centrifugeHoursOnTheMove > 0){
                    Product workHourCentrifugeOnTheMoveIndustrieProduct = productService.getWorkhourForCentrifugeOnTheMove().get();
                    workHourCentrifugeOnTheMoveIndustrieProduct.setSelectedAmount(Double.valueOf(centrifugeHoursOnTheMove));
                    workHourCentrifugeOnTheMoveIndustrieProduct.setTotalPrice(workHourCentrifugeOnTheMoveIndustrieProduct.getSellPriceIndustry()*Double.valueOf(centrifugeHoursOnTheMove));
                    workHourCentrifugeOnTheMoveIndustrieProduct.setBWorkHour(true);
                    workHourCentrifugeOnTheMoveIndustrieProduct.setTeamNumber(0);
                    allProducts.add(workHourCentrifugeOnTheMoveIndustrieProduct);
                }

                if(programHoursOnTheMove > 0){
                    Product workHourProgramOnTheMoveIndustrieProduct = productService.getWorkhourForProgammationOnTheMove().get();
                    workHourProgramOnTheMoveIndustrieProduct.setSelectedAmount(Double.valueOf(programHoursOnTheMove));
                    workHourProgramOnTheMoveIndustrieProduct.setTotalPrice(workHourProgramOnTheMoveIndustrieProduct.getSellPriceIndustry()*Double.valueOf(programHoursOnTheMove));
                    workHourProgramOnTheMoveIndustrieProduct.setBWorkHour(true);
                    workHourProgramOnTheMoveIndustrieProduct.setTeamNumber(0);
                    allProducts.add(workHourProgramOnTheMoveIndustrieProduct);
                }

            }

            else{
                if(generalHoursLocal > 0){
                    Product workHourRegularLocalAgroProduct = productService.getWorkhourForRegularLocal().get();
                    workHourRegularLocalAgroProduct.setSelectedAmount(Double.valueOf(generalHoursLocal));
                    workHourRegularLocalAgroProduct.setTotalPrice(workHourRegularLocalAgroProduct.getSellPrice()*Double.valueOf(generalHoursLocal));
                    workHourRegularLocalAgroProduct.setBWorkHour(true);
                    workHourRegularLocalAgroProduct.setTeamNumber(0);
                    allProducts.add(workHourRegularLocalAgroProduct);
                }

                if(centrifugeHoursLocal > 0){
                    Product workHourCentrifugeLocalAgroProduct = productService.getWorkhourForCentrifugeLocal().get();
                    workHourCentrifugeLocalAgroProduct.setSelectedAmount(Double.valueOf(centrifugeHoursLocal));
                    workHourCentrifugeLocalAgroProduct.setTotalPrice(workHourCentrifugeLocalAgroProduct.getSellPrice()*Double.valueOf(centrifugeHoursLocal));
                    workHourCentrifugeLocalAgroProduct.setBWorkHour(true);
                    workHourCentrifugeLocalAgroProduct.setTeamNumber(0);
                    allProducts.add(workHourCentrifugeLocalAgroProduct);
                }

                if(programHoursLocal > 0){
                    Product workHourProgramLocalAgroProduct = productService.getWorkhourForProgammationLocal().get();
                    workHourProgramLocalAgroProduct.setSelectedAmount(Double.valueOf(programHoursLocal));
                    workHourProgramLocalAgroProduct.setTotalPrice(workHourProgramLocalAgroProduct.getSellPrice()*Double.valueOf(programHoursLocal));
                    workHourProgramLocalAgroProduct.setBWorkHour(true);
                    workHourProgramLocalAgroProduct.setTeamNumber(0);
                    allProducts.add(workHourProgramLocalAgroProduct);
                }
                if(generalHoursOnTheMove > 0){
                    Product workHourRegularOnTheMoveAgroProduct = productService.getWorkhourForRegularOnTheMove().get();
                    workHourRegularOnTheMoveAgroProduct.setSelectedAmount(Double.valueOf(generalHoursOnTheMove));
                    workHourRegularOnTheMoveAgroProduct.setTotalPrice(workHourRegularOnTheMoveAgroProduct.getSellPrice()*Double.valueOf(generalHoursOnTheMove));
                    workHourRegularOnTheMoveAgroProduct.setBWorkHour(true);
                    workHourRegularOnTheMoveAgroProduct.setTeamNumber(0);
                    allProducts.add(workHourRegularOnTheMoveAgroProduct);
                }

                if(centrifugeHoursOnTheMove > 0){
                    Product workHourCentrifugeOnTheMoveAgroProduct = productService.getWorkhourForCentrifugeOnTheMove().get();
                    workHourCentrifugeOnTheMoveAgroProduct.setSelectedAmount(Double.valueOf(centrifugeHoursOnTheMove));
                    workHourCentrifugeOnTheMoveAgroProduct.setTotalPrice(workHourCentrifugeOnTheMoveAgroProduct.getSellPrice()*Double.valueOf(centrifugeHoursOnTheMove));
                    workHourCentrifugeOnTheMoveAgroProduct.setBWorkHour(true);
                    workHourCentrifugeOnTheMoveAgroProduct.setTeamNumber(0);
                    allProducts.add(workHourCentrifugeOnTheMoveAgroProduct);
                }

                if(programHoursOnTheMove > 0){
                    Product workHourProgramOnTheMoveAgroProduct = productService.getWorkhourForProgammationOnTheMove().get();
                    workHourProgramOnTheMoveAgroProduct.setSelectedAmount(Double.valueOf(programHoursOnTheMove));
                    workHourProgramOnTheMoveAgroProduct.setTotalPrice(workHourProgramOnTheMoveAgroProduct.getSellPrice()*Double.valueOf(programHoursOnTheMove));
                    workHourProgramOnTheMoveAgroProduct.setBWorkHour(true);
                    workHourProgramOnTheMoveAgroProduct.setTeamNumber(0);
                    allProducts.add(workHourProgramOnTheMoveAgroProduct);
                }
            }

            // add movement to Proforma
            Double amountKmRegular = 0.0;
            Integer amountRidesRegular = 0;
            Double amountKmTrailer = 0.0;
            Integer amountRidesTrailer = 0;
            Double amountKmCrane = 0.0;
            Integer amountRidesCrane = 0;
            Double amountHoursCraneRegular = 0.0;
            Double amountHoursCraneIntens = 0.0;
            Integer amountForfait = 0;

            for(WorkOrder workOrder : workOrderSet) {
                for (WorkOrderHeader workOrderHeader : workOrder.getWorkOrderHeaderList()) {
                    if((workAddress.getDistance() != null) && (((workOrderHeader.getFleet() != null) && (workOrderHeader.getFleet().equals(Fleet.VAN))) ||
                            ((workOrderHeader.getFleet() != null) && (workOrderHeader.getFleet().equals(Fleet.ATEGO))))){
                        amountKmRegular = amountKmRegular + (workAddress.getDistance());
                        amountRidesRegular = amountRidesRegular + 1;
                    }
                    if((workAddress.getDistance() != null) && (workOrderHeader.getFleet() != null) && (workOrderHeader.getFleet().equals(Fleet.TRUCK_TRAILER))){
                        amountKmTrailer = amountKmTrailer + (workAddress.getDistance());
                        amountRidesTrailer = amountRidesTrailer + 1;
                    }
                    if((workAddress.getDistance() != null) && (workOrderHeader.getFleet() != null) && (workOrderHeader.getFleet().equals(Fleet.TRUCK_CRANE))){
                        amountKmCrane = amountKmCrane + (workAddress.getDistance());
                        amountRidesCrane = amountRidesCrane + 1;
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
                    Product regularKm = productService.getRegularKm().get();
                    regularKm.setSelectedAmount(amountKmRegular);
                    regularKm.setTotalPrice(amountKmRegular*regularKm.getSellPriceIndustry());
                    regularKm.setBTravel(true);
                    regularKm.setTeamNumber(0);
                    if(amountRidesRegular > 1) {
                        regularKm.setInternalName(regularKm.getInternalName() + "("+ amountRidesRegular + " x heen en terug)");
                    }
                    allProducts.add(regularKm);
                }
                if(amountKmTrailer > 0.0){
                    Product forfaitTtrailer = productService.getWorkHoursTrailerForfait().get();
                    forfaitTtrailer.setSelectedAmount(1.0);
                    forfaitTtrailer.setTotalPrice(forfaitTtrailer.getSellPriceIndustry());
                    forfaitTtrailer.setBTravel(true);
                    forfaitTtrailer.setTeamNumber(0);
                    allProducts.add(forfaitTtrailer);

                    Product trailerKm = productService.getRegularTrailer().get();
                    trailerKm.setSelectedAmount(amountKmTrailer);
                    trailerKm.setTotalPrice(amountKmTrailer*trailerKm.getSellPriceIndustry());
                    trailerKm.setBTravel(true);
                    trailerKm.setTeamNumber(0);
                    if(amountRidesRegular > 1) {
                        trailerKm.setInternalName(trailerKm.getInternalName() + "("+ amountRidesTrailer + " x heen en terug)");
                    }
                    allProducts.add(trailerKm);
                }
                if(amountKmCrane > 0.0){
                    Product craneKm = productService.getRegularCrane().get();
                    craneKm.setSelectedAmount(amountKmCrane);
                    craneKm.setTotalPrice(amountKmCrane*craneKm.getSellPriceIndustry());
                    craneKm.setBTravel(true);
                    craneKm.setTeamNumber(0);
                    if(amountRidesRegular > 1) {
                        craneKm.setInternalName(craneKm.getInternalName() + "("+ amountRidesCrane + " x heen en terug)");
                    }
                    allProducts.add(craneKm);
                }
                if((amountHoursCraneRegular > 0.0) && (amountHoursCraneRegular >= 3.0)){
                    Product hoursCraneRegularIndustry = productService.getWorkHoursCraneRegular().get();
                    hoursCraneRegularIndustry.setSelectedAmount(amountHoursCraneRegular);
                    hoursCraneRegularIndustry.setTotalPrice(amountHoursCraneRegular*hoursCraneRegularIndustry.getSellPriceIndustry());
                    hoursCraneRegularIndustry.setBTravel(true);
                    hoursCraneRegularIndustry.setTeamNumber(0);
                    allProducts.add(hoursCraneRegularIndustry);
                }
                else if((amountHoursCraneRegular > 0.0) && (amountHoursCraneRegular < 3.0)){
                    Product hoursCraneRegularIndustry = productService.getWorkHoursCraneRegular().get();
                    hoursCraneRegularIndustry.setSelectedAmount(3.0);
                    hoursCraneRegularIndustry.setTotalPrice(3.0*hoursCraneRegularIndustry.getSellPriceIndustry());
                    hoursCraneRegularIndustry.setBTravel(true);
                    hoursCraneRegularIndustry.setTeamNumber(0);
                    allProducts.add(hoursCraneRegularIndustry);
                }
                else if((amountHoursCraneIntens > 0.0) && (amountHoursCraneIntens >= 4.0)){
                    Product hoursCraneIntenseIndustry = productService.getWorkHoursCraneIntense().get();
                    hoursCraneIntenseIndustry.setSelectedAmount(amountHoursCraneIntens);
                    hoursCraneIntenseIndustry.setTotalPrice(amountHoursCraneIntens*hoursCraneIntenseIndustry.getSellPriceIndustry());
                    hoursCraneIntenseIndustry.setBTravel(true);
                    hoursCraneIntenseIndustry.setTeamNumber(0);
                    allProducts.add(hoursCraneIntenseIndustry);
                }
                else if((amountHoursCraneIntens > 0.0) && (amountHoursCraneIntens < 4.0)){
                    Product hoursCraneIntenseIndustry = productService.getWorkHoursCraneIntense().get();
                    hoursCraneIntenseIndustry.setSelectedAmount(4.0);
                    hoursCraneIntenseIndustry.setTotalPrice(4.0*hoursCraneIntenseIndustry.getSellPriceIndustry());
                    hoursCraneIntenseIndustry.setBTravel(true);
                    hoursCraneIntenseIndustry.setTeamNumber(0);
                    allProducts.add(hoursCraneIntenseIndustry);
                }
                if(amountForfait > 0){
                    Product hoursCraneForfaitIndustry = productService.getWorkHoursCraneForfait().get();
                    hoursCraneForfaitIndustry.setSelectedAmount(Double.valueOf(amountForfait));
                    hoursCraneForfaitIndustry.setTotalPrice(amountForfait*hoursCraneForfaitIndustry.getSellPriceIndustry());
                    hoursCraneForfaitIndustry.setBTravel(true);
                    hoursCraneForfaitIndustry.setTeamNumber(0);
                    allProducts.add(hoursCraneForfaitIndustry);
                }
            }
            else{
                if(amountKmRegular > 0.0){
                    Product regularKm = productService.getRegularKm().get();
                    regularKm.setSelectedAmount(amountKmRegular);
                    regularKm.setTotalPrice(amountKmRegular*regularKm.getSellPrice());
                    regularKm.setBTravel(true);
                    regularKm.setTeamNumber(0);
                    if(amountRidesRegular > 1) {
                        regularKm.setInternalName(regularKm.getInternalName() + "("+ amountRidesRegular + " x heen en terug)");
                    }
                    allProducts.add(regularKm);
                }
                if(amountKmTrailer > 0.0){

                    Product forfaitTrailer = productService.getWorkHoursTrailerForfait().get();
                    forfaitTrailer.setSelectedAmount(1.0);
                    forfaitTrailer.setTotalPrice(forfaitTrailer.getSellPrice());
                    forfaitTrailer.setBTravel(true);
                    forfaitTrailer.setTeamNumber(0);
                    allProducts.add(forfaitTrailer);

                    Product trailerKm = productService.getRegularTrailer().get();
                    trailerKm.setSelectedAmount(amountKmTrailer);
                    trailerKm.setTotalPrice(amountKmTrailer*trailerKm.getSellPrice());
                    trailerKm.setBTravel(true);
                    trailerKm.setTeamNumber(0);
                    if(amountRidesRegular > 1) {
                        trailerKm.setInternalName(trailerKm.getInternalName() + "("+ amountRidesTrailer + " x heen en terug)");
                    }
                    allProducts.add(trailerKm);
                }
                if(amountKmCrane > 0.0){
                    Product craneKm = productService.getRegularCrane().get();
                    craneKm.setSelectedAmount(amountKmCrane);
                    craneKm.setTotalPrice(amountKmCrane*craneKm.getSellPrice());
                    craneKm.setBTravel(true);
                    craneKm.setTeamNumber(0);
                    if(amountRidesRegular > 1) {
                        craneKm.setInternalName(craneKm.getInternalName() + "("+ amountRidesCrane + " x heen en terug)");
                    }
                    allProducts.add(craneKm);
                }
                if((amountHoursCraneRegular > 0.0) && (amountHoursCraneRegular >= 3.0)){
                    Product hoursCraneRegularIndustry = productService.getWorkHoursCraneRegular().get();
                    hoursCraneRegularIndustry.setSelectedAmount(amountHoursCraneRegular);
                    hoursCraneRegularIndustry.setTotalPrice(amountHoursCraneRegular*hoursCraneRegularIndustry.getSellPrice());
                    hoursCraneRegularIndustry.setBTravel(true);
                    hoursCraneRegularIndustry.setTeamNumber(0);
                    allProducts.add(hoursCraneRegularIndustry);
                }
                else if((amountHoursCraneRegular > 0.0) && (amountHoursCraneRegular < 3.0)){
                    Product hoursCraneRegularIndustry = productService.getWorkHoursCraneRegular().get();
                    hoursCraneRegularIndustry.setSelectedAmount(3.0);
                    hoursCraneRegularIndustry.setTotalPrice(3.0*hoursCraneRegularIndustry.getSellPrice());
                    hoursCraneRegularIndustry.setBTravel(true);
                    hoursCraneRegularIndustry.setTeamNumber(0);
                    allProducts.add(hoursCraneRegularIndustry);
                }
                else if((amountHoursCraneIntens > 0.0) && (amountHoursCraneIntens >= 4.0)){
                    Product hoursCraneIntenseIndustry = productService.getWorkHoursCraneIntense().get();
                    hoursCraneIntenseIndustry.setSelectedAmount(amountHoursCraneIntens);
                    hoursCraneIntenseIndustry.setTotalPrice(amountHoursCraneIntens*hoursCraneIntenseIndustry.getSellPrice());
                    hoursCraneIntenseIndustry.setBTravel(true);
                    hoursCraneIntenseIndustry.setTeamNumber(0);
                    allProducts.add(hoursCraneIntenseIndustry);
                }
                else if((amountHoursCraneIntens > 0.0) && (amountHoursCraneIntens < 4.0)){
                    Product hoursCraneIntenseIndustry = productService.getWorkHoursCraneIntense().get();
                    hoursCraneIntenseIndustry.setSelectedAmount(4.0);
                    hoursCraneIntenseIndustry.setTotalPrice(4.0*hoursCraneIntenseIndustry.getSellPrice());
                    hoursCraneIntenseIndustry.setBTravel(true);
                    hoursCraneIntenseIndustry.setTeamNumber(0);
                    allProducts.add(hoursCraneIntenseIndustry);
                }
                if(amountForfait > 0){
                    Product hoursCraneForfaitIndustry = productService.getWorkHoursCraneForfait().get();
                    hoursCraneForfaitIndustry.setSelectedAmount(Double.valueOf(amountForfait));
                    hoursCraneForfaitIndustry.setTotalPrice(amountForfait*hoursCraneForfaitIndustry.getSellPrice());
                    hoursCraneForfaitIndustry.setBTravel(true);
                    hoursCraneForfaitIndustry.setTeamNumber(0);
                    allProducts.add(hoursCraneForfaitIndustry);
                }
            }

            //add roadTax / Tunneltax to workorder

            Double totalTax = 0.0;
            Double totalTunnelTax = 0.0;
            for(WorkOrder workOrder : workOrderSet){
                for(WorkOrderHeader workOrderHeader : workOrder.getWorkOrderHeaderList()){
                    Double roadTax = 0.0;
                    Double tunnelTax = 0.0;
                    if((workOrderHeader.getFleet() != null) && (workOrderHeader.getFleet().equals(Fleet.VAN))){
                        if(workOrderHeader.getRoadTax() != null){
                            roadTax = workOrderHeader.getRoadTax();
                        }
                        else{
                            roadTax = 0.0;
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
                                roadTax = workAddress.getRoadTaxAtego();
                            }
                            else{
                                roadTax = workOrderHeader.getRoadTax();
                            }
                        }
                        else{
                            roadTax = workAddress.getRoadTaxAtego();
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
                                roadTax = workAddress.getRoadTaxActros();
                            }
                            else{
                                roadTax = workOrderHeader.getRoadTax();
                            }
                        }
                        else{
                            roadTax = workAddress.getRoadTaxActros();
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
                                roadTax = workAddress.getRoadTaxArocs();
                            }
                            else{
                                roadTax = workOrderHeader.getRoadTax();
                            }
                        }
                        else{
                            roadTax = workAddress.getRoadTaxArocs();
                        }
                        if(workOrderHeader.getTunnelTax() != null){
                            tunnelTax = workOrderHeader.getTunnelTax();
                        }
                        else{
                            tunnelTax = 0.0;
                        }
                    }
                    if(roadTax != null){
                        totalTax += roadTax;
                    }
                    if(tunnelTax != null){
                        totalTunnelTax += tunnelTax;
                    }
                }
            }

            if(totalTax > 0.0){
                Product roadTaxProduct = new Product();
                roadTaxProduct.setSelectedAmount(1.0);
                roadTaxProduct.setInternalName("Wegentaks");
                roadTaxProduct.setSellPrice(totalTax);
                roadTaxProduct.setTotalPrice(1.0 * totalTax);
                roadTaxProduct.setBTravel(true);
                roadTaxProduct.setVat(VAT.EENENTWINTIG);
                roadTaxProduct.setTeamNumber(0);
                allProducts.add(roadTaxProduct);
            }

            if (totalTunnelTax > 0.0){
                Product tunnelTaxProduct = new Product();
                tunnelTaxProduct.setSelectedAmount(1.0);
                tunnelTaxProduct.setInternalName("tunneltaks");
                tunnelTaxProduct.setSellPrice(totalTunnelTax);
                tunnelTaxProduct.setTotalPrice(1.0 * totalTunnelTax);
                tunnelTaxProduct.setBTravel(true);
                tunnelTaxProduct.setVat(VAT.EENENTWINTIG);
                tunnelTaxProduct.setTeamNumber(0);
                allProducts.add(tunnelTaxProduct);
            }

            //All products has to have the same date as the starter.
            //this because it is a merged invoice with possibly one attachement
            LocalDateTime starterDateTime = workOrderSet.stream().filter(workorder -> workorder.getStarter() == true).findFirst().get().getWorkDateTime();
            allProducts.stream().forEach(product -> product.setDate(starterDateTime.toLocalDate()));

            invoice.setProductList(allProducts);
            checkIfToolsHoursAreSubtractedFromWorkOrder(invoice);
        }
        return invoice;
    }

    public Invoice getnerateInvoicePerDay(Set<WorkOrder> workOrderSet) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(getNewProFormaInvoiceNumber());
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setExpiryDate(LocalDate.now().plusDays(14));
        invoice.setBFinalInvoice(false);

        Address workAddress = workOrderSet.stream().findFirst().get().getWorkAddress();
        invoice.setWorkAddress(workAddress);
        Optional<List<Customer>> optCustomer = customerService.getCustomerByWorkAddress(workAddress)
        ;
        if(optCustomer.isEmpty()){
            Customer customer = new Customer();
            customer.setId(LocalDateTime.now().toString());
            customer.setName(workAddress.getAddressName());
            customer.setVatNumber("");
            customer.setComment("");
            customer.setAlertMessage("");
            customer.setAlert(false);
            List<Address>addressList = new ArrayList<>();
            Address address = new Address();
            addressList.add(address);
            customer.setAddresses(addressList);
            optCustomer = Optional.of(List.of(customer));
        }
        else {
            if (optCustomer.get().size() >= 2) {
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

            List<Product> allProducts = new ArrayList<>();

            for (WorkOrder workOrder : workOrderSet) {

                //generate empty line
//                Product emptyLine = new Product();
//                emptyLine.setDate(workOrder.getWorkDateTime().toLocalDate());
//                emptyLine.setTeamNumber(0);
//                emptyLine.setBComment(true);
//                allProducts.add(emptyLine);

                //generate Comments/Products per day
                //place comment first
                try {
                    String comment = workOrder.getWorkOrderHeaderList().getFirst().getDiscription();
                    if (comment != null && !comment.isEmpty()) {
                        List<String> commentRowList = Arrays.stream(comment.split("\\R")).toList();
                        for (int i = 0; i < commentRowList.size(); i++) {
                            Product newProduct = new Product();
                            newProduct.setDate(workOrder.getWorkDateTime().toLocalDate());
                            newProduct.setInternalName(commentRowList.get(i));
                            newProduct.setTeamNumber(0);
                            newProduct.setBComment(true);
                            allProducts.add(newProduct);
                        }
                    }
                } catch (Exception e) {
                    Notification.show("De starter bevat geen commentaar voor op de proforma! ");
                }

                //retrieve selected Products
                List<Product> selectedProducts = workOrder.getProductList().stream()
                        .filter(Objects::nonNull)
                        .filter((product -> (product.getInternalName() != null) && (product.getInternalName().length() > 0)))
                        .collect(Collectors.toList());

                //add date of looped Workorder to the Products so we can generate right attachements!
                selectedProducts.stream().forEach(product -> product.setDate(workOrder.getWorkDateTime().toLocalDate()));

                //sort selected Products
                Comparator<Product> productComparator = (o1, o2) -> compareOnderdeel(o1.getInternalName(), o2.getInternalName());
                selectedProducts.sort(productComparator);

                //place all options at the bottom of the list
                selectedProducts.sort(Comparator.comparing(
                        p -> (p.getProductLevel1() != null) && ("Extra".equalsIgnoreCase(p.getProductLevel1().getName()))
                ));

                //add sorted Products to list
                allProducts.addAll(selectedProducts);


                Double generalHoursOnTheMove = 0.0;
                Double programHoursOnTheMove = 0.0;
                Double centrifugeHoursOnTheMove = 0.0;

                Double generalHoursLocal = 0.0;
                Double programHoursLocal = 0.0;
                Double centrifugeHoursLocal = 0.0;

                int i = 0;
                for (WorkOrderHeader workOrderHeader : workOrder.getWorkOrderHeaderList()) {

                    int numberOfTechnicians = 0;

                    if ((workOrder.getWorkOrderHeaderList() != null) && (workOrder.getWorkOrderHeaderList().size() > 0) && (workOrderHeader.getWorkOrderTimeList() != null)) {
                        if (i == 0) {
                            numberOfTechnicians = numberOfTechnicians + 1 + workOrder.getExtraEmployeesTeam1().size();
                        }
                        if (i == 1) {
                            numberOfTechnicians = numberOfTechnicians + 1 + workOrder.getExtraEmployeesTeam2().size();
                        }
                        if (i == 2) {
                            numberOfTechnicians = numberOfTechnicians + 1 + workOrder.getExtraEmployeesTeam3().size();
                        }
                        if (i == 3) {
                            numberOfTechnicians = numberOfTechnicians + 1 + workOrder.getExtraEmployeesTeam4().size();
                        }
                        if ((workOrder.getWorkLocation().equals(WorkLocation.ON_THE_MOVE)) && (workOrderHeader.getWorkType() == WorkType.GENERAL)) {
                            for (WorkOrderTime workOrderTime : workOrderHeader.getWorkOrderTimeList()) {
                                generalHoursOnTheMove = generalHoursOnTheMove + numberOfTechnicians * (((Duration.between(workOrderTime.getTimeUp(), workOrderTime.getTimeDown()).toMinutes())-workOrderTime.getPauze()) / 60.0);
                            }
                        }
                        if ((workOrder.getWorkLocation().equals(WorkLocation.ON_THE_MOVE) && (workOrderHeader.getWorkType()) == WorkType.CENTRIFUGE)) {
                            for (WorkOrderTime workOrderTime : workOrderHeader.getWorkOrderTimeList()) {
                                centrifugeHoursOnTheMove = centrifugeHoursOnTheMove + numberOfTechnicians * (((Duration.between(workOrderTime.getTimeUp(), workOrderTime.getTimeDown()).toMinutes())-workOrderTime.getPauze()) / 60.0);
                            }
                        }
                        if ((workOrder.getWorkLocation().equals(WorkLocation.ON_THE_MOVE) && (workOrderHeader.getWorkType()) == WorkType.PROGRAMMATIC)) {
                            for (WorkOrderTime workOrderTime : workOrderHeader.getWorkOrderTimeList()) {
                                programHoursOnTheMove = programHoursOnTheMove + numberOfTechnicians * (((Duration.between(workOrderTime.getTimeUp(), workOrderTime.getTimeDown()).toMinutes())-workOrderTime.getPauze()) / 60.0);
                            }
                        }
                        if ((workOrder.getWorkLocation().equals(WorkLocation.WORKPLACE)) && (workOrderHeader.getWorkType() == WorkType.GENERAL)) {
                            for (WorkOrderTime workOrderTime : workOrderHeader.getWorkOrderTimeList()) {
                                generalHoursLocal = generalHoursLocal + (numberOfTechnicians * (((Duration.between(workOrderTime.getTimeStart(), workOrderTime.getTimeStop()).toMinutes())-workOrderTime.getPauze()) / 60.0));
                            }
                        }
                        if ((workOrder.getWorkLocation().equals(WorkLocation.WORKPLACE) && (workOrderHeader.getWorkType()) == WorkType.CENTRIFUGE)) {
                            for (WorkOrderTime workOrderTime : workOrderHeader.getWorkOrderTimeList()) {
                                centrifugeHoursLocal = centrifugeHoursLocal + numberOfTechnicians * (((Duration.between(workOrderTime.getTimeStart(), workOrderTime.getTimeStop()).toMinutes())-workOrderTime.getPauze()) / 60.0);
                            }
                        }
                        if ((workOrder.getWorkLocation().equals(WorkLocation.WORKPLACE) && (workOrderHeader.getWorkType()) == WorkType.PROGRAMMATIC)) {
                            for (WorkOrderTime workOrderTime : workOrderHeader.getWorkOrderTimeList()) {
                                programHoursLocal = programHoursLocal + numberOfTechnicians * (((Duration.between(workOrderTime.getTimeStart(), workOrderTime.getTimeStop()).toMinutes())-workOrderTime.getPauze()) / 60.0);
                            }
                        }
                        i++;
                    }
                }

                if (optCustomer.get().getFirst().getBIndustry() == true) {

                    if (generalHoursLocal > 0) {
                        Product workHourRegularLocalIndustrieProduct = productService.getWorkhourForRegularLocal().get();
                        workHourRegularLocalIndustrieProduct.setSelectedAmount(Double.valueOf(generalHoursLocal));
                        workHourRegularLocalIndustrieProduct.setTotalPrice(Double.valueOf(generalHoursLocal) * workHourRegularLocalIndustrieProduct.getSellPriceIndustry());
                        workHourRegularLocalIndustrieProduct.setDate(workOrder.getWorkDateTime().toLocalDate());
                        workHourRegularLocalIndustrieProduct.setBWorkHour(true);
                        workHourRegularLocalIndustrieProduct.setTeamNumber(0);
                        allProducts.add(workHourRegularLocalIndustrieProduct);
                    }

                    if (centrifugeHoursLocal > 0) {
                        Product workHourCentrifugeLocalIndustrieProduct = productService.getWorkhourForCentrifugeLocal().get();
                        workHourCentrifugeLocalIndustrieProduct.setSelectedAmount(Double.valueOf(centrifugeHoursLocal));
                        workHourCentrifugeLocalIndustrieProduct.setTotalPrice(Double.valueOf(centrifugeHoursLocal) * workHourCentrifugeLocalIndustrieProduct.getSellPriceIndustry());
                        workHourCentrifugeLocalIndustrieProduct.setDate(workOrder.getWorkDateTime().toLocalDate());
                        workHourCentrifugeLocalIndustrieProduct.setBWorkHour(true);
                        workHourCentrifugeLocalIndustrieProduct.setTeamNumber(0);
                        allProducts.add(workHourCentrifugeLocalIndustrieProduct);
                    }

                    if (programHoursLocal > 0) {
                        Product workHourProgramLocalIndustrieProduct = productService.getWorkhourForProgammationLocal().get();
                        workHourProgramLocalIndustrieProduct.setSelectedAmount(Double.valueOf(programHoursLocal));
                        workHourProgramLocalIndustrieProduct.setTotalPrice(Double.valueOf(programHoursLocal) * workHourProgramLocalIndustrieProduct.getSellPriceIndustry());
                        workHourProgramLocalIndustrieProduct.setDate(workOrder.getWorkDateTime().toLocalDate());
                        workHourProgramLocalIndustrieProduct.setBWorkHour(true);
                        workHourProgramLocalIndustrieProduct.setTeamNumber(0);
                        allProducts.add(workHourProgramLocalIndustrieProduct);
                    }

                    if (generalHoursOnTheMove > 0) {
                        Product workHourRegularOnTheMoveIndustrieProduct = productService.getWorkhourForRegularOnTheMove().get();
                        workHourRegularOnTheMoveIndustrieProduct.setSelectedAmount(Double.valueOf(generalHoursOnTheMove));
                        workHourRegularOnTheMoveIndustrieProduct.setTotalPrice(Double.valueOf(generalHoursOnTheMove) * workHourRegularOnTheMoveIndustrieProduct.getSellPriceIndustry());
                        workHourRegularOnTheMoveIndustrieProduct.setDate(workOrder.getWorkDateTime().toLocalDate());
                        workHourRegularOnTheMoveIndustrieProduct.setBWorkHour(true);
                        workHourRegularOnTheMoveIndustrieProduct.setTeamNumber(0);
                        allProducts.add(workHourRegularOnTheMoveIndustrieProduct);
                    }

                    if (centrifugeHoursOnTheMove > 0) {
                        Product workHourCentrifugeOnTheMoveIndustrieProduct = productService.getWorkhourForCentrifugeOnTheMove().get();
                        workHourCentrifugeOnTheMoveIndustrieProduct.setSelectedAmount(Double.valueOf(centrifugeHoursOnTheMove));
                        workHourCentrifugeOnTheMoveIndustrieProduct.setTotalPrice(Double.valueOf(centrifugeHoursOnTheMove) * workHourCentrifugeOnTheMoveIndustrieProduct.getSellPriceIndustry());
                        workHourCentrifugeOnTheMoveIndustrieProduct.setDate(workOrder.getWorkDateTime().toLocalDate());
                        workHourCentrifugeOnTheMoveIndustrieProduct.setBWorkHour(true);
                        workHourCentrifugeOnTheMoveIndustrieProduct.setTeamNumber(0);
                        allProducts.add(workHourCentrifugeOnTheMoveIndustrieProduct);
                    }

                    if (programHoursOnTheMove > 0) {
                        Product workHourProgramOnTheMoveIndustrieProduct = productService.getWorkhourForProgammationOnTheMove().get();
                        workHourProgramOnTheMoveIndustrieProduct.setSelectedAmount(Double.valueOf(programHoursOnTheMove));
                        workHourProgramOnTheMoveIndustrieProduct.setTotalPrice(Double.valueOf(programHoursOnTheMove) * workHourProgramOnTheMoveIndustrieProduct.getSellPriceIndustry());
                        workHourProgramOnTheMoveIndustrieProduct.setDate(workOrder.getWorkDateTime().toLocalDate());
                        workHourProgramOnTheMoveIndustrieProduct.setBWorkHour(true);
                        workHourProgramOnTheMoveIndustrieProduct.setTeamNumber(0);
                        allProducts.add(workHourProgramOnTheMoveIndustrieProduct);
                    }

                } else {
                    if (generalHoursLocal > 0) {
                        Product workHourRegularLocalAgroProduct = productService.getWorkhourForRegularLocal().get();
                        workHourRegularLocalAgroProduct.setSelectedAmount(Double.valueOf(generalHoursLocal));
                        workHourRegularLocalAgroProduct.setTotalPrice(Double.valueOf(generalHoursLocal) * workHourRegularLocalAgroProduct.getSellPrice());
                        workHourRegularLocalAgroProduct.setDate(workOrder.getWorkDateTime().toLocalDate());
                        workHourRegularLocalAgroProduct.setBWorkHour(true);
                        workHourRegularLocalAgroProduct.setTeamNumber(0);
                        allProducts.add(workHourRegularLocalAgroProduct);
                    }

                    if (centrifugeHoursLocal > 0) {
                        Product workHourCentrifugeLocalAgroProduct = productService.getWorkhourForCentrifugeLocal().get();
                        workHourCentrifugeLocalAgroProduct.setSelectedAmount(Double.valueOf(centrifugeHoursLocal));
                        workHourCentrifugeLocalAgroProduct.setTotalPrice(Double.valueOf(centrifugeHoursLocal) * workHourCentrifugeLocalAgroProduct.getSellPrice());
                        workHourCentrifugeLocalAgroProduct.setDate(workOrder.getWorkDateTime().toLocalDate());
                        workHourCentrifugeLocalAgroProduct.setBWorkHour(true);
                        workHourCentrifugeLocalAgroProduct.setTeamNumber(0);
                        allProducts.add(workHourCentrifugeLocalAgroProduct);
                    }

                    if (programHoursLocal > 0) {
                        Product workHourProgramLocalAgroProduct = productService.getWorkhourForProgammationLocal().get();
                        workHourProgramLocalAgroProduct.setSelectedAmount(Double.valueOf(programHoursLocal));
                        workHourProgramLocalAgroProduct.setTotalPrice(Double.valueOf(programHoursLocal) * workHourProgramLocalAgroProduct.getSellPrice());
                        workHourProgramLocalAgroProduct.setDate(workOrder.getWorkDateTime().toLocalDate());
                        workHourProgramLocalAgroProduct.setBWorkHour(true);
                        workHourProgramLocalAgroProduct.setTeamNumber(0);
                        allProducts.add(workHourProgramLocalAgroProduct);
                    }
                    if (generalHoursOnTheMove > 0) {
                        Product workHourRegularOnTheMoveAgroProduct = productService.getWorkhourForRegularOnTheMove().get();
                        workHourRegularOnTheMoveAgroProduct.setSelectedAmount(Double.valueOf(generalHoursOnTheMove));
                        workHourRegularOnTheMoveAgroProduct.setTotalPrice(Double.valueOf(generalHoursOnTheMove) * workHourRegularOnTheMoveAgroProduct.getSellPrice());
                        workHourRegularOnTheMoveAgroProduct.setDate(workOrder.getWorkDateTime().toLocalDate());
                        workHourRegularOnTheMoveAgroProduct.setBWorkHour(true);
                        workHourRegularOnTheMoveAgroProduct.setTeamNumber(0);
                        allProducts.add(workHourRegularOnTheMoveAgroProduct);
                    }

                    if (centrifugeHoursOnTheMove > 0) {
                        Product workHourCentrifugeOnTheMoveAgroProduct = productService.getWorkhourForCentrifugeOnTheMove().get();
                        workHourCentrifugeOnTheMoveAgroProduct.setSelectedAmount(Double.valueOf(centrifugeHoursOnTheMove));
                        workHourCentrifugeOnTheMoveAgroProduct.setTotalPrice(Double.valueOf(centrifugeHoursOnTheMove) * workHourCentrifugeOnTheMoveAgroProduct.getSellPrice());
                        workHourCentrifugeOnTheMoveAgroProduct.setDate(workOrder.getWorkDateTime().toLocalDate());
                        workHourCentrifugeOnTheMoveAgroProduct.setBWorkHour(true);
                        workHourCentrifugeOnTheMoveAgroProduct.setTeamNumber(0);
                        allProducts.add(workHourCentrifugeOnTheMoveAgroProduct);
                    }

                    if (programHoursOnTheMove > 0) {
                        Product workHourProgramOnTheMoveAgroProduct = productService.getWorkhourForProgammationOnTheMove().get();
                        workHourProgramOnTheMoveAgroProduct.setSelectedAmount(Double.valueOf(programHoursOnTheMove));
                        workHourProgramOnTheMoveAgroProduct.setTotalPrice(Double.valueOf(programHoursOnTheMove) * workHourProgramOnTheMoveAgroProduct.getSellPrice());
                        workHourProgramOnTheMoveAgroProduct.setDate(workOrder.getWorkDateTime().toLocalDate());
                        workHourProgramOnTheMoveAgroProduct.setBWorkHour(true);
                        workHourProgramOnTheMoveAgroProduct.setTeamNumber(0);
                        allProducts.add(workHourProgramOnTheMoveAgroProduct);
                    }
                }

                // add movement to Proforma
                Double amountKmRegular = 0.0;
                Integer amountRidesRegular = 0;
                Double amountKmTrailer = 0.0;
                Integer amountRidesTrailer = 0;
                Double amountKmCrane = 0.0;
                Integer amountRidesCrane = 0;
                Double amountHoursCraneRegular = 0.0;
                Double amountHoursCraneIntens = 0.0;
                Integer amountForfait = 0;


                for (WorkOrderHeader workOrderHeader : workOrder.getWorkOrderHeaderList()) {
                    if ((workAddress.getDistance() != null) && ((workOrderHeader.getFleet() != null) && (workOrderHeader.getFleet().equals(Fleet.VAN))) ||
                            ((workOrderHeader.getFleet() != null) && (workOrderHeader.getFleet().equals(Fleet.ATEGO)))) {
                        amountKmRegular = amountKmRegular + (workAddress.getDistance());
                        amountRidesRegular = amountRidesRegular + 1;
                    }
                    if ((workOrderHeader.getFleet() != null) && (workOrderHeader.getFleet().equals(Fleet.TRUCK_TRAILER))) {
                        amountKmTrailer = amountKmTrailer + (workAddress.getDistance());
                        amountRidesTrailer = amountRidesTrailer + 1;
                    }
                    if ((workOrderHeader.getFleet() != null) && (workOrderHeader.getFleet().equals(Fleet.TRUCK_CRANE))) {
                        amountKmCrane = amountKmCrane + (workAddress.getDistance());
                        amountRidesCrane = amountRidesCrane + 1;
                        if (workOrderHeader.getFleetWorkType().equals(FleetWorkType.DELIVERY)) {
                            amountForfait = ++amountForfait;
                        }
                    }
                    if ((workOrderHeader.getFleetHours() != null) && (workOrderHeader.getFleetHours() >= 0.0)) {
                        if (workOrderHeader.getFleetWorkType().equals(FleetWorkType.REGULAR)) {
                            amountHoursCraneRegular = amountHoursCraneRegular + workOrderHeader.getFleetHours();
                        }
                        if (workOrderHeader.getFleetWorkType().equals(FleetWorkType.INTENS)) {
                            amountHoursCraneIntens = amountHoursCraneIntens + workOrderHeader.getFleetHours();
                        }
                    }
                }


                if (optCustomer.get().getFirst().getBIndustry() == true) {
                    if (amountKmRegular > 0.0) {
                        Product regularKm = productService.getRegularKm().get();
                        regularKm.setSelectedAmount(amountKmRegular);
                        regularKm.setTotalPrice(amountKmRegular * regularKm.getSellPriceIndustry());
                        regularKm.setTeamNumber(0);
                        regularKm.setBTravel(true);
                        regularKm.setDate(workOrder.getWorkDateTime().toLocalDate());
                        if(amountRidesRegular > 1) {
                            regularKm.setInternalName(regularKm.getInternalName() + "("+ amountRidesRegular + " x heen en terug)");
                        }
                        allProducts.add(regularKm);
                    }
                    if (amountKmTrailer > 0.0) {
                        Product forfaitTrailer = productService.getWorkHoursTrailerForfait().get();
                        forfaitTrailer.setSelectedAmount(1.0);
                        forfaitTrailer.setTotalPrice(forfaitTrailer.getSellPriceIndustry());
                        forfaitTrailer.setTeamNumber(0);
                        forfaitTrailer.setBTravel(true);
                        forfaitTrailer.setDate(workOrder.getWorkDateTime().toLocalDate());
                        allProducts.add(forfaitTrailer);

                        Product trailerKm = productService.getRegularTrailer().get();
                        trailerKm.setSelectedAmount(amountKmTrailer);
                        trailerKm.setTotalPrice(amountKmTrailer * trailerKm.getSellPriceIndustry());
                        trailerKm.setTeamNumber(0);
                        trailerKm.setBTravel(true);
                        trailerKm.setDate(workOrder.getWorkDateTime().toLocalDate());
                        if(amountRidesTrailer > 1) {
                            trailerKm.setInternalName(trailerKm.getInternalName() + "("+ amountRidesTrailer + " x heen en terug)");
                        }
                        allProducts.add(trailerKm);
                    }
                    if (amountKmCrane > 0.0) {
                        Product craneKm = productService.getRegularCrane().get();
                        craneKm.setSelectedAmount(amountKmCrane);
                        craneKm.setTotalPrice(amountKmCrane * craneKm.getSellPriceIndustry());
                        craneKm.setTeamNumber(0);
                        craneKm.setBTravel(true);
                        craneKm.setDate(workOrder.getWorkDateTime().toLocalDate());
                        if(amountRidesCrane > 1) {
                            craneKm.setInternalName(craneKm.getInternalName() + "("+ amountRidesCrane + " x heen en terug)");
                        }
                        allProducts.add(craneKm);
                    }
                    if ((amountHoursCraneRegular > 0.0) && (amountHoursCraneRegular >= 3.0)) {
                        Product hoursCraneRegularIndustry = productService.getWorkHoursCraneRegular().get();
                        hoursCraneRegularIndustry.setSelectedAmount(amountHoursCraneRegular);
                        hoursCraneRegularIndustry.setTotalPrice(amountHoursCraneRegular * hoursCraneRegularIndustry.getSellPriceIndustry());
                        hoursCraneRegularIndustry.setTeamNumber(0);
                        hoursCraneRegularIndustry.setBTravel(true);
                        hoursCraneRegularIndustry.setDate(workOrder.getWorkDateTime().toLocalDate());
                        allProducts.add(hoursCraneRegularIndustry);
                    }
                    else if ((amountHoursCraneRegular > 0.0) && (amountHoursCraneRegular < 3.0)) {
                        Product hoursCraneRegularIndustry = productService.getWorkHoursCraneRegular().get();
                        hoursCraneRegularIndustry.setSelectedAmount(3.0);
                        hoursCraneRegularIndustry.setTotalPrice(3.0 * hoursCraneRegularIndustry.getSellPriceIndustry());
                        hoursCraneRegularIndustry.setTeamNumber(0);
                        hoursCraneRegularIndustry.setBTravel(true);
                        hoursCraneRegularIndustry.setDate(workOrder.getWorkDateTime().toLocalDate());
                        allProducts.add(hoursCraneRegularIndustry);
                    }
                    else if ((amountHoursCraneIntens > 0.0) && (amountHoursCraneIntens >= 4.0)) {
                        Product hoursCraneIntenseIndustry = productService.getWorkHoursCraneIntense().get();
                        hoursCraneIntenseIndustry.setSelectedAmount(amountHoursCraneIntens);
                        hoursCraneIntenseIndustry.setTotalPrice(amountHoursCraneIntens * hoursCraneIntenseIndustry.getSellPriceIndustry());
                        hoursCraneIntenseIndustry.setTeamNumber(0);
                        hoursCraneIntenseIndustry.setBTravel(true);
                        hoursCraneIntenseIndustry.setDate(workOrder.getWorkDateTime().toLocalDate());
                        allProducts.add(hoursCraneIntenseIndustry);
                    }
                    else if ((amountHoursCraneIntens > 0.0) && (amountHoursCraneIntens < 4.0)) {
                        Product hoursCraneIntenseIndustry = productService.getWorkHoursCraneIntense().get();
                        hoursCraneIntenseIndustry.setSelectedAmount(4.0);
                        hoursCraneIntenseIndustry.setTotalPrice(4.0 * hoursCraneIntenseIndustry.getSellPriceIndustry());
                        hoursCraneIntenseIndustry.setTeamNumber(0);
                        hoursCraneIntenseIndustry.setBTravel(true);
                        hoursCraneIntenseIndustry.setDate(workOrder.getWorkDateTime().toLocalDate());
                        allProducts.add(hoursCraneIntenseIndustry);
                    }
                    if (amountForfait > 0) {
                        Product hoursCraneForfaitIndustry = productService.getWorkHoursCraneForfait().get();
                        hoursCraneForfaitIndustry.setSelectedAmount(Double.valueOf(amountForfait));
                        hoursCraneForfaitIndustry.setTotalPrice(amountForfait * hoursCraneForfaitIndustry.getSellPriceIndustry());
                        hoursCraneForfaitIndustry.setTeamNumber(0);
                        hoursCraneForfaitIndustry.setBTravel(true);
                        hoursCraneForfaitIndustry.setDate(workOrder.getWorkDateTime().toLocalDate());
                        allProducts.add(hoursCraneForfaitIndustry);
                    }
                } else {
                    if (amountKmRegular > 0.0) {
                        Product regularKm = productService.getRegularKm().get();
                        regularKm.setSelectedAmount(amountKmRegular);
                        regularKm.setTotalPrice(amountKmRegular * regularKm.getSellPrice());
                        regularKm.setTeamNumber(0);
                        regularKm.setBTravel(true);
                        regularKm.setDate(workOrder.getWorkDateTime().toLocalDate());
                        if(amountRidesRegular > 1) {
                            regularKm.setInternalName(regularKm.getInternalName() + "("+ amountRidesRegular + " x heen en terug)");
                        }
                        allProducts.add(regularKm);
                    }
                    if (amountKmTrailer > 0.0) {

                        Product forfaitTrailer = productService.getWorkHoursTrailerForfait().get();
                        forfaitTrailer.setSelectedAmount(amountKmTrailer);
                        forfaitTrailer.setTotalPrice(forfaitTrailer.getSellPrice());
                        forfaitTrailer.setTeamNumber(0);
                        forfaitTrailer.setBTravel(true);
                        forfaitTrailer.setDate(workOrder.getWorkDateTime().toLocalDate());
                        allProducts.add(forfaitTrailer);


                        Product trailerKm = productService.getRegularTrailer().get();
                        trailerKm.setSelectedAmount(amountKmTrailer);
                        trailerKm.setTotalPrice(amountKmTrailer * trailerKm.getSellPrice());
                        trailerKm.setTeamNumber(0);
                        trailerKm.setBTravel(true);
                        trailerKm.setDate(workOrder.getWorkDateTime().toLocalDate());
                        if(amountRidesTrailer > 1) {
                            trailerKm.setInternalName(trailerKm.getInternalName() + "("+ amountRidesTrailer + " x heen en terug)");
                        }
                        allProducts.add(trailerKm);
                    }
                    if (amountKmCrane > 0.0) {
                        Product craneKm = productService.getRegularCrane().get();
                        craneKm.setSelectedAmount(amountKmCrane);
                        craneKm.setTotalPrice(amountKmCrane * craneKm.getSellPrice());
                        craneKm.setTeamNumber(0);
                        craneKm.setBTravel(true);
                        craneKm.setDate(workOrder.getWorkDateTime().toLocalDate());
                        if(amountRidesCrane > 1) {
                            craneKm.setInternalName(craneKm.getInternalName() + "("+ amountRidesCrane + " x heen en terug)");
                        }
                        allProducts.add(craneKm);
                    }
                    if ((amountHoursCraneRegular > 0.0) && (amountHoursCraneRegular >= 3.0)) {
                        Product hoursCraneRegularIndustry = productService.getWorkHoursCraneRegular().get();
                        hoursCraneRegularIndustry.setSelectedAmount(amountHoursCraneRegular);
                        hoursCraneRegularIndustry.setTotalPrice(amountHoursCraneRegular * hoursCraneRegularIndustry.getSellPrice());
                        hoursCraneRegularIndustry.setTeamNumber(0);
                        hoursCraneRegularIndustry.setBTravel(true);
                        hoursCraneRegularIndustry.setDate(workOrder.getWorkDateTime().toLocalDate());
                        allProducts.add(hoursCraneRegularIndustry);
                    }
                    else if ((amountHoursCraneRegular > 0.0) && (amountHoursCraneRegular < 3.0)) {
                        Product hoursCraneRegularIndustry = productService.getWorkHoursCraneRegular().get();
                        hoursCraneRegularIndustry.setSelectedAmount(3.0);
                        hoursCraneRegularIndustry.setTotalPrice(3.0 * hoursCraneRegularIndustry.getSellPrice());
                        hoursCraneRegularIndustry.setTeamNumber(0);
                        hoursCraneRegularIndustry.setBTravel(true);
                        hoursCraneRegularIndustry.setDate(workOrder.getWorkDateTime().toLocalDate());
                        allProducts.add(hoursCraneRegularIndustry);
                    }
                    else if ((amountHoursCraneIntens > 0.0) && (amountHoursCraneIntens >= 4.0)) {
                        Product hoursCraneIntenseIndustry = productService.getWorkHoursCraneIntense().get();
                        hoursCraneIntenseIndustry.setSelectedAmount(amountHoursCraneIntens);
                        hoursCraneIntenseIndustry.setTotalPrice(amountHoursCraneIntens * hoursCraneIntenseIndustry.getSellPrice());
                        hoursCraneIntenseIndustry.setTeamNumber(0);
                        hoursCraneIntenseIndustry.setBTravel(true);
                        hoursCraneIntenseIndustry.setDate(workOrder.getWorkDateTime().toLocalDate());
                        allProducts.add(hoursCraneIntenseIndustry);
                    }
                    else if ((amountHoursCraneIntens > 0.0) && (amountHoursCraneIntens < 4.0)) {
                        Product hoursCraneIntenseIndustry = productService.getWorkHoursCraneIntense().get();
                        hoursCraneIntenseIndustry.setSelectedAmount(4.0);
                        hoursCraneIntenseIndustry.setTotalPrice(4.0 * hoursCraneIntenseIndustry.getSellPrice());
                        hoursCraneIntenseIndustry.setTeamNumber(0);
                        hoursCraneIntenseIndustry.setBTravel(true);
                        hoursCraneIntenseIndustry.setDate(workOrder.getWorkDateTime().toLocalDate());
                        allProducts.add(hoursCraneIntenseIndustry);
                    }
                    if (amountForfait > 0) {
                        Product hoursCraneForfaitIndustry = productService.getWorkHoursCraneForfait().get();
                        hoursCraneForfaitIndustry.setSelectedAmount(Double.valueOf(amountForfait));
                        hoursCraneForfaitIndustry.setTotalPrice(amountForfait * hoursCraneForfaitIndustry.getSellPrice());
                        hoursCraneForfaitIndustry.setTeamNumber(0);
                        hoursCraneForfaitIndustry.setBTravel(true);
                        hoursCraneForfaitIndustry.setDate(workOrder.getWorkDateTime().toLocalDate());
                        allProducts.add(hoursCraneForfaitIndustry);
                    }
                }

                //add roadTax / Tunneltax to workorder

                Double totalTax = 0.0;
                Double totalTunnelTax = 0.0;

                for (WorkOrderHeader workOrderHeader : workOrder.getWorkOrderHeaderList()) {
                    Double roadTax = 0.0;
                    Double tunnelTax = 0.0;
                    if ((workOrderHeader.getFleet() != null) && (workOrderHeader.getFleet().equals(Fleet.VAN))) {
                        if (workOrderHeader.getRoadTax() != null) {
                            roadTax = workOrderHeader.getRoadTax();
                        } else {
                            roadTax = 0.0;
                        }
                        if (workOrderHeader.getTunnelTax() != null) {
                            tunnelTax = workOrderHeader.getTunnelTax();
                        } else {
                            tunnelTax = 0.0;
                        }
                    }
                    if ((workOrderHeader.getFleet() != null) && (workOrderHeader.getFleet().equals(Fleet.ATEGO))) {

                        if (workOrderHeader.getRoadTax() != null) {
                            if ((workAddress.getRoadTaxAtego() != null) && (workAddress.getRoadTaxAtego() > workOrderHeader.getRoadTax())) {
                                roadTax = workAddress.getRoadTaxAtego();
                            } else {
                                roadTax = workOrderHeader.getRoadTax();
                            }
                        } else {
                            roadTax = workAddress.getRoadTaxAtego();
                        }
                        if (workOrderHeader.getTunnelTax() != null) {
                            tunnelTax = workOrderHeader.getTunnelTax();
                        } else {
                            tunnelTax = 0.0;
                        }
                    }
                    if ((workOrderHeader.getFleet() != null) && (workOrderHeader.getFleet().equals(Fleet.TRUCK_TRAILER))) {
                        if (workOrderHeader.getRoadTax() != null) {
                            if ((workAddress.getRoadTaxActros() != null) && (workAddress.getRoadTaxActros() > workOrderHeader.getRoadTax())) {
                                roadTax = workAddress.getRoadTaxActros();
                            } else {
                                roadTax = workOrderHeader.getRoadTax();
                            }
                        } else {
                            roadTax = workAddress.getRoadTaxActros();
                        }
                        if (workOrderHeader.getTunnelTax() != null) {
                            tunnelTax = workOrderHeader.getTunnelTax();
                        } else {
                            tunnelTax = 0.0;
                        }
                    }
                    if ((workOrderHeader.getFleet() != null) && (workOrderHeader.getFleet().equals(Fleet.TRUCK_CRANE))) {
                        if (workOrderHeader.getRoadTax() != null) {
                            if ((workAddress.getRoadTaxArocs() != null) && (workAddress.getRoadTaxArocs() > workOrderHeader.getRoadTax())) {
                                roadTax = workAddress.getRoadTaxArocs();
                            } else {
                                roadTax = workOrderHeader.getRoadTax();
                            }
                        } else {
                            roadTax = workAddress.getRoadTaxArocs();
                        }
                        if (workOrderHeader.getTunnelTax() != null) {
                            tunnelTax = workOrderHeader.getTunnelTax();
                        } else {
                            tunnelTax = 0.0;
                        }
                    }
                    totalTax += roadTax;
                    totalTunnelTax += tunnelTax;
                }


                if (totalTax > 0.0) {
                    Product roadTaxProduct = new Product();
                    roadTaxProduct.setSelectedAmount(1.0);
                    roadTaxProduct.setInternalName("Wegentaks");
                    roadTaxProduct.setSellPrice(totalTax);
                    roadTaxProduct.setTotalPrice(1.0 * totalTax);
                    roadTaxProduct.setBTravel(true);
                    roadTaxProduct.setVat(VAT.EENENTWINTIG);
                    roadTaxProduct.setTeamNumber(0);
                    roadTaxProduct.setDate(workOrder.getWorkDateTime().toLocalDate());
                    allProducts.add(roadTaxProduct);
                }

                if (totalTunnelTax > 0.0) {
                    Product tunnelTaxProduct = new Product();
                    tunnelTaxProduct.setSelectedAmount(1.0);
                    tunnelTaxProduct.setInternalName("tunneltaks");
                    tunnelTaxProduct.setSellPrice(totalTunnelTax);
                    tunnelTaxProduct.setTotalPrice(1.0 * totalTunnelTax);
                    tunnelTaxProduct.setBTravel(true);
                    tunnelTaxProduct.setVat(VAT.EENENTWINTIG);
                    tunnelTaxProduct.setTeamNumber(0);
                    tunnelTaxProduct.setDate(workOrder.getWorkDateTime().toLocalDate());
                    allProducts.add(tunnelTaxProduct);
                }

                Product emptyProduct1 = new Product();
                emptyProduct1.setTeamNumber(0);
                emptyProduct1.setBComment(true);
                emptyProduct1.setInternalName("");
                emptyProduct1.setDate(workOrder.getWorkDateTime().toLocalDate());
                allProducts.add(emptyProduct1);

                invoice.setProductList(allProducts);

                checkIfToolsHoursAreSubtractedFromWorkOrder(invoice);
            }
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
            if(invoice.getBFinalInvoice() != null){
                parameters.put("factuurNummer", invoice.getInvoiceNumber().toString());
            }
            else{
                parameters.put("factuurNummer", invoice.getFinalInvoiceNumber().toString());
            }
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

        try {
            parameters.put("vervalDatum", invoice.getExpiryDate().format(FORMATTER));
        }
        catch (Exception e){
            Notification.show("Gelieve voor een volledige BTW nummer te zorgen aub");
        }

        //only show dates at the beginning / rest of block set as null
        LocalDate vorigeDatum = null;

        for (Product item : invoice.getProductList()) {
            if (item.getDate().equals(vorigeDatum)) {
                item.setShowDate(false);
            } else {
                vorigeDatum = item.getDate();
            }
        }

        List<Product>totalProductList = new ArrayList<>(invoice.getProductList());

        List<Product> attachments = totalProductList.stream()
                .filter(product -> {
                    if((product.getBAttachement() == null) || (product.getBAttachement() == false)){
                        return false;
                    }
                    else{
                        return true;
                    }
                })
                .collect(Collectors.toList());


        List<Product> products = totalProductList.stream()
                .filter(product -> {
                    if((product.getBAttachement() == null) || (product.getBAttachement() == false)){
                        return true;
                    }
                    else{
                        return false;
                    }
                })
                .collect(Collectors.toList());

        //generate pointer for every different attachement (every attachement has a date id!)

        if((attachments != null) && (attachments.size() > 0)){

            List<LocalDate> uniqueDates = attachments.stream()
                    .map(Product::getAttachementNumber)
                    .distinct()
                    .collect(Collectors.toList());

            for(LocalDate uniqueDate : uniqueDates){

                Product pointerProduct = new Product();
                pointerProduct.setTeamNumber(0);
                pointerProduct.setSelectedAmount(1.0);
                pointerProduct.setTotalPrice(attachments.stream()
                                .filter(product -> product.getAttachementNumber().equals(uniqueDate))
                        .map(Product::getTotalPrice)
                        .filter(Objects::nonNull)
                        .mapToDouble(Double::doubleValue)
                        .sum());
                pointerProduct.setVat(attachments.get(0).getVat());
                pointerProduct.setInternalName("materiaal (zie bijlage)");
                OptionalInt indexWorkHoursOpt =
                        IntStream.range(0, products.size())
                                .filter(i -> products.get(i).getBWorkHour() != null)
                                .filter(i -> (products.get(i).getBWorkHour()) && (products.get(i).getDate() != null) && (products.get(i).getDate().equals(uniqueDate)))
                                .reduce((first, second) -> second);

                OptionalInt indexCommentOpt =
                        IntStream.range(0, products.size())
                            .filter(i -> products.get(i).getBComment() != null)
                            .filter(i -> (products.get(i).getBComment()) && (products.get(i).getDate() != null) && (products.get(i).getDate().equals(uniqueDate)))
                            .reduce((first, second) -> second);


                if (indexWorkHoursOpt.isPresent()) {
                    int index = indexWorkHoursOpt.getAsInt();
                    products.add(index + 1, pointerProduct);
                } else if (indexCommentOpt.isPresent()) {
                    int index = indexCommentOpt.getAsInt();
                    products.add(index + 1, pointerProduct);
                }
                else{
                    products.add( pointerProduct);
                }

            }
        }

        Collections.reverse(products);
        DataImplementation dataImplementation = new DataImplementation(products,invoice.getCustomer());

        parameters.put( "ItemDataSource", dataImplementation );
        parameters.put("netto",invoice.getProductList().stream()
                        .filter(product -> product.getTotalPrice() != null)
                .mapToDouble(Product::getTotalPrice)
                .sum());

        parameters.put("btwBedrag",invoice.getProductList().stream()
                .filter(product -> product.getTotalPrice() != null)
                .mapToDouble(x -> (x.getTotalPrice() * x.getVat().getValue())/100)
                .sum());

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

        //now show it in a new tab in the browser
        pdfController.setPdfNaam("invoice_"+ invoice.getInvoiceNumber()+".pdf");

        UI.getCurrent().getPage().open("/pdf", "_blank");

        // now generate the attachement
        if(attachments.size() > 0){

            attachementNumber = 0;

            List<LocalDate> uniqueDates = attachments.stream()
                    .map(Product::getAttachementNumber)
                    .distinct()
                    .collect(Collectors.toList());


            attachementNames.clear();
            for(LocalDate date : uniqueDates){
                generateAttachement(date, invoice,attachments.stream().filter(product -> product.getAttachementNumber().equals(date)).collect(Collectors.toList()), attachementNumber);
                UI.getCurrent().getPage().open("/attachement/" + attachementNumber, "_blank");
                attachementNumber++;
            }
        }
    }

    private void generateAttachement(LocalDate datum, Invoice invoice, List<Product> attachments, Integer attachementNumber) {
        JasperReport jasperReportAttachement = null;
        JasperPrint jasperPrintAttachement = null;

        try {
            jasperReportAttachement = JasperCompileManager.compileReport( attachementResourceJRXML.getInputStream() );
        } catch (JRException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            Notification.show("Kan de Attachement- JRXML template niet vinden op de server!");
        }

        parameters.clear();

        try {
            parameters.put("factuurNummer", String.valueOf(invoice.getInvoiceNumber()));
        }
        catch (Exception e){
            Notification.show("Gelieve voor een klantnaam in te geven aub");
        }

        try {
            parameters.put("datum",datum.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) );
        }
        catch (Exception e){
            Notification.show("Gelieve een bijlagenaam in te geven aub");
        }
        DataImplementation dataImplementation = new DataImplementation(attachments,invoice.getCustomer());
        parameters.put( "ItemDataSource", dataImplementation );

        try {
            jasperPrintAttachement  = JasperFillManager.fillReport(jasperReportAttachement, parameters, new JREmptyDataSource(  ));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        String exportName = rootFolder + "attachement_"+ attachementNumber+".pdf";

        try {
            JasperExportManager.exportReportToPdfFile( jasperPrintAttachement, exportName );
        } catch (JRException e) {
            throw new RuntimeException(e);
        }

        //now show it in a new tab in the browser
        attachementNames.add("attachement_"+ attachementNumber+".pdf");
        pdfController.setAttachementNames(attachementNames);
    }

    public void checkIfToolsHoursAreSubtractedFromWorkOrder(Invoice selectedInvoice){
        if((selectedInvoice != null) && (selectedInvoice.getProductList() != null)){
            if(selectedInvoice.getProductList().size() > 0){

                Optional<Product> optWorkHoursProduct = selectedInvoice.getProductList().stream().filter(product -> (product.getProductCode().contains("WU")) && (product.getBWorkHour() == true)).findFirst();

                List<Product> laserListToSubtract = selectedInvoice.getProductList().stream().filter(product -> product.getProductCode().matches("OPAT-laser")).collect(Collectors.toList());
                List<Product> bendListToSubtract = selectedInvoice.getProductList().stream().filter(product -> product.getProductCode().matches("OPAT-plooi")).collect(Collectors.toList());
                List<Product> laserbendListToSubtract = selectedInvoice.getProductList().stream().filter(product -> product.getProductCode().matches("OPAT-laser-plooi")).collect(Collectors.toList());
                List<Product> cncToSubtract = selectedInvoice.getProductList().stream().filter(product -> product.getProductCode().matches("OPAT-dr-fr")).collect(Collectors.toList());
                List<Product> cncCuttingListToSubtract = selectedInvoice.getProductList().stream().filter(product -> product.getProductCode().matches("OPAT-cncsn")).collect(Collectors.toList());


                if((laserListToSubtract != null) && (laserListToSubtract.size() > 0)){
                    Double totalLaserMinutesToSubtract = laserListToSubtract.stream()
                            .mapToDouble(item -> item.getSelectedAmount())
                            .sum();

//                    Product productToAdd = productService.findByProductCodeEqualCaseInsensitive("OPAT-laser").get().getFirst();
//                    productToAdd.setSelectedAmount(totalLaserMinutesToSubtract);

//                    productToAdd.setVat(VAT.EENENTWINTIG);
//                    productToAdd.setBWorkHour(true);
//                    selectedInvoice.getProductList().remove(laserListToSubtract);
//                    selectedInvoice.getProductList().add(productToAdd);

                    if((optWorkHoursProduct.isPresent()) && (!optWorkHoursProduct.isEmpty())){
                        Double correctedAmountWorkedHours = optWorkHoursProduct.get().getSelectedAmount() - totalLaserMinutesToSubtract;
                        optWorkHoursProduct.get().setSelectedAmount(correctedAmountWorkedHours);
                        if((selectedInvoice.getCustomer().getBIndustry()!= null) && (selectedInvoice.getCustomer().getBIndustry() == true)){
                            if(optWorkHoursProduct.get().getSellPriceIndustry() > 0.0){
                                optWorkHoursProduct.get().setTotalPrice(correctedAmountWorkedHours * optWorkHoursProduct.get().getSellPriceIndustry());
                            }
                            else{
                                optWorkHoursProduct.get().setTotalPrice(correctedAmountWorkedHours * optWorkHoursProduct.get().getSellPrice());
                            }
                        }
                        else{
                            optWorkHoursProduct.get().setTotalPrice(correctedAmountWorkedHours * optWorkHoursProduct.get().getSellPrice());
                        }
                    }
                }

                if((bendListToSubtract != null) && (bendListToSubtract.size() > 0)){
                    Double totalBendMinutesToSubtract = laserListToSubtract.stream()
                            .mapToDouble(item -> item.getSelectedAmount())
                            .sum();

//                    Product productToAdd = productService.findByProductCodeEqualCaseInsensitive("OPAT-plooi").get().getFirst();
//                    productToAdd.setSelectedAmount(totalBendMinutesToSubtract);
//                    if((selectedInvoice.getCustomer().getBIndustry()!= null) && (selectedInvoice.getCustomer().getBIndustry() == true)){
//                        if(productToAdd.getSellPriceIndustry() > 0.0){
//                            productToAdd.setTotalPrice(totalBendMinutesToSubtract * productToAdd.getSellPriceIndustry());
//                        }
//                        else{
//                            productToAdd.setTotalPrice(totalBendMinutesToSubtract * productToAdd.getSellPrice());
//                        }
//                    }
//                    else{
//                        productToAdd.setTotalPrice(totalBendMinutesToSubtract * productToAdd.getSellPrice());
//                    }
//                    productToAdd.setVat(VAT.EENENTWINTIG);
//                    productToAdd.setBWorkHour(true);
//                    selectedInvoice.getProductList().remove(bendListToSubtract);
//                    selectedInvoice.getProductList().add(productToAdd);

                    if((optWorkHoursProduct.isPresent()) && (!optWorkHoursProduct.isEmpty())){
                        Double correctedAmountWorkedHours = optWorkHoursProduct.get().getSelectedAmount() - totalBendMinutesToSubtract;
                        optWorkHoursProduct.get().setSelectedAmount(correctedAmountWorkedHours);

                        if((selectedInvoice.getCustomer().getBIndustry()!= null) && (selectedInvoice.getCustomer().getBIndustry() == true)){
                            if(optWorkHoursProduct.get().getSellPriceIndustry() > 0.0){
                                optWorkHoursProduct.get().setTotalPrice(correctedAmountWorkedHours * optWorkHoursProduct.get().getSellPriceIndustry());
                            }
                            else{
                                optWorkHoursProduct.get().setTotalPrice(correctedAmountWorkedHours * optWorkHoursProduct.get().getSellPrice());
                            }
                        }
                        else{
                            optWorkHoursProduct.get().setTotalPrice(correctedAmountWorkedHours * optWorkHoursProduct.get().getSellPrice());
                        }
                    }
                }

                if((laserbendListToSubtract != null) && (laserbendListToSubtract.size() > 0)){
                    Double totalLaserBendMinutesToSubtract = laserListToSubtract.stream()
                            .mapToDouble(item -> item.getSelectedAmount())
                            .sum();

//                    Product productToAdd = productService.findByProductCodeEqualCaseInsensitive("OPAT-laser-plooi").get().getFirst();
//                    productToAdd.setSelectedAmount(totalLaserBendMinutesToSubtract);
//                    if((selectedInvoice.getCustomer().getBIndustry()!= null) && (selectedInvoice.getCustomer().getBIndustry() == true)){
//                        if(productToAdd.getSellPriceIndustry() > 0.0){
//                            productToAdd.setTotalPrice(totalLaserBendMinutesToSubtract * productToAdd.getSellPriceIndustry());
//                        }
//                        else{
//                            productToAdd.setTotalPrice(totalLaserBendMinutesToSubtract * productToAdd.getSellPrice());
//                        }
//                    }
//                    else{
//                        productToAdd.setTotalPrice(totalLaserBendMinutesToSubtract * productToAdd.getSellPrice());
//                    }
//                    productToAdd.setVat(VAT.EENENTWINTIG);
//                    productToAdd.setBWorkHour(true);
//                    selectedInvoice.getProductList().remove(laserbendListToSubtract);
//                    selectedInvoice.getProductList().add(productToAdd);

                    if((optWorkHoursProduct.isPresent()) && (!optWorkHoursProduct.isEmpty())){
                        Double correctedAmountWorkedHours = optWorkHoursProduct.get().getSelectedAmount() - totalLaserBendMinutesToSubtract;
                        optWorkHoursProduct.get().setSelectedAmount(correctedAmountWorkedHours);

                        if((selectedInvoice.getCustomer().getBIndustry()!= null) && (selectedInvoice.getCustomer().getBIndustry() == true)){
                            if(optWorkHoursProduct.get().getSellPriceIndustry() > 0.0){
                                optWorkHoursProduct.get().setTotalPrice(correctedAmountWorkedHours * optWorkHoursProduct.get().getSellPriceIndustry());
                            }
                            else{
                                optWorkHoursProduct.get().setTotalPrice(correctedAmountWorkedHours * optWorkHoursProduct.get().getSellPrice());
                            }
                        }
                        else{
                            optWorkHoursProduct.get().setTotalPrice(correctedAmountWorkedHours * optWorkHoursProduct.get().getSellPrice());
                        }
                    }
                }

                if((cncToSubtract != null) && (cncToSubtract.size() > 0)){
                    Double totalCncMinutesToSubtract = cncToSubtract.stream()
                            .mapToDouble(item -> item.getSelectedAmount())
                            .sum();

//                    Product productToAdd = productService.findByProductCodeEqualCaseInsensitive("OPAT-laser-plooi").get().getFirst();
//                    productToAdd.setSelectedAmount(totalLaserBendMinutesToSubtract);
//                    if((selectedInvoice.getCustomer().getBIndustry()!= null) && (selectedInvoice.getCustomer().getBIndustry() == true)){
//                        if(productToAdd.getSellPriceIndustry() > 0.0){
//                            productToAdd.setTotalPrice(totalLaserBendMinutesToSubtract * productToAdd.getSellPriceIndustry());
//                        }
//                        else{
//                            productToAdd.setTotalPrice(totalLaserBendMinutesToSubtract * productToAdd.getSellPrice());
//                        }
//                    }
//                    else{
//                        productToAdd.setTotalPrice(totalLaserBendMinutesToSubtract * productToAdd.getSellPrice());
//                    }
//                    productToAdd.setVat(VAT.EENENTWINTIG);
//                    productToAdd.setBWorkHour(true);
//                    selectedInvoice.getProductList().remove(laserbendListToSubtract);
//                    selectedInvoice.getProductList().add(productToAdd);

                    if((optWorkHoursProduct.isPresent()) && (!optWorkHoursProduct.isEmpty())){
                        Double correctedAmountWorkedHours = optWorkHoursProduct.get().getSelectedAmount() - totalCncMinutesToSubtract;
                        optWorkHoursProduct.get().setSelectedAmount(correctedAmountWorkedHours);

                        if((selectedInvoice.getCustomer().getBIndustry()!= null) && (selectedInvoice.getCustomer().getBIndustry() == true)){
                            if(optWorkHoursProduct.get().getSellPriceIndustry() > 0.0){
                                optWorkHoursProduct.get().setTotalPrice(correctedAmountWorkedHours * optWorkHoursProduct.get().getSellPriceIndustry());
                            }
                            else{
                                optWorkHoursProduct.get().setTotalPrice(correctedAmountWorkedHours * optWorkHoursProduct.get().getSellPrice());
                            }
                        }
                        else{
                            optWorkHoursProduct.get().setTotalPrice(correctedAmountWorkedHours * optWorkHoursProduct.get().getSellPrice());
                        }

                    }
                }

                if((cncCuttingListToSubtract != null) && (cncCuttingListToSubtract.size() > 0)){
                    Double totalCncCuttingMinutesToSubtract = cncCuttingListToSubtract.stream()
                            .mapToDouble(item -> item.getSelectedAmount())
                            .sum();

//                    Product productToAdd = productService.findByProductCodeEqualCaseInsensitive("OPAT-laser-plooi").get().getFirst();
//                    productToAdd.setSelectedAmount(totalLaserBendMinutesToSubtract);
//                    if((selectedInvoice.getCustomer().getBIndustry()!= null) && (selectedInvoice.getCustomer().getBIndustry() == true)){
//                        if(productToAdd.getSellPriceIndustry() > 0.0){
//                            productToAdd.setTotalPrice(totalLaserBendMinutesToSubtract * productToAdd.getSellPriceIndustry());
//                        }
//                        else{
//                            productToAdd.setTotalPrice(totalLaserBendMinutesToSubtract * productToAdd.getSellPrice());
//                        }
//                    }
//                    else{
//                        productToAdd.setTotalPrice(totalLaserBendMinutesToSubtract * productToAdd.getSellPrice());
//                    }
//                    productToAdd.setVat(VAT.EENENTWINTIG);
//                    productToAdd.setBWorkHour(true);
//                    selectedInvoice.getProductList().remove(laserbendListToSubtract);
//                    selectedInvoice.getProductList().add(productToAdd);

                    if((optWorkHoursProduct.isPresent()) && (!optWorkHoursProduct.isEmpty())){
                        Double correctedAmountWorkedHours = optWorkHoursProduct.get().getSelectedAmount() - totalCncCuttingMinutesToSubtract;
                        optWorkHoursProduct.get().setSelectedAmount(correctedAmountWorkedHours);

                        if((selectedInvoice.getCustomer().getBIndustry()!= null) && (selectedInvoice.getCustomer().getBIndustry() == true)){
                            if(optWorkHoursProduct.get().getSellPriceIndustry() > 0.0){
                                optWorkHoursProduct.get().setTotalPrice(correctedAmountWorkedHours * optWorkHoursProduct.get().getSellPriceIndustry());
                            }
                            else{
                                optWorkHoursProduct.get().setTotalPrice(correctedAmountWorkedHours * optWorkHoursProduct.get().getSellPrice());
                            }
                        }
                        else{
                            optWorkHoursProduct.get().setTotalPrice(correctedAmountWorkedHours * optWorkHoursProduct.get().getSellPrice());
                        }
                    }
                }
            }
        }
    }
}
