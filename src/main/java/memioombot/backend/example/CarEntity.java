package memioombot.backend.example;

import memioombot.backend.database.ydbdriver.annotations.YdbEntity;
import memioombot.backend.database.ydbdriver.annotations.YdbPrimaryKey;

@YdbEntity(dbName = "car_entity")
public class CarEntity {
    @YdbPrimaryKey
    Long car_id;
    String number;
    String model;

    public CarEntity(String number, String model) {
        this.model = model;
        this.number = number;
    }

    public CarEntity() {

    }

    public Long getCar_id() {
        return car_id;
    }

    public String getNumber() {
        return number;
    }

    public String getModel() {
        return model;
    }
}

