package memioombot.backend.database.repositories;

import memioombot.backend.database.entities.CarEntity;
import memioombot.backend.database.ydbdriver.repository.YdbRepository;
import org.springframework.stereotype.Component;

@Component
public class CarRepository extends YdbRepository<CarEntity, String> {
}
