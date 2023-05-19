package memioombot.backend.database.repositories;

import memioombot.backend.database.entities.UserEntity;
import memioombot.backend.database.ydbdriver.annotations.YdbStorage;
import memioombot.backend.database.ydbdriver.repository.YdbRepository;

@YdbStorage
public class UserRepository extends YdbRepository<UserEntity, Long> {
}
