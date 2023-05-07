package memioombot.backend.database.ydbdriver.repository;

import memioombot.backend.database.ydbdriver.config.YdbDatabaseInfo;
import memioombot.backend.database.ydbdriver.util.PrimitiveTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import tech.ydb.core.Result;
import tech.ydb.table.SessionRetryContext;
import tech.ydb.table.query.DataQueryResult;
import tech.ydb.table.result.ResultSetReader;
import tech.ydb.table.transaction.TxControl;

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
    private String entityTypeName;
    private Class<?> entityClass;
    private Field primaryKey;
    private Field[] fields;
    private String database;
    private final TxControl<TxControl.TxSerializableRw> txControl = TxControl.serializableRw().setCommitTx(true);


    @PostConstruct
    private void completeDatabaseInfo() {
        Type type = getClass().getGenericSuperclass();
        ParameterizedType paramType = (ParameterizedType) type;
        Type[] typeArguments = paramType.getActualTypeArguments();
        this.entityClass = (Class<?>) typeArguments[0];
        this.entityTypeName = typeArguments[0].getTypeName();
        this.database = ydbDatabaseInfo.getDatabase(entityTypeName);
        this.fields = ydbDatabaseInfo.getFields(database);
        this.primaryKey = ydbDatabaseInfo.getPrimaryKey(database);
        this.primaryKey.setAccessible(true);
    }

    @Override
    public <S extends T> S save(S entity) {
        try {
            QueryBuilder queryBuilder = QueryBuilder.newClassBuilder(database)
                    .declareVariables(entity)
                    .addCommand("UPSERT INTO")
                    .addTableConstruct()
                    .build();

            Result<DataQueryResult> result = sessionRetryContext.supplyResult(session ->
                    session.executeDataQuery(queryBuilder.getQuery(), txControl, queryBuilder.getParams())).join();
            if (result.isSuccess()) return entity;
            else return null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to save entity", e);
        }
    }

    @Override
    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
        try {
            List<S> savedEntities = new ArrayList<>();
            QueryBuilder.ClassBuilder classBuilder = QueryBuilder.newClassBuilder(database);
            for (S entity : entities) {
                classBuilder.declareVariables(entity);
                savedEntities.add(entity);
            }
            QueryBuilder queryBuilder = classBuilder
                    .addCommand("UPSERT INTO")
                    .addTableConstruct()
                    .build();
            sessionRetryContext.supplyResult(session ->
                    session.executeDataQuery(queryBuilder.getQuery(), txControl, queryBuilder.getParams())).join();
            return savedEntities;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to save all entities by id", e);
        }
    }

    @Override
    public Optional<T> findById(ID id) {
        try {
            QueryBuilder queryBuilder = QueryBuilder.newSingleParamBuilder(database)
                    .declareVariables(id, "uId")
                    .addCommand("SELECT * FROM")
                    .variablesIn()
                    .build();
            Result<DataQueryResult> result = sessionRetryContext.supplyResult(session ->
                    session.executeDataQuery(queryBuilder.getQuery(), txControl, queryBuilder.getParams())).join();
            ResultSetReader rs = result.getValue().getResultSet(0);
            if (result.isSuccess()) {
                return Optional.of(createEntity(rs));
            } else {
                return Optional.empty();
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to find entity by id", e);
        }
    }

    @Override
    public boolean existsById(ID id) {
        try {
            QueryBuilder queryBuilder = QueryBuilder.newSingleParamBuilder(database)
                    .declareVariables(id, "uId")
                    .addCommand("SELECT COUNT(*) FROM")
                    .variablesIn()
                    .build();
            DataQueryResult result = sessionRetryContext.supplyResult(session ->
                    session.executeDataQuery(queryBuilder.getQuery(), txControl, queryBuilder.getParams())).join().getValue();
            return result.getResultSet(0).getRowCount() > 0;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to determine entity by id", e);
        }
    }

    @Override
    public Iterable<T> findAll() {
        try {
            QueryBuilder queryBuilder = QueryBuilder.newSingleParamBuilder(database)
                    .addCommand("SELECT * FROM")
                    .build();
            DataQueryResult result = sessionRetryContext.supplyResult(session ->
                    session.executeDataQuery(queryBuilder.getQuery(), txControl)).join().getValue();
            ResultSetReader rs = result.getResultSet(0);
            List<T> Entities = new ArrayList<>();
            while (rs.next()) {
                Entities.add(createEntity(rs));
            }
            return Entities;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to find all entities", e);
        }
    }

    @Override
    public Iterable<T> findAllById(Iterable<ID> ids) {
        try {
            QueryBuilder.SingleParamBuilder singleParamBuilder = QueryBuilder.newSingleParamBuilder(database);
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
            List<T> Entities = new ArrayList<>();
            while (rs.next()) {
                Entities.add(createEntity(rs));
            }
            return Entities;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to find all entities by id", e);
        }
    }

    @Override
    public long count() {
        try {
            QueryBuilder queryBuilder = QueryBuilder.newSingleParamBuilder(database)
                    .addCommand("SELECT COUNT(*) FROM")
                    .build();
            DataQueryResult result = sessionRetryContext.supplyResult(session ->
                    session.executeDataQuery(queryBuilder.getQuery(), txControl)).join().getValue();
            ResultSetReader rs = result.getResultSet(0);
            return rs.getColumn("column0").getUint64();
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to count entities", e);
        }
    }

    @Override
    public void deleteById(ID id) {
        try {
            QueryBuilder queryBuilder = QueryBuilder.newSingleParamBuilder(database)
                    .declareVariables(id, "uId")
                    .addCommand("DELETE FROM")
                    .variablesIn()
                    .build();

            TxControl<TxControl.TxSerializableRw> txControl = TxControl.serializableRw().setCommitTx(true);
            sessionRetryContext.supplyResult(session ->
                    session.executeDataQuery(queryBuilder.getQuery(), txControl, queryBuilder.getParams())).join();
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to delete entity by id", e);
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
            QueryBuilder.SingleParamBuilder singleParamBuilder = QueryBuilder.newSingleParamBuilder(database);
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
            throw new RuntimeException("Failed to delete all entities by id", e);
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
        try {
            QueryBuilder queryBuilder = QueryBuilder.newSingleParamBuilder(database)
                    .addCommand("DELETE FROM")
                    .build();
            sessionRetryContext.supplyResult(session ->
                    session.executeDataQuery(queryBuilder.getQuery(), txControl)).join().getValue();
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to delete all entities", e);
        }

    }

    private ID getId(T entity) throws IllegalAccessException {
        Object fieldValue = this.primaryKey.get(entity);
        return (ID) fieldValue;
    }

    public T createEntity(ResultSetReader rs) {
        try {
            T entity = (T) entityClass.newInstance();
            for (Field field : fields) {
                Object fieldValue = PrimitiveTranslator.convertFromValueReader(rs.getColumn(field.getName()), field.getType());
                field.setAccessible(true);
                field.set(entity, fieldValue);
            }
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create entity", e);
        }
    }
}
