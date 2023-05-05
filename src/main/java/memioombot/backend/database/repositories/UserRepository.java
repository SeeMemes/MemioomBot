package memioombot.backend.database.repositories;

import memioombot.backend.database.entities.UserEntity;
import memioombot.backend.database.ydbdriver.repository.YdbRepository;
import org.springframework.stereotype.Component;

@Component
public class UserRepository extends YdbRepository<UserEntity, Long> {
}
