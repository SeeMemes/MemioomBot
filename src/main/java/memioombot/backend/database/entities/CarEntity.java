package memioombot.backend.database.entities;

import memioombot.backend.database.ydbdriver.annotations.YdbEntity;
import memioombot.backend.database.ydbdriver.annotations.YdbPrimaryKey;

@YdbEntity
public class CarEntity {
    @YdbPrimaryKey
    String name;
    String color;
}

