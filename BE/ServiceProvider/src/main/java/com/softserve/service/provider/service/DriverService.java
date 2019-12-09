package com.softserve.service.provider.service;

import com.softserve.service.provider.dto.*;
import com.softserve.service.provider.model.Driver;
import com.softserve.service.provider.model.History;
import com.softserve.service.provider.repository.DriverRepository;
import com.softserve.service.provider.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;
    private final HistoryRepository historyRepository;
    private final JdbcTemplate jdbcTemplate;

    public void add(DriverDTO driverDTO) {
        Driver driver = Driver.builder()
                .city(driverDTO.getCity())
                .lastName(driverDTO.getLastName())
                .firstName(driverDTO.getFirstName())
                .email(driverDTO.getEmail())
                .phone(driverDTO.getPhone())
                .password(driverDTO.getPassword())
                .licence(driverDTO.getLicence())
                .fine(driverDTO.getFine())
                .rating(driverDTO.getRating())
                .build();
        driverRepository.save(driver);
    }

    public Iterable<DriverDTO> getAll() {
        Iterable<Driver> drivers = driverRepository.findAll();
        List<DriverDTO> dtos = new ArrayList<>();

        for (Driver driver : drivers) {
            dtos.add(getDriverDTO(driver));
        }
        return dtos;
    }

    public Double getRating(UUID id) {
        return jdbcTemplate.queryForObject(
                "SELECT AVG(rating) from histories where driver_id = ?", Double.class, id);
    }

    public void updateFine(UUID id, DriverFineDTO driverFineDTO) {
        driverRepository.findById(id)
                .map(driver -> {
                    driver.setFine(driver.getFine() + driverFineDTO.getFine());
                    driverRepository.save(driver);

                    return getDriverDTO(driver);
                })
                .orElseThrow(RuntimeException::new);
    }

    public void updateDriver(UUID id, DriverDTO driverDTO) {
        driverRepository.findById(id)
                .map(driver -> {
                    driver.setPhone(driverDTO.getPhone());
                    driver.setLicence(driverDTO.getLicence());
                    driver.setFirstName(driverDTO.getFirstName());
                    driver.setLastName(driverDTO.getLastName());
                    driver.setPassword(driverDTO.getPassword());
                    driver.setEmail(driverDTO.getEmail());
                    driver.setCity(driverDTO.getCity());
                    driverRepository.save(driver);
                    return getDriverDTO(driver);
                })
                .orElseThrow(RuntimeException::new);
    }

    public void updateRating(UUID id) {
        driverRepository.findById(id)
                .map(driver -> {
                    driver.setRating(getRating(id));
                    driverRepository.save(driver);

                    return getDriverDTO(driver);
                })
                .orElseThrow(RuntimeException::new);

    }

    public DriverProfileDTO getProfile(UUID id) {

        List<History> histories = historyRepository.findAllByDriverId(id);
        List<HistoryDTO> historyDTOList = new ArrayList<>();

        for (History history : histories) {
            HistoryDTO historyDTO = HistoryDTO.builder()
                    .date(history.getDate())
                    .description(history.getDescription())
                    .distance(history.getDistance())
                    .finishPoint(history.getFinishPoint())
                    .startPoint(history.getStartPoint())
                    .price(history.getPrice())
                    .fine(history.getFine())
                    .rating(history.getRating())
                    .travelTime(history.getTravelTime())
                    .build();

            historyDTOList.add(historyDTO);
        }

        return driverRepository.findById(id)
                .map(driver -> {
                    CarDTO carDTO = CarDTO.builder()
                            .babyCarSeat(driver.getCar().isBabyCarSeat())
                            .brand(driver.getCar().getBrand())
                            .licencePlate(driver.getCar().getLicencePlate())
                            .colour(driver.getCar().getColour())
                            .type(driver.getCar().getType())
                            .conditioner(driver.getCar().isConditioner())
                            .courier(driver.getCar().isCourier())
                            .english(driver.getCar().isEnglish())
                            .model(driver.getCar().getModel())
                            .nonSmoker(driver.getCar().isNonSmoker())
                            .numberOfSeats(driver.getCar().getNumberOfSeats())
                            .pet(driver.getCar().isPet())
                            .silence(driver.getCar().isSilence())
                            .yearOfProduction(driver.getCar().getYearOfProduction())
                            .build();


                    return DriverProfileDTO.builder()
                            .histories(historyDTOList)
                            .car(carDTO)
                            .email(driver.getEmail())
                            .firstName(driver.getFirstName())
                            .lastName(driver.getLastName())
                            .phone(driver.getPhone())
                            .city(driver.getCity())
                            .licence(driver.getLicence())
                            .fine(driver.getFine())
                            .rating(driver.getRating())
                            .build();
                })
                .orElseThrow(RuntimeException::new);
    }

    public List<UUID> findId(SearchCarDTO searchCarDTO) {
        String sql = String.format("SELECT distinct vehicle_id FROM CARS where" +
                        " car_type = '%s' and" +
                        " baby_car_seat = '%s' and" +
                        " conditioner = '%s' and" +
                        " courier = '%s' and" +
                        " english = '%s' and" +
                        " non_smoker = '%s' and" +
                        " pet = '%s' and" +
                        " silence = '%s'",
                searchCarDTO.getType(),
                searchCarDTO.isBabyCarSeat(),
                searchCarDTO.isConditioner(),
                searchCarDTO.isCourier(),
                searchCarDTO.isEnglish(),
                searchCarDTO.isNonSmoker(),
                searchCarDTO.isPet(),
                searchCarDTO.isSilence());

        List<UUID> carsId = jdbcTemplate.queryForList(sql, UUID.class);
        List<UUID> driversId = new ArrayList<>();

        System.out.println(carsId);

        for (UUID uuid : carsId) {
            driversId.add(jdbcTemplate.queryForObject(
                    "select driver_id from drivers where vehicle_id = ?", UUID.class, uuid));
            System.out.println(driversId);
        }
        return driversId;
    }

    private DriverDTO getDriverDTO(Driver driver) {
        return DriverDTO.builder()
                .city(driver.getCity())
                .firstName(driver.getFirstName())
                .lastName(driver.getLastName())
                .licence(driver.getLicence())
                .email(driver.getEmail())
                .password(driver.getPassword())
                .phone(driver.getPhone())
                .fine(driver.getFine())
                .rating(driver.getRating())
                .build();
    }
}
