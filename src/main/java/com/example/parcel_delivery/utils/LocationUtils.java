package com.example.parcel_delivery.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.example.parcel_delivery.exceptions.TendrilExExceptionHandler;
import com.example.parcel_delivery.models.dtos.requests.CustomerLocationReqDTO;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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

    public Point geocodeLocation(CustomerLocationReqDTO lockerReqDTO) {
        try {

            String addressStr = lockerReqDTO.getCustomerAddress() + ", " +
                    lockerReqDTO.getCustomerPostcode() + " " +
                    lockerReqDTO.getCustomerCity() + ", Finland";
            String encodedAddress = URLEncoder.encode(addressStr, "UTF-8");

            String requestUri = GEOCODING_RESOURCE + "?key=" + API_KEY + "&address=" + encodedAddress;

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(requestUri))
                    .timeout(Duration.ofMillis(2000))
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String response = httpResponse.body();
            System.out.println("Geocoding API Response: " + response);

            if (httpResponse.statusCode() != 200) {
                throw new TendrilExExceptionHandler(HttpStatus.BAD_GATEWAY,
                        "Geocoding API request failed with status code: " + httpResponse.statusCode());
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode responseJson = mapper.readTree(response);
            JsonNode results = responseJson.get("results");

            if (results != null && results.isArray() && results.size() > 0) {
                // Check for partial match
                if (results.get(0).has("partial_match") && results.get(0).get("partial_match").asBoolean()) {
                    throw new TendrilExExceptionHandler(HttpStatus.BAD_REQUEST,
                            "Geocoding API returned a partial match for the address: " + addressStr);
                }
                JsonNode location = results.get(0).get("geometry").get("location");
                double latitude = location.get("lat").asDouble();
                double longitude = location.get("lng").asDouble();
                Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
                point.setSRID(4326);
                return point;
            } else {
                throw new TendrilExExceptionHandler(HttpStatus.NOT_FOUND,
                        "Geocoding API returned no results for the address: " + addressStr);
            }
        } catch (InterruptedException | IOException e) {
            throw new TendrilExExceptionHandler(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error while geocoding: " + e.getMessage());
        }
    }
}
