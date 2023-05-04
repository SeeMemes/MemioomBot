package memioombot.backend.database.ydbdriver;

import memioombot.backend.database.entities.UserEntity;
import memioombot.backend.database.ydbdriver.util.PrimitiveTranslator;
import memioombot.backend.database.ydbdriver.util.YdbDatabaseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import tech.ydb.core.Result;
import tech.ydb.table.SessionRetryContext;
import tech.ydb.table.query.DataQueryResult;
import tech.ydb.table.query.Params;
import tech.ydb.table.result.ResultSetReader;
import tech.ydb.table.transaction.TxControl;
import tech.ydb.table.values.Value;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@NoRepositoryBean
public abstract class YdbRepository<T, ID> implements CrudRepository<T, ID> {

    @Autowired
    private YdbDatabaseInfo ydbDatabaseInfo;
    @Autowired
    private SessionRetryContext sessionRetryContext;
    String entityType;
    private final TxControl<TxControl.TxSerializableRw> txControl = TxControl.serializableRw().setCommitTx(true);
    private String database;

    @PostConstruct
    private void completeDatabaseInfo() {
        Type type = getClass().getGenericSuperclass();
        ParameterizedType paramType = (ParameterizedType) type;
        Type[] typeArguments = paramType.getActualTypeArguments();
        this.entityType = typeArguments[0].getTypeName();
        this.database = ydbDatabaseInfo.getDatabase(entityType);
    }

    @Override
    public <S extends T> S save(S entity) {
        String query
                = "DECLARE $uId AS Int64; " +
                "DECLARE $uName AS Utf8; " +
                "DECLARE $uDiscriminator AS Int32; " +
                "UPSERT INTO " + database + " (uId, uName, uDiscriminator) " +
                "VALUES ($uId, $uName, $uDiscriminator); ";
        try {
            Params params = buildClassParams(entity);
            Result<DataQueryResult> result = sessionRetryContext.supplyResult(session -> session.executeDataQuery(query, txControl, params)).join();
            if (result.isSuccess()) return entity;
            else return null;
        } catch (IllegalAccessException e) {
            System.err.println("Cannot get field in entity");
            e.printStackTrace();
        }

        return entity;
    }

    @Override
    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
        List<S> savedEntities = new ArrayList<>();
        for (S entity : entities) {
            String query
                    = "DECLARE $uId AS Int64; " +
                    "DECLARE $uName AS Utf8; " +
                    "DECLARE $uDiscriminator AS Int32; " +
                    "UPSERT INTO " + database + " (uId, uName, uDiscriminator) " +
                    "VALUES ($uId, $uName, $uDiscriminator); ";
            try {
                Params params = buildClassParams(entity);
                Result<DataQueryResult> result = sessionRetryContext.supplyResult(session -> session.executeDataQuery(query, txControl, params)).join();

                if (result.isSuccess()) savedEntities.add(entity);
            } catch (IllegalAccessException e) {
                System.err.println("Cannot get field in entity");
                e.printStackTrace();
            }
        }
        return savedEntities;
    }

    @Override
    public Optional<T> findById(ID id) {
        String query
                = "DECLARE $uId AS Int64; " +
                "SELECT * from " + database + " WHERE uId = $uId; ";

        //EXPERIMENTAL
        Params params = buildIdParams(id, "$uId");

        Result<DataQueryResult> result = sessionRetryContext.supplyResult(session -> session.executeDataQuery(query, txControl, params)).join();
        ResultSetReader rs = result.getValue().getResultSet(0);
        if (result.isSuccess()) {
            //TO BE DONE
            UserEntity userEntity = new UserEntity(
                    rs.getColumn("uId").getInt64(),
                    rs.getColumn("uName").getText(),
                    rs.getColumn("uDiscriminator").getInt32()
            );
            return Optional.of((T) userEntity);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsById(ID id) {
        String query
                = "DECLARE $uId AS Int64; " +
                "SELECT COUNT(*) from " + database + " WHERE uId = $uId; ";

        //EXPERIMENTAL
        Params params = buildIdParams(id, "$uId");

        DataQueryResult result = sessionRetryContext.supplyResult(session -> session.executeDataQuery(query, txControl, params)).join().getValue();

        return result.getResultSet(0).getRowCount() > 0;
    }

    @Override
    public Iterable<T> findAll() {
        String query
                = "SELECT * from " + database + "; ";
        DataQueryResult result = sessionRetryContext.supplyResult(session -> session.executeDataQuery(query, txControl)).join().getValue();
        ResultSetReader rs = result.getResultSet(0);
        List<T> userEntities = new ArrayList<>();
        while (rs.next()) {
            //TO BE DONE
            UserEntity userEntity = new UserEntity(
                    rs.getColumn("uId").getInt64(),
                    rs.getColumn("uName").getText(),
                    rs.getColumn("uDiscriminator").getInt32()
            );
            userEntities.add((T) userEntity);
        }

        return userEntities;
    }

    @Override
    public Iterable<T> findAllById(Iterable<ID> ids) {
        StringBuilder query = new StringBuilder();
        Params params = Params.empty();
        StringJoiner idPlaceholders = new StringJoiner(",");
        int paramID = 1;
        for (ID id : ids) {
            String parameterName = "$uId" + paramID;
            idPlaceholders.add(parameterName);

            //EXPERIMENTAL
            buildIdParams(id, parameterName);

            query.append("DECLARE ").append(parameterName).append(" AS Int64;\n");
        }
        query.append("SELECT * FROM ").append(database).append(" WHERE uId IN (").append(idPlaceholders).append("); ");

        DataQueryResult result = sessionRetryContext.supplyResult(session -> session.executeDataQuery(query.toString(), txControl, params)).join().getValue();
        ResultSetReader rs = result.getResultSet(0);
        List<T> userEntities = new ArrayList<>();
        while (rs.next()) {
            //TO BE DONE
            UserEntity userEntity = new UserEntity(
                    rs.getColumn("uId").getInt64(),
                    rs.getColumn("uName").getText(),
                    rs.getColumn("uDiscriminator").getInt32()
            );
            userEntities.add((T) userEntity);
        }

        return userEntities;
    }

    @Override
    public long count() {
        String query = "SELECT COUNT(*) FROM " + database + "; ";
        DataQueryResult result = sessionRetryContext.supplyResult(session -> session.executeDataQuery(query.toString(), txControl)).join().getValue();
        ResultSetReader rs = result.getResultSet(0);
        return rs.getColumn("column0").getUint64();
    }

    @Override
    public void deleteById(ID id) {
        String query
                = "DECLARE $uId AS Int64; " +
                "DELETE FROM " + database + " WHERE uId = $uId; ";

        //EXPERIMENTAL
        Params params = buildIdParams(id, "$uId");

        TxControl<TxControl.TxSerializableRw> txControl = TxControl.serializableRw().setCommitTx(true);
        sessionRetryContext.supplyResult(session -> session.executeDataQuery(query, txControl, params)).join().getValue();
    }

    @Override
    public void delete(T entity) {
        try {
            deleteById(getId(entity));
        } catch (IllegalAccessException e) {
            System.err.println("Cannot get ID of entity");
            e.printStackTrace();
        }
    }

    @Override
    public void deleteAllById(Iterable<? extends ID> ids) {
        StringBuilder query = new StringBuilder();
        Params params = Params.empty();
        StringJoiner idPlaceholders = new StringJoiner(",");
        int paramID = 1;
        for (ID id : ids) {
            String parameterName = "$uId" + paramID;
            idPlaceholders.add(parameterName);

            //EXPERIMENTAL
            buildIdParams(id, parameterName);

            query.append("DECLARE ").append(parameterName).append(" AS Int64;\n");
        }
        query.append("DELETE FROM ").append(database).append(" WHERE uId IN (").append(idPlaceholders).append("); ");

        sessionRetryContext.supplyResult(session -> session.executeDataQuery(query.toString(), txControl, params)).join().getValue();
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        List<ID> IDs = new ArrayList<>();
        for (T entity : entities) {
            try {
                IDs.add(getId(entity));
            } catch (IllegalAccessException e) {
                System.err.println("Cannot get ID of entity");
                e.printStackTrace();
            }
        }
        deleteAllById(IDs);
    }

    @Override
    public void deleteAll() {
        String query = "DELETE FROM " + database;
        TxControl<TxControl.TxSerializableRw> txControl = TxControl.serializableRw().setCommitTx(true);
        sessionRetryContext.supplyResult(session -> session.executeDataQuery(query, txControl)).join().getValue();
    }

    private ID getId(T entity) throws IllegalAccessException {
        Field field = ydbDatabaseInfo.getPrimaryKey();
        field.setAccessible(true);
        Object fieldValue = field.get(entity);
        return (ID) fieldValue;
    }

    private Params buildClassParams(T entity) throws IllegalAccessException {
        Map<String, Value<?>> params = new HashMap<>();
        Field[] fields = ydbDatabaseInfo.getFields(entityType);

        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            Object fieldValue = field.get(entity);
            Type fieldType = field.getType();
            params.put("$" + fieldName, PrimitiveTranslator.convertToPrimitiveValue(fieldValue, fieldType));
        }

        return Params.copyOf(params);
    }

    private Params buildIdParams(ID id, String paramName) {
        Map<String, Value<?>> params = new HashMap<>();
        Type idType = ydbDatabaseInfo.getPrimaryKey().getType();
        params.put(paramName, PrimitiveTranslator.convertToPrimitiveValue(id, idType));

        return Params.copyOf(params);
    }
}
