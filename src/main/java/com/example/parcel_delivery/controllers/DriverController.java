package com.example.parcel_delivery.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.parcel_delivery.models.dtos.responses.DriverResDTO;
import com.example.parcel_delivery.models.entities.Driver;
import com.example.parcel_delivery.models.mappers.DriverMapper;
import com.example.parcel_delivery.services.DriverService;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/drivers")
@AllArgsConstructor
public class DriverController {

        @Autowired
        private DriverService driverService;

        @Autowired
        private DriverMapper driverMapper;

        @GetMapping("/id/{id}")
        public ResponseEntity<DriverResDTO> getDriverById(@PathVariable Long id) {
                return ResponseEntity
                                .ok(driverMapper
                                                .toDriverResDTO(driverService
                                                                .getDriverById(id)));
        }

        @GetMapping("/authenticated")
        public ResponseEntity<DriverResDTO> getDriverByAuthenticatedUser() {
                return ResponseEntity
                                .ok(driverMapper
                                                .toDriverResDTO(driverService
                                                                .getAuthenticatedDriver()));
        }

        @PutMapping("/courier/authenticated/status/{status}")
        public ResponseEntity<DriverResDTO> updateDriverStatus(@PathVariable Long id) {
                return ResponseEntity
                                .ok(driverMapper
                                                .toDriverResDTO(driverService
                                                                .markDriverAsUnavailable(id)));
        }

        @GetMapping("/intra-city")
        public ResponseEntity<List<Driver>> getActiveAvailableIntraCityDrivers(@RequestParam String city) {
                return ResponseEntity
                                .ok(driverService.getActiveAvailableIntraCityDrivers(city));
        }

        @GetMapping("/inter-city")
        public ResponseEntity<List<Driver>> getActiveAvailableInterCityDrivers(@RequestParam String city) {
                return ResponseEntity
                                .ok(driverService.getActiveAvailableInterCityDrivers(city));
        }
}
