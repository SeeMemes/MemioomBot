package memioombot.backend.database.ydbdriver.config;

import java.lang.reflect.Field;
import java.util.Map;

public class YdbDatabaseInfo {
    private Map<String, String> database;
    private Map<String, Field[]> fields;
    private Map<String, Field> primaryKeys;

    public YdbDatabaseInfo(Map<String, String> database, Map<String, Field[]> fields, Map<String, Field> primaryKeys) {
        this.database = database;
        this.fields = fields;
        this.primaryKeys = primaryKeys;
    }

    public String getDatabase(String entityName) {
        return database.get(entityName);
    }

    public Field[] getFields(String database) {
        return fields.get(database);
    }

    public Field getPrimaryKey(String database) {
        return primaryKeys.get(database);
    }
}
