package memioombot.backend.database.ydbdriver.repository;

import memioombot.backend.database.ydbdriver.util.PrimitiveTranslator;
import tech.ydb.table.query.Params;
import tech.ydb.table.values.Value;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

public class QueryBuilder {
    private final String query;
    private final Params params;

    private QueryBuilder(ClassBuilder builder) {
        this.query = builder.query;
        this.params = builder.params;
    }

    private QueryBuilder(SingleParamBuilder builder) {
        this.query = builder.query;
        this.params = builder.params;
    }

    public static ClassBuilder newBuilder(String database, Object object) {
        return new ClassBuilder(database, object);
    }

    public static SingleParamBuilder newBuilder (String database) {
        return new SingleParamBuilder(database);
    }

    public Params getParams() {
        return params;
    }

    public String getQuery() {
        return query;
    }

    public static class ClassBuilder {
        private String database;
        private Field[] fields;
        private Object object;
        private StringBuilder stringBuilder = new StringBuilder();
        private String query = "";
        private Params params;

        public ClassBuilder(String database, Object object) {
            this.database = database;
            this.fields = object.getClass().getDeclaredFields();
            this.object = object;
        }

        public ClassBuilder declareVariables() {
            for (Field field : fields) {
                stringBuilder
                        .append(" DEClARE $")
                        .append(field.getName())
                        .append(" AS ")
                        .append(PrimitiveTranslator.getStringType(field.getType()))
                        .append(" ;\n");
            }
            return this;
        }

        public ClassBuilder addCommand(String commandType) {
            stringBuilder
                    .append(commandType)
                    .append(" ")
                    .append(database)
                    .append(" ");
            return this;
        }

        public ClassBuilder addWhereCondition() {
            stringBuilder
                    .append(" WHERE ");
            for (Field field : fields) {
                stringBuilder
                        .append(field.getName())
                        .append(" = ")
                        .append(" $")
                        .append(field.getName())
                        .append(" ;\n");
            }
            return this;
        }

        public ClassBuilder addTableConstruct() {
            int i = 0;
            stringBuilder
                    .append(" (");
            for (Field field : fields) {
                stringBuilder
                        .append(field.getName());
                if (++i < fields.length)
                    stringBuilder
                            .append(", ");
            }

            i = 0;
            stringBuilder
                    .append(")\n")
                    .append("VALUES (");
            for (Field field : fields) {
                stringBuilder
                        .append("$")
                        .append(field.getName());
                if (++i < fields.length)
                    stringBuilder
                            .append(", ");
            }
            stringBuilder
                    .append(");\n");
            return this;
        }

        public QueryBuilder build() throws IllegalAccessException {
            Map<String, Value<?>> paramMap = new HashMap<>();

            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object fieldValue = field.get(object);
                Type fieldType = field.getType();
                paramMap.put("$" + fieldName, PrimitiveTranslator.convertToPrimitiveValue(fieldValue, fieldType));
            }

            this.query = stringBuilder.toString();
            this.params = Params.copyOf(paramMap);
            return new QueryBuilder(this);
        }
    }

    public static class SingleParamBuilder {
        private String database;
        private String objectLastName;
        private List<List<Object>> objects = new ArrayList<>();
        private List<String> objectNames = new ArrayList<>();
        private StringBuilder stringBuilder = new StringBuilder();
        private String query = "";
        private Params params;

        public SingleParamBuilder(String database) {
            this.database = database;
        }

        public SingleParamBuilder declareVariables(Object object, String objectName) {
            if (!objectName.equals(objectLastName)) {
                List<Object> newObjList = new ArrayList<>();
                newObjList.add(object);
                objects.add(newObjList);
                objectNames.add(objectName);
            } else {
                objects.get(objects.size() - 1).add(object);
            }
            int objNum = objects.get(objects.size() - 1).size() - 1;
            String queryName = objectName + objNum;
            stringBuilder
                    .append(" DECLARE $")
                    .append(queryName)
                    .append(" AS ")
                    .append(PrimitiveTranslator.getStringType(object.getClass()))
                    .append(" ;\n");
            return this;
        }

        public SingleParamBuilder addCommand(String commandType) {
            stringBuilder
                    .append(commandType)
                    .append(" ")
                    .append(database)
                    .append(" ");
            return this;
        }

        public SingleParamBuilder variablesIn() {
            int paramID = 0;
            int listOrd = 0;
            stringBuilder
                    .append("WHERE ");
            StringJoiner differentObjectsJoiner = new StringJoiner(" or ");
            for (List<Object> list : objects) {
                String lastName = objectNames.get(listOrd);

                String objectsIn = lastName + " IN (";
                StringJoiner differentVariables = new StringJoiner(",");
                for (Object object : list) {
                    String parameterName = "$" + lastName + paramID;
                    differentVariables.add(parameterName);
                    paramID++;
                }

                objectsIn += differentVariables + ") ";
                differentObjectsJoiner.add(objectsIn);

                listOrd++;
                paramID = 0;
            }
            stringBuilder
                    .append(differentObjectsJoiner);
            return this;
        }

        public QueryBuilder build() throws IllegalAccessException {
            Map<String, Value<?>> paramMap = new HashMap<>();

            int paramID = 0;
            int listOrd = 0;
            for (List<Object> objectList : objects) {
                for (Object object : objectList) {
                    String objectName = objectNames.get(listOrd) + paramID;
                    Type fieldType = object.getClass();
                    paramMap.put("$" + objectName, PrimitiveTranslator.convertToPrimitiveValue(object, fieldType));
                    paramID++;
                }
                paramID = 0;
                listOrd++;
            }

            this.query = stringBuilder.toString();
            this.params = Params.copyOf(paramMap);
            return new QueryBuilder(this);
        }
    }
}
