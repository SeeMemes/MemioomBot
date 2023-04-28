package memioombot.backend.database.repositories;

import memioombot.backend.database.entities.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import tech.ydb.core.Result;
import tech.ydb.core.grpc.GrpcTransport;
import tech.ydb.table.SessionRetryContext;
import tech.ydb.table.TableClient;
import tech.ydb.table.query.DataQueryResult;
import tech.ydb.table.query.Params;
import tech.ydb.table.result.ResultSetReader;
import tech.ydb.table.transaction.TxControl;
import tech.ydb.table.values.PrimitiveValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

@Component
public class UserRepository implements CrudRepository<UserEntity, Long> {
    @Value("${ydb.datasource.database}")
    private String database;

    @Autowired
    private SessionRetryContext sessionRetryContext;

    @Autowired
    private GrpcTransport grpcTransport;

    @Autowired
    private TableClient tableClient;

    private final TxControl<TxControl.TxSerializableRw> txControl = TxControl.serializableRw().setCommitTx(true);

    @Override
    public <S extends UserEntity> S save(S entity) {
        String query
                = "DECLARE $uId AS Uint64; " +
                "DECLARE $uName AS Text; " +
                "DECLARE $uDiscriminator AS Uint8; " +
                "UPSERT INTO " + database + " (uId, uName, uDiscriminator) " +
                "VALUES ($uId, $uName, $uDiscriminator); ";

        Params params = Params.of(
                "$uId", PrimitiveValue.newUint64(entity.getUId()),
                "$uName", PrimitiveValue.newText(entity.getuName()),
                "$uDiscriminator", PrimitiveValue.newUint8(entity.getuDiscriminator())
        );
        DataQueryResult result = sessionRetryContext.supplyResult(session -> session.executeDataQuery(query, txControl, params)).join().getValue();
        System.out.println(result);

        return entity;
    }

    @Override
    public <S extends UserEntity> Iterable<S> saveAll(Iterable<S> entities) {
        List<UserEntity> savedEntities = new ArrayList<>();
        for (UserEntity entity : entities) {
            String query
                    = "DECLARE $uId AS Uint64; " +
                    "DECLARE $uName AS Text; " +
                    "DECLARE $uDiscriminator AS Uint8; " +
                    "UPSERT INTO " + database + " (uId, uName, uDiscriminator) " +
                    "VALUES ($uId, $uName, $uDiscriminator); ";

            Params params = Params.of(
                    "$uId", PrimitiveValue.newUint64(entity.getUId()),
                    "$uName", PrimitiveValue.newText(entity.getuName()),
                    "$uDiscriminator", PrimitiveValue.newUint8(entity.getuDiscriminator())
            );
            DataQueryResult result = sessionRetryContext.supplyResult(session -> session.executeDataQuery(query, txControl, params)).join().getValue();
            savedEntities.add(entity);
        }

        return (Iterable<S>) savedEntities;
    }

    @Override
    public Optional<UserEntity> findById(Long aLong) {
        String query
                = "SELECT * from " + database + " WHERE uId = $uId; ";
        Params params = Params.of(
                "$uId", PrimitiveValue.newUint64(aLong)
        );
        Result<DataQueryResult> result = sessionRetryContext.supplyResult(session -> session.executeDataQuery(query, txControl, params)).join();
        ResultSetReader rs = result.getValue().getResultSet(0);
        if (result.isSuccess()) {
            UserEntity userEntity = new UserEntity(
                    rs.getColumn("uId").getUint64(),
                    rs.getColumn("uName").getText(),
                    rs.getColumn("uDiscriminator").getUint8()
            );
            return Optional.of(userEntity);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsById(Long aLong) {
        String query
                = "SELECT COUNT(*) from " + database + " WHERE uId = $uId; ";
        Params params = Params.of(
                "$uId", PrimitiveValue.newUint64(aLong)
        );
        DataQueryResult result = sessionRetryContext.supplyResult(session -> session.executeDataQuery(query, txControl)).join().getValue();

        return result.getResultSet(0).getRowCount() > 0;
    }

    @Override
    public Iterable<UserEntity> findAll() {
        String query
                = "SELECT * from " + database + "; ";
        DataQueryResult result = sessionRetryContext.supplyResult(session -> session.executeDataQuery(query, txControl)).join().getValue();
        ResultSetReader rs = result.getResultSet(0);
        List<UserEntity> userEntities = new ArrayList<>();
        while (rs.next()) {
            userEntities.add(new UserEntity(
                    rs.getColumn("uId").getUint64(),
                    rs.getColumn("uName").getText(),
                    rs.getColumn("uDiscriminator").getUint8()
            ));
        }

        return userEntities;
    }

    @Override
    public Iterable<UserEntity> findAllById(Iterable<Long> longs) {

        StringBuilder query = new StringBuilder();
        Params params = Params.empty();
        StringJoiner idPlaceholders = new StringJoiner(",");
        int paramID = 1;
        for (Long uId : longs) {
            String parameterName = "$uId" + paramID;
            idPlaceholders.add(parameterName);
            params.put(parameterName, PrimitiveValue.newUint64(uId));

            query.append("DECLARE ").append(parameterName).append(" AS Uint64;\n");
        }
        query.append("SELECT * from ").append(database).append(" WHERE uId IN (").append(idPlaceholders).append("); ");

        DataQueryResult result = sessionRetryContext.supplyResult(session -> session.executeDataQuery(query.toString(), txControl, params)).join().getValue();
        ResultSetReader rs = result.getResultSet(0);
        List<UserEntity> userEntities = new ArrayList<>();
        while (rs.next()) {
            userEntities.add(new UserEntity(
                    rs.getColumn("uId").getUint64(),
                    rs.getColumn("uName").getText(),
                    rs.getColumn("uDiscriminator").getUint8()
            ));
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
    public void deleteById(Long aLong) {
        String query
                = "DECLARE $uId AS Uint64; " +
                "DELETE FROM " + database + " WHERE uId = $uId; ";

        Params params = Params.of(
                "$uId", PrimitiveValue.newUint64(aLong)
        );
        TxControl<TxControl.TxSerializableRw> txControl = TxControl.serializableRw().setCommitTx(true);
        DataQueryResult result = sessionRetryContext.supplyResult(session -> session.executeDataQuery(query, txControl, params)).join().getValue();
    }

    @Override
    public void delete(UserEntity entity) {
        deleteById(entity.getUId());
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {
        StringBuilder query = new StringBuilder();
        Params params = Params.empty();
        StringJoiner idPlaceholders = new StringJoiner(",");
        int paramID = 1;
        for (Long uId : longs) {
            String parameterName = "$uId" + paramID;
            idPlaceholders.add(parameterName);
            params.put(parameterName, PrimitiveValue.newUint64(uId));

            query.append("DECLARE ").append(parameterName).append(" AS Uint64;\n");
        }
        query.append("DELETE FROM ").append(database).append(" WHERE uId IN (").append(idPlaceholders).append("); ");

        sessionRetryContext.supplyResult(session -> session.executeDataQuery(query.toString(), txControl, params)).join().getValue();
    }

    @Override
    public void deleteAll(Iterable<? extends UserEntity> entities) {
        List<Long> uIDs = new ArrayList<Long>();
        for (UserEntity entity : entities) {
            uIDs.add(entity.getUId());
        }
        deleteAllById(uIDs);
    }

    @Override
    public void deleteAll() {
        String query = "DELETE FROM " + database;
        TxControl<TxControl.TxSerializableRw> txControl = TxControl.serializableRw().setCommitTx(true);
        DataQueryResult result = sessionRetryContext.supplyResult(session -> session.executeDataQuery(query, txControl)).join().getValue();
    }
}
