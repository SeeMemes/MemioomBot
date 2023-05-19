package memioombot.backend.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/cars")
public class CarController {

    private final CarRepository carRepository;

    @Autowired
    public CarController(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    @GetMapping("/getCar/{id}")
    public ResponseEntity<CarEntity> getCarEntity (@PathVariable Long id) {
        return carRepository.findById(id).thenApplyAsync((carEntity) -> {
            if (carEntity.isPresent()) return ResponseEntity.ok(carEntity.get());
            else return null;
        }).join();
    }

    @GetMapping("/getCars")
    public ResponseEntity<Iterable<CarEntity>> getCarsEntity () {
        return carRepository.findAll().thenApplyAsync((carsEntity) -> {
            return ResponseEntity.ok(carsEntity);
        }).join();
    }

    @PostMapping("/addCar")
    public ResponseEntity<String> createCar (@RequestBody CarEntity carEntity) {
        return carRepository.save(carEntity).thenApplyAsync((car) -> {
            return ResponseEntity.ok(car.getNumber() + "Car has been added successfully");
        }).join();
    }

    @GetMapping("/deleteCar/{id}")
    public ResponseEntity<String> deleteCar (@PathVariable Long id) {
        return CompletableFuture.supplyAsync(() -> {
            carRepository.deleteById(id);
            return (ResponseEntity.ok("Car deleted successfully"));
        }).join();
    }

    @GetMapping("/getCar/{id1}/{id2}")
    public ResponseEntity<List> getCarEntities (@PathVariable Long id1, @PathVariable Long id2) {
        return carRepository.findAllById(new ArrayList<Long>(){{
                add(id1);
                add(id2);
            }}).thenApplyAsync((carEntities) -> {
            List carList = new ArrayList<>();
            for (CarEntity carEntity: carEntities) {
                carList.add(carEntities);
            }
            return ResponseEntity.ok(carList);
        }).join();
    }
}
