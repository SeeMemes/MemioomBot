package memioombot.backend.database.ydbdriver.util;

public class YdbDatabaseInfo {
    private String database;
    public YdbDatabaseInfo (String database) {
        this.database = database;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }
}
