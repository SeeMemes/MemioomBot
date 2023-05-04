package memioombot.backend.database.ydbdriver.util.exceptions;

public class CreateTableException extends RuntimeException{

    public CreateTableException(String tableName, Throwable cause) {
        super("Cannot create database " + tableName, cause);
    }

    public CreateTableException(String tableName) {
        super("Cannot create database " + tableName);
    }
}
