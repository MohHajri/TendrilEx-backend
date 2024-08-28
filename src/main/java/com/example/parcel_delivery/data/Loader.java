package com.example.parcel_delivery.data;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.List;
import java.util.Arrays;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.parcel_delivery.models.entities.Cabinet;
import com.example.parcel_delivery.models.entities.Customer;
import com.example.parcel_delivery.models.entities.Driver;
import com.example.parcel_delivery.models.entities.ParcelLocker;
import com.example.parcel_delivery.models.entities.Role;
import com.example.parcel_delivery.models.entities.Storage;
import com.example.parcel_delivery.models.entities.User;
import com.example.parcel_delivery.models.enums.CabinetStatus;
import com.example.parcel_delivery.models.enums.DriverType;
import com.example.parcel_delivery.repositories.CabinetRepo;
import com.example.parcel_delivery.repositories.CustomerRepo;
import com.example.parcel_delivery.repositories.DriverRepo;
import com.example.parcel_delivery.repositories.ParcelLockerRepo;
import com.example.parcel_delivery.repositories.RoleRepo;
import com.example.parcel_delivery.repositories.StorageRepo;
import com.example.parcel_delivery.repositories.UserRepo;

@Component
@Order(1)  // Lower values have higher priority
public class Loader implements CommandLineRunner {

    @Autowired
    private ParcelLockerRepo parcelLockerRepo;

    @Autowired
    private CabinetRepo cabinetRepo;

    @Autowired
    private CustomerRepo customerRepository; 

    @Autowired
    private UserRepo userRepository;
    
    @Autowired
    private DriverRepo driverRepository;

    @Autowired
    private StorageRepo storageRepo;

    @Autowired
    private RoleRepo roleRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;


    private static final double RADIUS = 35000; // 35 km in meters
    private Random random = new Random();

    @Override
    public void run(String... args) throws Exception {
        System.out.println("beginging of loader");

        generateStorages();
        generateParcelLockers();
        generateRecipients(10);
        generateDriversInCities();
    }

    private void generateStorages() {
        List<String> cities = Arrays.asList("Helsinki", "Espoo", "Tampere", "Vantaa", "Oulu");
        cities.forEach(city -> {
            if (storageRepo.findByCity(city).isEmpty()) {
                Storage storage = new Storage();
                storage.setName(city + " Storage");
                storage.setAddress("Storage Street, " + city);
                storage.setCity(city);
                storageRepo.save(storage);
            }
        });
    }


    private void generateParcelLockers() {
        GeometryFactory geometryFactory = new GeometryFactory();
        Random random = new Random();
    
        generateLockersInCity("Oulu", 65.01236, 25.46816, geometryFactory, random, 30);
    
        generateLockersInCity("Helsinki", 60.192059, 24.945831, geometryFactory, random, 30);
    }
    
    private void generateLockersInCity(String city, double latitude, double longitude, GeometryFactory geometryFactory, Random random, int numberOfLockers) {
        for (int i = 1; i <= numberOfLockers; i++) {
            double[] randomPoint = generateRandomPoint(latitude, longitude, RADIUS, random);
            Point location = geometryFactory.createPoint(new Coordinate(randomPoint[1], randomPoint[0]));
            location.setSRID(4326);
    
            ParcelLocker locker = new ParcelLocker();
            locker.setName(city + " Locker " + i);
            locker.setLockerPoint(location);
    
            Set<Cabinet> cabinets = new HashSet<>();
            for (int j = 1; j <= 10; j++) {
                Cabinet cabinet = new Cabinet();
                cabinet.setStatus(CabinetStatus.FREE);
                cabinet.setWidth(50.0);
                cabinet.setHeight(50.0);
                cabinet.setDepth(50.0);
                cabinet.setLockerLocation(locker);
                cabinets.add(cabinet);
            }
            locker.setCabinets(cabinets);
    
            parcelLockerRepo.save(locker);
            cabinetRepo.saveAll(cabinets);
        }
    }    
    


    private double[] generateRandomPoint(double latitude, double longitude, double radius, Random random) {
        // Convert radius from meters to degrees
        double radiusInDegrees = radius / 111000f;

        double u = random.nextDouble();
        double v = random.nextDouble();
        double w = radiusInDegrees * Math.sqrt(u);
        double t = 2 * Math.PI * v;
        double x = w * Math.cos(t);
        double y = w * Math.sin(t);

        // Adjust the x-coordinate for the shrinking of the east-west distances
        double new_x = x / Math.cos(Math.toRadians(latitude));

        double foundLongitude = new_x + longitude;
        double foundLatitude = y + latitude;

        return new double[]{foundLatitude, foundLongitude};
    }

    private void generateRecipients(int numberOfRecipientsPerCity) {
        generateRecipientsInCity("Helsinki", numberOfRecipientsPerCity);
        generateRecipientsInCity("Oulu", numberOfRecipientsPerCity);
    }
    
    private void generateRecipientsInCity(String city, int numberOfRecipients) {
        for (int i = 0; i < numberOfRecipients; i++) {
            User user = createUser("recipient" + city + i, "Some Street " + i, city);
            Customer customer = new Customer();
            customer.setUser(user);
            customerRepository.save(customer);
        }
    }    

    private void generateDriversInCities() {
        // Ensure 5 INTRA_CITY and 5 INTER_CITY drivers in Helsinki
        createSpecificDriversInCity("Helsinki", 5, DriverType.INTRA_CITY);
        createSpecificDriversInCity("Helsinki", 5, DriverType.INTER_CITY);

        // Ensure 5 INTRA_CITY and 5 INTER_CITY drivers in Oulu
        createSpecificDriversInCity("Oulu", 5, DriverType.INTRA_CITY);
        createSpecificDriversInCity("Oulu", 5, DriverType.INTER_CITY);
    }

    private void createSpecificDriversInCity(String city, int count, DriverType driverType) {
        for (int i = 0; i < count; i++) {
            createDriverUser(city, driverType);
        }
    }

    private void createDriverUser(String city, DriverType driverType) {
        User driverUser = createUser("driver" + city + random.nextInt(1000), "Driver Street " + random.nextInt(100), city);
        Driver driver = new Driver();
        driver.setUser(driverUser);
        driver.setDriverType(driverType);
        driver.setIsAvailable(true);
        driverRepository.save(driver);
    }

    private User createUser(String username, String address, String city) {
        String phoneNumber;
        do {
            phoneNumber = "050" + (1000000 + random.nextInt(9000000));
        } while (userRepository.existsByPhoneNumber(phoneNumber));
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("password"));
        user.setFirstName(username + "FirstName");
        user.setLastName(username + "LastName");
        user.setEmail(username + "@example.com");
        user.setPhoneNumber(phoneNumber);
        user.setAddress(address);
        user.setCity(city);
        user.setPostcode("00100");
    
        // Assign ROLE_USER to this user
        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new IllegalStateException("ROLE_USER not found"));
        user.setRoles(Set.of(userRole));
    
        return userRepository.save(user);
    }
    
}