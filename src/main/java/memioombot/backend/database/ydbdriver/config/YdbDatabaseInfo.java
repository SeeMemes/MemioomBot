package memioombot.backend.database.ydbdriver.config;

import java.lang.reflect.Field;
import java.util.Map;

public class YdbDatabaseInfo {
    private Map<String, String> database;
    private Map<String, Field[]> fields;
    private Field primaryKey;

    public YdbDatabaseInfo(Map<String, String> database, Map<String, Field[]> fields, Field primaryKey) {
        this.database = database;
        this.fields = fields;
        this.primaryKey = primaryKey;
    }

    public String getDatabase(String entityName) {
        return database.get(entityName);
    }

    public Field[] getFields(String database) {
        return fields.get(database);
    }

    public Field getPrimaryKey() {
        return primaryKey;
    }
}
