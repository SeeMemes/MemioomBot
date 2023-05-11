package memioombot.backend.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<CarEntity> getCarEntity (@PathVariable Long id) {
        CarEntity resultCar = carRepository.findById(id).join().get();
        return ResponseEntity.ok(resultCar);
    }

    @PostMapping("/addCar")
    public ResponseEntity<String> createCar (@RequestBody CarEntity carEntity) {
        Long carId = carEntity.getCar_id();
        if (!carRepository.findById(carId).join().isPresent()) {
            carRepository.save(carEntity);
            return ResponseEntity.ok("Car has been added successfully");
        }
        else return ResponseEntity.badRequest().body("Car exists");
    }

    @DeleteMapping("/deleteCar/{id}")
    public ResponseEntity<String> deleteCar (@PathVariable Long id) {
        carRepository.deleteById(id);
        return ResponseEntity.ok("Car deleted successfully");
    }
}
