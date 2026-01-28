package com.adverto.dejonghe.application.services.workorder;

import com.adverto.dejonghe.application.entities.WorkOrder.BowlEntity;
import com.adverto.dejonghe.application.entities.WorkOrder.WorkOrder;
import com.adverto.dejonghe.application.entities.WorkOrder.WorkOrderHeader;
import com.adverto.dejonghe.application.entities.WorkOrder.WorkOrderTime;
import com.adverto.dejonghe.application.entities.enums.fleet.Fleet;
import com.adverto.dejonghe.application.entities.enums.fleet.FleetWorkType;
import com.adverto.dejonghe.application.entities.enums.workorder.WorkLocation;
import com.adverto.dejonghe.application.entities.enums.workorder.WorkType;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WorkOrderServices {

    List<String>errorList;

    public WorkOrderServices() {

    }
    
    public List<String> checkWorkOrderBeforeSendToInvoice(WorkOrder workOrder, List<String>errorList){

        this.errorList = errorList;

        errorList.clear();

        checkWorkOrder(workOrder);

        if(workOrder.getMasterEmployeeTeam1() != null){
            checkHeader("Team 1", workOrder.getWorkLocation(), workOrder.getWorkOrderHeaderList().get(0));
        }
        if(workOrder.getMasterEmployeeTeam2() != null){
            checkHeader("Team 2", workOrder.getWorkLocation(),workOrder.getWorkOrderHeaderList().get(1));
        }
        if(workOrder.getMasterEmployeeTeam3() != null){
            checkHeader("Team 3", workOrder.getWorkLocation(),workOrder.getWorkOrderHeaderList().get(2));
        }
        if(workOrder.getMasterEmployeeTeam4() != null){
            checkHeader("Team 4", workOrder.getWorkLocation(),workOrder.getWorkOrderHeaderList().get(3));
        }
        return errorList;
    }

    private void checkWorkOrder(WorkOrder workOrder) {
        if(workOrder.getWorkAddress() == null){
            errorList.add("Klant is niet ingevuld!");
        }
        if(workOrder.getWorkDateTime() == null){
            errorList.add("Datum is niet ingevuld!");
        }
        if(workOrder.getWorkLocation() == null){
            errorList.add("Locatie is niet ingevuld!");
        }
    }

    private void checkHeader(String teamNumber, WorkLocation workLocation, WorkOrderHeader workOrderHeader) {

        if(workOrderHeader.getWorkType() == null){
            errorList.add(teamNumber + " Type Werk is niet ingevuld!");
        }
        if(workOrderHeader.getWorkType().equals(WorkType.CENTRIFUGE)){
            if((workOrderHeader.getBowlEntityList() != null) && (workOrderHeader.getBowlEntityList().size() > 0)){
                for(BowlEntity bowlEntity : workOrderHeader.getBowlEntityList()){
                    if((bowlEntity.getChassisNumber() == null) || (bowlEntity.getChassisNumber().isEmpty())){
                        errorList.add(teamNumber + " Chassisnummer centrifuge is niet ingevuld!");
                    }
                    if((bowlEntity.getWorkhours() == null)){
                        errorList.add(teamNumber + " Draaiuren centrifuge is niet ingevuld!");
                    }
                    if((bowlEntity.getBBowlRemoved())){
                        if((bowlEntity.getBowlRemovedNumber() == null) || (bowlEntity.getBowlRemovedNumber().isEmpty())){
                            errorList.add(teamNumber + " Verwijderde Bowlnummer is niet ingevuld!");
                        }
                    }
                    if((bowlEntity.getBBowlReplaced())){
                        if((bowlEntity.getBowlReplacedNumber() == null) || (bowlEntity.getBowlReplacedNumber().isEmpty())){
                            errorList.add(teamNumber + " Teruggeplaatste Bowlnummer is niet ingevuld!");
                        }
                    }
                }
            }
        }
        if(workOrderHeader.getDiscription() == null){
            errorList.add(teamNumber + " :  Omschrijving is niet ingevuld!");
        }

        if(workLocation.equals(WorkLocation.ON_THE_MOVE)){
            int amountHoursNotCorrect = 0;
            if(workOrderHeader.getWorkOrderTimeList() != null){
                for(WorkOrderTime workOrderTime : workOrderHeader.getWorkOrderTimeList()){
                    if((workOrderTime.getTimeUp() == null)
                            || (workOrderTime.getTimeStart() == null)
                            || (workOrderTime.getTimeStop() == null)
                            || (workOrderTime.getTimeDown() == null)){
                        amountHoursNotCorrect++;
                    }
                }
                if(amountHoursNotCorrect > 0){
                    errorList.add(teamNumber + " : " + amountHoursNotCorrect + " Werkuren zijn niet ingevuld!");
                }
            }
            else{
                errorList.add(teamNumber + " : " +" Werkuren zijn niet ingevuld!");
            }
        }
        else{
            int amountHoursNotCorrect = 0;
            if(workOrderHeader.getWorkOrderTimeList() != null){
                for(WorkOrderTime workOrderTime : workOrderHeader.getWorkOrderTimeList()){
                    if((workOrderTime.getTimeStart() == null)
                            || (workOrderTime.getTimeStop() == null)){
                        amountHoursNotCorrect++;
                    }
                }
                if(amountHoursNotCorrect > 0){
                    errorList.add(teamNumber + " : " + amountHoursNotCorrect + " Werkuren zijn niet ingevuld!");
                }
            }
            else{
                errorList.add(teamNumber + " : " +" Werkuren zijn niet ingevuld!");
            }
        }

        if(workLocation.equals(WorkLocation.ON_THE_MOVE)){
            if(workOrderHeader.getFleet() == null){
                errorList.add(teamNumber + " :  Voertuig is niet ingevuld!");
            }
            if((workOrderHeader.getFleet() != null) && (workOrderHeader.getFleet().equals(Fleet.TRUCK_CRANE))){
                if(workOrderHeader.getFleetWorkType() == null){
                    errorList.add(teamNumber + " :  Type werk kraan is niet ingevuld!");
                }
                if((workOrderHeader.getFleetWorkType() != null) && (workOrderHeader.getFleetWorkType().equals(FleetWorkType.INTENS))){
                    if(workOrderHeader.getFleetHours() == null){
                        errorList.add(teamNumber + " :  Uren kraan is niet ingevuld!");
                    }
                }
            }
        }
    }
}
