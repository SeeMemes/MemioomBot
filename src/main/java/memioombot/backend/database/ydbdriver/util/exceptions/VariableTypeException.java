package memioombot.backend.database.ydbdriver.util.exceptions;

public class VariableTypeException extends RuntimeException{
    private String variableType;

    public VariableTypeException(String variableType, Throwable cause) {
        super("Type " + variableType + " not present", cause);
        this.variableType = variableType;
    }

    public VariableTypeException(String variableType) {
        super("Cannot translate " + variableType + " to PrimitiveType");
        this.variableType = variableType;
    }
}
