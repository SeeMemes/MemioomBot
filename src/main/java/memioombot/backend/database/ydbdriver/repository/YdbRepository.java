package memioombot.backend.database.ydbdriver.repository;

import memioombot.backend.database.entities.UserEntity;
import memioombot.backend.database.ydbdriver.util.PrimitiveTranslator;
import memioombot.backend.database.ydbdriver.config.YdbDatabaseInfo;
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
        try {
            QueryBuilder queryBuilder = QueryBuilder.newBuilder(database, entity)
                    .declareVariables()
                    .addCommand("UPSERT INTO")
                    .addTableConstruct()
                    .build();

            Result<DataQueryResult> result = sessionRetryContext.supplyResult(session ->
                    session.executeDataQuery(queryBuilder.getQuery(), txControl, queryBuilder.getParams())).join();
            if (result.isSuccess()) return entity;
            else return null;
        } catch (IllegalAccessException e) {
            System.err.println("Cannot get field in entity");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
        List<S> savedEntities = new ArrayList<>();
        for (S entity : entities) {
            try {
                QueryBuilder queryBuilder = QueryBuilder.newBuilder(database, entity)
                        .declareVariables()
                        .addCommand("UPSERT INTO")
                        .addTableConstruct()
                        .build();

                Result<DataQueryResult> result = sessionRetryContext.supplyResult(session ->
                        session.executeDataQuery(queryBuilder.getQuery(), txControl, queryBuilder.getParams())).join();
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
        try {
            QueryBuilder queryBuilder = QueryBuilder.newBuilder(database)
                    .declareVariables(id, "uId")
                    .addCommand("SELECT * FROM")
                    .variablesIn()
                    .build();
            Result<DataQueryResult> result = sessionRetryContext.supplyResult(session ->
                    session.executeDataQuery(queryBuilder.getQuery(), txControl, queryBuilder.getParams())).join();
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
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public boolean existsById(ID id) {
        try {
            QueryBuilder queryBuilder = QueryBuilder.newBuilder(database)
                    .declareVariables(id, "uId")
                    .addCommand("SELECT COUNT(*) FROM")
                    .variablesIn()
                    .build();
            DataQueryResult result = sessionRetryContext.supplyResult(session ->
                    session.executeDataQuery(queryBuilder.getQuery(), txControl, queryBuilder.getParams())).join().getValue();
            return result.getResultSet(0).getRowCount() > 0;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<T> findAll() {
        String query
                = "SELECT * from " + database + "; ";
        DataQueryResult result = sessionRetryContext.supplyResult(session ->
                session.executeDataQuery(query, txControl)).join().getValue();
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
        try {
            QueryBuilder.SingleParamBuilder singleParamBuilder = QueryBuilder.newBuilder(database);
            for (ID id : ids) {
                singleParamBuilder
                        .declareVariables(id, "uId");
            }
            singleParamBuilder
                    .addCommand("SELECT * FROM")
                    .variablesIn();

            QueryBuilder queryBuilder = singleParamBuilder.build();
            DataQueryResult result = sessionRetryContext.supplyResult(session ->
                    session.executeDataQuery(queryBuilder.getQuery(), txControl, queryBuilder.getParams())).join().getValue();
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
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long count() {
        String query = "SELECT COUNT(*) FROM " + database + "; ";
        DataQueryResult result = sessionRetryContext.supplyResult(session ->
                session.executeDataQuery(query.toString(), txControl)).join().getValue();
        ResultSetReader rs = result.getResultSet(0);
        return rs.getColumn("column0").getUint64();
    }

    @Override
    public void deleteById(ID id) {
        try {
            QueryBuilder queryBuilder = QueryBuilder.newBuilder(database)
                    .declareVariables(id, "uId")
                    .addCommand("DELETE FROM")
                    .variablesIn()
                    .build();

            TxControl<TxControl.TxSerializableRw> txControl = TxControl.serializableRw().setCommitTx(true);
            sessionRetryContext.supplyResult(session ->
                    session.executeDataQuery(queryBuilder.getQuery(), txControl, queryBuilder.getParams())).join();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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
        try {
            QueryBuilder.SingleParamBuilder singleParamBuilder = QueryBuilder.newBuilder(database);
            for (ID id : ids) {
                singleParamBuilder
                        .declareVariables(id, "uId");
            }
            singleParamBuilder
                    .addCommand("DELETE FROM")
                    .variablesIn();
            QueryBuilder queryBuilder = singleParamBuilder.build();
            sessionRetryContext.supplyResult(session ->
                    session.executeDataQuery(queryBuilder.getQuery(), txControl, queryBuilder.getParams())).join();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

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
}
