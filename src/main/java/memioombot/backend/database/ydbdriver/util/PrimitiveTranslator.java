package memioombot.backend.database.ydbdriver.util;

import memioombot.backend.database.ydbdriver.util.exceptions.VariableTypeException;
import tech.ydb.table.result.ValueReader;
import tech.ydb.table.values.PrimitiveType;
import tech.ydb.table.values.PrimitiveValue;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Types that are not presented yet
 * {@link PrimitiveType#Yson}
 * {@link PrimitiveType#Json}
 * {@link PrimitiveType#Uuid}
 * {@link PrimitiveType#Date}
 * {@link PrimitiveType#Datetime}
 * {@link PrimitiveType#Timestamp}
 * {@link PrimitiveType#Interval}
 * {@link PrimitiveType#TzDate}
 * {@link PrimitiveType#TzDatetime}
 * {@link PrimitiveType#TzTimestamp}
 * {@link PrimitiveType#JsonDocument}
 * {@link PrimitiveType#DyNumber}
 */

public class PrimitiveTranslator {

    private static final Map<Class<?>, PrimitiveType> JAVA_TO_YQL_TYPES = new HashMap<>();
    private static final Map<Class<?>, String> JAVA_TO_STRING_TYPES = new HashMap<>();

    static {
        JAVA_TO_YQL_TYPES.put(Boolean.class, PrimitiveType.Bool);
        JAVA_TO_YQL_TYPES.put(Byte.class, PrimitiveType.Int8);
        JAVA_TO_YQL_TYPES.put(Short.class, PrimitiveType.Int16);
        JAVA_TO_YQL_TYPES.put(Integer.class, PrimitiveType.Int32);
        JAVA_TO_YQL_TYPES.put(Long.class, PrimitiveType.Int64);
        JAVA_TO_YQL_TYPES.put(Float.class, PrimitiveType.Float);
        JAVA_TO_YQL_TYPES.put(Double.class, PrimitiveType.Double);
        JAVA_TO_YQL_TYPES.put(byte[].class, PrimitiveType.Bytes);
        JAVA_TO_YQL_TYPES.put(String.class, PrimitiveType.Text);

        JAVA_TO_STRING_TYPES.put(Boolean.class, "Bool");
        JAVA_TO_STRING_TYPES.put(Byte.class, "Int8");
        JAVA_TO_STRING_TYPES.put(Short.class, "Int16");
        JAVA_TO_STRING_TYPES.put(Integer.class, "Int32");
        JAVA_TO_STRING_TYPES.put(Long.class, "Int64");
        JAVA_TO_STRING_TYPES.put(Float.class, "Float");
        JAVA_TO_STRING_TYPES.put(Double.class, "Double");
        JAVA_TO_STRING_TYPES.put(byte[].class, "Bytes");
        JAVA_TO_STRING_TYPES.put(String.class, "Text");
    }

    public static PrimitiveType getPrimitiveType(Class<?> fieldType) {
        return JAVA_TO_YQL_TYPES.get(fieldType);
    }

    public static String getStringType(Class<?> fieldType) {
        return JAVA_TO_STRING_TYPES.get(fieldType);
    }

    public static PrimitiveValue convertToPrimitiveValue(Object value, Type fieldType) {
        if(value instanceof Boolean && fieldType == Boolean.class) {
            return PrimitiveValue.newBool((Boolean) value);
        } else if (value instanceof Byte && fieldType == Byte.class) {
            return PrimitiveValue.newInt8((Byte) value);
        } else if (value instanceof Short && fieldType == Short.class) {
            return PrimitiveValue.newInt16((Short) value);
        } else if (value instanceof Integer && fieldType == Integer.class) {
            return PrimitiveValue.newInt32((Integer) value);
        } else if (value instanceof Long && fieldType == Long.class) {
            return PrimitiveValue.newInt64((Long) value);
        } else if (value instanceof Float && fieldType == Float.class) {
            return PrimitiveValue.newFloat((Float) value);
        } else if (value instanceof Double && fieldType == Double.class) {
            return PrimitiveValue.newDouble((Double) value);
        } else if (value instanceof byte[] && fieldType == byte[].class) {
            return PrimitiveValue.newBytes((byte[]) value);
        } else if (value instanceof String && fieldType == String.class) {
            return PrimitiveValue.newText((String) value);
        } else {
            throw new VariableTypeException("Unsupported field type: " + value.getClass());
        }
    }

    public static Object convertFromValueReader(ValueReader value, Type fieldType) {
        if(fieldType == Boolean.class) {
            return value.getBool();
        } else if (fieldType == Byte.class) {
            return value.getInt8();
        } else if (fieldType == Short.class) {
            return value.getInt16();
        } else if (fieldType == Integer.class) {
            return value.getInt32();
        } else if (fieldType == Long.class) {
            return value.getInt64();
        } else if (fieldType == Float.class) {
            return value.getFloat();
        } else if (fieldType == Double.class) {
            return value.getDouble();
        } else if (fieldType == byte[].class) {
            return value.getBytes();
        } else if (fieldType == String.class) {
            return value.getText();
        } else {
            throw new VariableTypeException("Unsupported field type: " + value.getClass());
        }
    }
}
