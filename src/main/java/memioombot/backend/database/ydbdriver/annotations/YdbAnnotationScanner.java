package memioombot.backend.database.ydbdriver.annotations;

import memioombot.backend.database.ydbdriver.util.PrimitiveTranslator;
import memioombot.backend.database.ydbdriver.util.exceptions.PrimaryKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import tech.ydb.table.SessionRetryContext;
import tech.ydb.table.description.TableDescription;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.Map;

@Component
public class YdbAnnotationScanner {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SessionRetryContext sessionRetryContext;

    private String database;

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
                    break;
                }
            }
            if (!primaryKeyName.isEmpty()) {
                YdbEntity annotation = entityClass.getAnnotation(YdbEntity.class);
                String tableName = annotation.dbName().isEmpty() ? entityClass.getSimpleName() : annotation.dbName();
                this.database = tableName;
                createTableInDatabase(tableName, primaryKeyName, fields);
            } else throw new PrimaryKeyException("Primary key is not set");
        }
    }

    private void createTableInDatabase(String tableName, String primaryKeyName, Field[] fields) {
        TableDescription.Builder tableBuilder = TableDescription.newBuilder().setPrimaryKey(primaryKeyName);
        for (Field field : fields) {
            tableBuilder.addNullableColumn(field.getName(), PrimitiveTranslator.convertToPrimitiveType(field));
        }
        TableDescription tableToCreate = tableBuilder.build();

        sessionRetryContext.supplyStatus(session -> session.createTable(tableName, tableToCreate))
                .join().expectSuccess("Can't create table " + tableName);
    }

    public String getDatabase () {
        return database;
    }
}