package com.adverto.dejonghe.application.Controllers;

import com.adverto.dejonghe.application.entities.GoogleMapsEntities.LatLng;
import com.adverto.dejonghe.application.entities.GoogleMapsEntities.Results;
import com.adverto.dejonghe.application.entities.customers.Address;
import com.adverto.dejonghe.application.entities.customers.Coordinates;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Controller
public class GoogleRestController {
    RestTemplate restTemplate = new RestTemplate();

    public Optional<Coordinates> getOptCoordinatesforAdres(Address addressToEvaluate){
        LatLng latLng = restTemplate.getForObject("https://maps.googleapis.com/maps/api/geocode/json?address=" + addressToEvaluate.getStreet() + addressToEvaluate.getZip() + addressToEvaluate.getCity() + "&key=AIzaSyDPXSH4nbG9IiEHq-KF1FJrLmnS2iCOAFA", LatLng.class);
        Results[] results = latLng.getResults();
        if(results.length != 0) {
            Coordinates newCoordinates = new Coordinates();
            newCoordinates.setLatitude(results[0].getGeometry().getLocation().getLat());
            newCoordinates.setLongitude(results[0].getGeometry().getLocation().getLng());
            return Optional.of(newCoordinates);
        }
        else{
            return Optional.empty();
        }

    }

    public Optional<Double> getOptDistanceforAdres(Address addressToEvaluate) throws JSONException {
        String json = restTemplate.getForObject( "https://maps.googleapis.com/maps/api/distancematrix/json?origins=De Linde 8840 Staden&destinations="+addressToEvaluate.getStreet()+" "+addressToEvaluate.getCity()+"&key=AIzaSyDPXSH4nbG9IiEHq-KF1FJrLmnS2iCOAFA", String.class );
        JSONObject object = new JSONObject( json );
        try {
            String[] parts = object.getJSONArray( "rows" ).getJSONObject( 0 ).getJSONArray( "elements" ).getJSONObject( 0 ).getJSONObject( "distance" ).get( "text" ).toString().split( Pattern.quote("."));
            return Optional.of(Double.valueOf( parts[0].replace( "km","" )));
            // mobiliteitEntity.setVerplaatsingLaatsteLocatie(30);
        }
        catch (Exception h){
            return Optional.empty();
        }
    }
}
