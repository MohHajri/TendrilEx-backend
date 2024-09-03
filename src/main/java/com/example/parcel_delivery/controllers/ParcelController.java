package com.example.parcel_delivery.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.parcel_delivery.models.dtos.responses.ParcelResDTO;
import com.example.parcel_delivery.models.entities.Parcel;
import com.example.parcel_delivery.models.mappers.ParcelMapper;
import com.example.parcel_delivery.services.ParcelService;
import com.example.parcel_delivery.models.dtos.requests.ParcelReqDTO;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/parcels")
@AllArgsConstructor
public class ParcelController {

        @Autowired
        private ParcelService parcelService;

        @Autowired
        private ParcelMapper parcelMapper;

        // Endpoint to send a new parcel
        @PostMapping("/send")
        public ResponseEntity<ParcelResDTO> sendNewParcel(@RequestBody ParcelReqDTO request) {
                return ResponseEntity
                                .ok(parcelMapper
                                                .toParcelResDTO(parcelService
                                                                .sendNewParcel(request)));
        }

        // Endpoint to get a parcel by its ID
        @GetMapping("/{id}")
        public ResponseEntity<ParcelResDTO> getParcelById(@PathVariable Long id) {
                return ResponseEntity
                                .ok(parcelMapper
                                                .toParcelResDTO(parcelService
                                                                .getParcelById(id)));
        }

        // Endpoint to get a parcel by its ID and the sender's ID
        @GetMapping("id/{id}/sender/{senderId}")
        public ResponseEntity<ParcelResDTO> getParcelByParcelIdAndSenderId(@PathVariable Long id,
                        @PathVariable Long senderId) {
                return ResponseEntity
                                .ok(parcelMapper
                                                .toParcelResDTO(parcelService
                                                                .getByParcelIdAndSenderId(id, senderId)));
        }

        // Endpoint to get a parcel by its ID and the recipient's ID
        @GetMapping("id/{id}/recipient/{recipientId}")
        public ResponseEntity<ParcelResDTO> getParcelByParcelIdAndRecipientId(@PathVariable Long id,
                        @PathVariable Long recipientId) {
                return ResponseEntity
                                .ok(parcelMapper
                                                .toParcelResDTO(parcelService
                                                                .getByParcelIdAndRecipientId(id, recipientId)));
        }

        // Endpoint to get a parcel by its ID and the driver's ID
        @GetMapping("id/{id}/driver/{driverId}")
        public ResponseEntity<ParcelResDTO> getParcelByParcelIdAndDriverId(@PathVariable Long id,
                        @PathVariable Long recipientId) {
                return ResponseEntity
                                .ok(parcelMapper
                                                .toParcelResDTO(parcelService
                                                                .getByParcelIdAndDriverId(id, recipientId)));
        }

        // Endpoint to get all parcels sent by a specific customer
        @GetMapping("/sender/{id}/sent")
        public ResponseEntity<List<ParcelResDTO>> getParcelsBySenderId(@PathVariable Long id) {
                List<Parcel> parcels = parcelService.getParcelsBySenderId(id);
                List<ParcelResDTO> dtoList = parcels.stream()
                                .map(parcelMapper::toParcelResDTO)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(dtoList);
        }

        // Endpoint to get all parcels received by a specific customer
        @GetMapping("/recipient/{id}/received")
        public ResponseEntity<List<ParcelResDTO>> getParcelsByRecipientId(@PathVariable Long id) {
                List<Parcel> parcels = parcelService.getParcelsByRecipientId(id);
                List<ParcelResDTO> dtoList = parcels.stream()
                                .map(parcelMapper::toParcelResDTO)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(dtoList);
        }

        // Endpoint to get all parcels assigned to a specific driver
        @GetMapping("/driver/{id}/assigned")
        public ResponseEntity<List<ParcelResDTO>> getParcelsByDriverId(@PathVariable Long id) {
                List<Parcel> parcels = parcelService.getParcelsAssignedToDriver(id);
                List<ParcelResDTO> dtoList = parcels.stream()
                                .map(parcelMapper::toParcelResDTO)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(dtoList);
        }

        // Endpoint for a driver to pick up a parcel from a locker
        @PostMapping("/pickup/{parcelId}/{transactionCode}")
        public ResponseEntity<ParcelResDTO> pickUpParcelFromLocker(@PathVariable Long parcelId,
                        @PathVariable Integer transactionCode) {
                return ResponseEntity.ok(
                                parcelMapper
                                                .toParcelResDTO(parcelService
                                                                .pickUpParcelFromLocker(parcelId, transactionCode)));
        }

        // Endpoint for a customer (sender) to drop off a parcel in the cabinet
        // Endpoint to drop off a parcel in a cabinet
        @PostMapping("/customer/drop-off/id/{parcelId}/transactionCode/{transactionCode}")
        public ResponseEntity<ParcelResDTO> dropOffParcelInCabinet(@PathVariable Long parcelId,
                        @PathVariable Integer transactionCode) {
                return ResponseEntity
                                .ok(parcelMapper
                                                .toParcelResDTO(parcelService
                                                                .dropOffParcelInCabinet(parcelId, transactionCode)));
        }

        // Endpoint to deliver a parcel to the departure storage (for inter-city
        // delivery)
        @PostMapping("/deliver/departure-storage/{parcelId}")
        public ResponseEntity<ParcelResDTO> deliverToDepartureStorage(@PathVariable Long parcelId) {
                return ResponseEntity
                                .ok(parcelMapper
                                                .toParcelResDTO(parcelService
                                                                .deliverToDepartureStorage(parcelId)));
        }

        // Endpoint to deliver a parcel to the destination storage (after inter-city
        // delivery)
        @PostMapping("/deliver/destination-storage/{parcelId}")
        public ResponseEntity<ParcelResDTO> deliverToDestinationStorage(@PathVariable Long parcelId) {
                return ResponseEntity.ok(
                                parcelMapper
                                                .toParcelResDTO(parcelService
                                                                .deliverToDestinationStorage(
                                                                                parcelId)));
        }

        // Endpoint to deliver a parcel directly to the recipient
        @PostMapping("/deliver/recipient/{parcelId}")
        public ResponseEntity<ParcelResDTO> deliverToRecipient(@PathVariable Long parcelId) {
                return ResponseEntity
                                .ok(parcelMapper
                                                .toParcelResDTO(parcelService
                                                                .deliverToRecipient(parcelId)));
        }

        // Endpoint to get parcels assigned to an intra-city driver
        @GetMapping("/driver/intra-city/{driverId}/parcels")
        public ResponseEntity<List<ParcelResDTO>> getParcelsAssignedToIntraCityDriver(@PathVariable Long driverId) {
                List<Parcel> parcels = parcelService.getParcelsAssignedToIntraCityDriver(driverId);
                List<ParcelResDTO> dtoList = parcels.stream()
                                .map(parcelMapper::toParcelResDTO)
                                .collect(Collectors.toList());
                return ResponseEntity.ok(dtoList);
        }

        // Endpoint to get parcels assigned to an inter-city driver
        @GetMapping("/driver/inter-city/{driverId}/parcels")
        public ResponseEntity<List<ParcelResDTO>> getParcelsAssignedToInterCityDriver(@PathVariable Long driverId) {
                List<Parcel> parcels = parcelService.getParcelsAssignedToInterCityDriver(driverId);
                List<ParcelResDTO> dtoList = parcels.stream()
                                .map(parcelMapper::toParcelResDTO)
                                .collect(Collectors.toList());
                return ResponseEntity.ok(dtoList);
        }

        // Endpoint to retrieves all parcels that are currently in a specific storage.
        @GetMapping("/storage/{storageId}")
        public ResponseEntity<List<ParcelResDTO>> getAllParcelsInStorage(@PathVariable Long storageId) {
                List<Parcel> parcels = parcelService.getAllParcelsInStorage(storageId);
                List<ParcelResDTO> dtoList = parcels.stream()
                                .map(parcelMapper::toParcelResDTO)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(dtoList);
        }

        // Endpoint to etrieves intra-city parcels that are currently in a specific
        // storage.
        @GetMapping("/storage/{storageId}/intra-city")
        public ResponseEntity<List<ParcelResDTO>> getIntraCityParcelsInStorage(@PathVariable Long storageId) {
                List<Parcel> parcels = parcelService.getIntraCityParcelsInStorage(storageId);
                List<ParcelResDTO> dtoList = parcels.stream()
                                .map(parcelMapper::toParcelResDTO)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(dtoList);
        }

        // Endoint to retrieves inter-city parcels that are currently in a specific
        // storage
        @GetMapping("/storage/{storageId}/inter-city")
        public ResponseEntity<List<ParcelResDTO>> getInterCityParcelsInStorage(@PathVariable Long storageId) {
                List<Parcel> parcels = parcelService.getInterCityParcelsInStorage(storageId);
                List<ParcelResDTO> dtoList = parcels.stream()
                                .map(parcelMapper::toParcelResDTO)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(dtoList);
        }

}
