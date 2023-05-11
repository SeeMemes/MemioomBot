package memioombot.backend.example;

import memioombot.backend.example.CarEntity;
import memioombot.backend.database.ydbdriver.repository.YdbRepository;
import org.springframework.stereotype.Component;

@Component
public class CarRepository extends YdbRepository<CarEntity, Long> {
}
