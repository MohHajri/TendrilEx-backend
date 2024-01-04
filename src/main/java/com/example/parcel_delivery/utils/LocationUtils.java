package com.example.parcel_delivery.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.example.parcel_delivery.models.dtos.requests.CustomerLocationReqDTO;
import com.example.parcel_delivery.models.dtos.requests.ParcelReqDTO;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.locationtech.jts.geom.Coordinate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class LocationUtils {

    @Value("${GEOCODING_RESOURCE}")
    private String GEOCODING_RESOURCE;

    @Value("${API_KEY}")
    private String API_KEY;

    private static final GeometryFactory geometryFactory = new GeometryFactory();

    public Point getLocationFromDTO(ParcelReqDTO parcelReqDTO) {
        double latitude = Double.parseDouble(parcelReqDTO.getDropOffLatitude());
        double longitude = Double.parseDouble(parcelReqDTO.getDropOffLongitude());
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }

    public Point geocodeLocation(CustomerLocationReqDTO lockerReqDTO) {
        try {
            String addressStr = lockerReqDTO.getSenderAddress() + ", " +
                                lockerReqDTO.getSenderPostcode() + " " +
                                lockerReqDTO.getSenderCity() + ", Finland";
            String encodedAddress = URLEncoder.encode(addressStr, "UTF-8");
            String requestUri = GEOCODING_RESOURCE + "?key=" + API_KEY + "&address=" + encodedAddress;

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(requestUri))
                    .timeout(Duration.ofMillis(2000))
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String response = httpResponse.body();

            if (httpResponse.statusCode() != 200) {
                throw new RuntimeException("Geocoding API request failed with status code: " + httpResponse.statusCode());
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode responseJson = mapper.readTree(response);
            JsonNode results = responseJson.get("results");

            if (results != null && results.isArray() && results.size() > 0) {
                JsonNode location = results.get(0).get("geometry").get("location");
                double latitude = location.get("lat").asDouble();
                double longitude = location.get("lng").asDouble();
                Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
                point.setSRID(4326); 
                return point;
            } else {
                throw new RuntimeException("Geocoding API returned no results.");
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException("Error while geocoding: " + e.getMessage());
        }
    }
}
