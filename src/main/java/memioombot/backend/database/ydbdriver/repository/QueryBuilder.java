package memioombot.backend.database.ydbdriver.repository;

import memioombot.backend.database.ydbdriver.util.PrimitiveTranslator;
import memioombot.backend.database.ydbdriver.util.exceptions.VariableTypeException;
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

    public static ClassBuilder newClassBuilder(String database) {
        return new ClassBuilder(database);
    }

    public static SingleParamBuilder newSingleParamBuilder(String database) {
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
        private List<Field[]> fieldsArray = new ArrayList<>();
        private List<Object> objects = new ArrayList<>();
        private StringBuilder stringBuilder = new StringBuilder();
        private String query = "";
        private Params params;

        public ClassBuilder(String database) {
            this.database = database;
        }

        public ClassBuilder declareVariables(Object object) {
            int listOrd;
            if (objects.size() > 0) {
                Object prevObj = this.objects.get(objects.size() - 1);
                if (!object.getClass().equals(prevObj.getClass())) {
                    throw new VariableTypeException("The type of object differs from previous");
                }
            }

            Field[] fields = object.getClass().getDeclaredFields();
            this.fieldsArray.add(fields);
            objects.add(object);

            listOrd = objects.size() - 1;
            for (Field field : fields) {
                String parameterName = "$" + field.getName() + listOrd;
                stringBuilder
                        .append(" DECLARE ")
                        .append(parameterName)
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
            int listOrd = 0;

            stringBuilder
                    .append(" WHERE ");
            for (Field[] fields : this.fieldsArray) {
                for (Field field : fields) {
                    String fieldName = field.getName();
                    String parameterName = "$" + field.getName() + listOrd;
                    stringBuilder
                            .append(fieldName)
                            .append(" = ")
                            .append(parameterName)
                            .append(" ;\n");
                }
                listOrd++;
            }
            return this;
        }

        public ClassBuilder addTableConstruct() {
            int listOrd = 0;

            stringBuilder
                    .append(" (");
            StringJoiner differentVariables = new StringJoiner(",");
            for (Field field : objects.get(0).getClass().getDeclaredFields()) {
                String parameterName = field.getName();
                differentVariables.add(parameterName);
            }
            stringBuilder
                    .append(differentVariables)
                    .append(")\n")
                    .append("VALUES ");

            StringJoiner differentConstructs = new StringJoiner(",\n");
            for (Field[] fields : fieldsArray) {
                String tableConstruct = "(";
                differentVariables = new StringJoiner(",");
                for (Field field : fields) {
                    String parameterName = "$" + field.getName() + listOrd;
                    differentVariables.add(parameterName);
                }
                tableConstruct += differentVariables + ")";
                differentConstructs.add(tableConstruct);
                listOrd++;
            }

            stringBuilder
                    .append(differentConstructs)
                    .append(";\n");
            return this;
        }

        public QueryBuilder build() throws IllegalAccessException {
            int listOrd = 0;

            Map<String, Value<?>> paramMap = new HashMap<>();

            for (Field[] fields : fieldsArray) {
                for (Field field : fields) {
                    field.setAccessible(true);
                    String parameterName = "$" + field.getName() + listOrd;
                    Object fieldValue = field.get(objects.get(listOrd));
                    Type fieldType = field.getType();
                    paramMap.put(parameterName, PrimitiveTranslator.convertToPrimitiveValue(fieldValue, fieldType));
                }
                listOrd++;
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
                    .append(" WHERE ");
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
