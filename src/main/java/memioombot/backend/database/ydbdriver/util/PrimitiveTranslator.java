package memioombot.backend.database.ydbdriver.util;

import memioombot.backend.database.ydbdriver.util.exceptions.VariableTypeException;
import tech.ydb.table.values.PrimitiveType;

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
    public static PrimitiveType convertToPrimitiveType(Object value) {
        if (value instanceof Boolean) {
            return PrimitiveType.Bool;
        } else if (value instanceof Byte) {
            return PrimitiveType.Int8;
        } else if (value instanceof Short) {
            return PrimitiveType.Int16;
        } else if (value instanceof Integer) {
            return PrimitiveType.Int32;
        } else if (value instanceof Long) {
            return PrimitiveType.Int64;
        } else if (value instanceof Float) {
            return PrimitiveType.Float;
        } else if (value instanceof Double) {
            return PrimitiveType.Double;
        } else if (value instanceof byte[]) {
            return PrimitiveType.Bytes;
        } else if (value instanceof String) {
            return PrimitiveType.Text;
        } else {
            throw new VariableTypeException(value.getClass().getName());
        }
    }
}
