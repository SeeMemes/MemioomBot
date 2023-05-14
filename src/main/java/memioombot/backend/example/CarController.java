package memioombot.backend.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

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
    @Async
    public ResponseEntity<CarEntity> getCarEntity (@PathVariable Long id) {
        return CompletableFuture.supplyAsync(() -> {
            CarEntity resultCar = carRepository.findById(id).join().get();
            return ResponseEntity.ok(resultCar);
        }).join();

    }

    @GetMapping("/getCars")
    @Async
    public ResponseEntity<Iterable<CarEntity>> getCarsEntity () {
        return CompletableFuture.supplyAsync(() -> {
            Iterable<CarEntity> resultCars = carRepository.findAll().join();
            return ResponseEntity.ok(resultCars);
        }).join();
    }

    @PostMapping("/addCar")
    @Async
    public ResponseEntity<String> createCar (@RequestBody CarEntity carEntity) {
        return CompletableFuture.supplyAsync(() -> {
            carRepository.save(carEntity);
            return ResponseEntity.ok("Car has been added successfully");
        }).join();
    }

    @GetMapping("/deleteCar/{id}")
    @Async
    public ResponseEntity<String> deleteCar (@PathVariable Long id) {
        return CompletableFuture.supplyAsync(() -> {
            carRepository.deleteById(id);
            return (ResponseEntity.ok("Car deleted successfully"));
        }).join();
    }
}
