package memioombot.backend.database.ydbdriver.util;

import memioombot.backend.database.ydbdriver.util.exceptions.VariableTypeException;
import tech.ydb.table.values.PrimitiveType;

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
    }

    public static PrimitiveType convertToPrimitiveType(Class<?> fieldType) {
        return JAVA_TO_YQL_TYPES.get(fieldType);
    }
}
