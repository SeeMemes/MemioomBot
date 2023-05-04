package memioombot.backend.database.ydbdriver.annotations;

import memioombot.backend.database.ydbdriver.util.PrimitiveTranslator;
import memioombot.backend.database.ydbdriver.util.exceptions.CreateTableException;
import memioombot.backend.database.ydbdriver.util.exceptions.PrimaryKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import tech.ydb.core.Result;
import tech.ydb.core.Status;
import tech.ydb.core.grpc.GrpcTransport;
import tech.ydb.table.SessionRetryContext;
import tech.ydb.table.description.TableDescription;
import tech.ydb.table.query.DataQueryResult;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Component
public class YdbAnnotationScanner {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private SessionRetryContext sessionRetryContext;
    @Autowired
    private GrpcTransport grpcTransport;
    private Map<String, String> database = new HashMap<>();
    private Map<String, Field[]> fieldsMap = new HashMap<>();
    private Field primaryKey;

    @PostConstruct
    public void scanAndCreateDatabase() {
        Map<String, Object> ydbEntityBeans = applicationContext.getBeansWithAnnotation(YdbEntity.class);

        for (Object ydbEntityBean : ydbEntityBeans.values()) {
            Class<?> entityClass = ydbEntityBean.getClass();
            Field[] fields = entityClass.getDeclaredFields();
            String primaryKeyName = "";

            for (Field field : fields) {
                if (field.isAnnotationPresent(YdbPrimaryKey.class)) {
                    primaryKeyName = field.getName();
                    this.primaryKey = field;
                    break;
                }
            }
            if (!primaryKeyName.isEmpty()) {
                YdbEntity annotation = entityClass.getAnnotation(YdbEntity.class);
                String tableName = annotation.dbName().isEmpty() ? entityClass.getSimpleName() : annotation.dbName();
                this.database.put(entityClass.getName(), tableName);
                this.fieldsMap.put(entityClass.getName(), fields);
                createTableInDatabase(tableName, primaryKeyName, fields);
            } else throw new PrimaryKeyException("Primary key is not set");
        }
    }

    private void createTableInDatabase(String tableName, String primaryKeyName, Field[] fields) {
        TableDescription.Builder tableBuilder = TableDescription.newBuilder();
        for (Field field : fields) {
            tableBuilder.addNullableColumn(field.getName(), PrimitiveTranslator.getPrimitiveType(field.getType()));
        }
        tableBuilder.setPrimaryKey(primaryKeyName);
        TableDescription tableToCreate = tableBuilder.build();

        Status result = sessionRetryContext.supplyStatus(session -> session.createTable(grpcTransport.getDatabase() + "/" + tableName, tableToCreate))
                .join();
        if (!result.isSuccess()) throw new CreateTableException(tableName);
    }

    public Map<String, String> getDatabase() {
        return database;
    }

    public Map<String, Field[]> getFieldsMap() {
        return fieldsMap;
    }

    public Field getPrimaryKey() {
        return primaryKey;
    }
}
