package memioombot.backend.database.repositories;

import memioombot.backend.database.entities.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import tech.ydb.core.grpc.GrpcTransport;
import tech.ydb.table.SessionRetryContext;
import tech.ydb.table.TableClient;
import tech.ydb.table.query.DataQueryResult;
import tech.ydb.table.query.Params;
import tech.ydb.table.transaction.TxControl;
import tech.ydb.table.values.PrimitiveValue;

import java.util.Optional;

@Component
public class UserRepository implements CrudRepository<UserEntity, Long> {
    @Value("${ydb.datasource.database}") private String database;

    @Autowired private SessionRetryContext sessionRetryContext;

    @Autowired private GrpcTransport grpcTransport;

    @Autowired private TableClient tableClient;

    @Override
    public <S extends UserEntity> S save(S entity) {
        String query
                = "UPSERT INTO blacklistUsers (uId, uName, uDiscriminator) "
                + "VALUES ($uId, $uName, $uDiscriminator);";

        Params params = Params.of(
                //"$database", PrimitiveValue.newText(database),
                "$uId", PrimitiveValue.newInt64(entity.getUId()),
                "$uName", PrimitiveValue.newText(entity.getuName()),
                "$uDiscriminator", PrimitiveValue.newUint8(entity.getuDiscriminator())
        );

        TxControl txControl = TxControl.serializableRw().setCommitTx(true);

        DataQueryResult result = sessionRetryContext.supplyResult(session -> session.executeDataQuery(query, txControl, params)).join().getValue();

        System.out.println(result);

        return entity;
    }

    @Override
    public <S extends UserEntity> Iterable<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<UserEntity> findById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public Iterable<UserEntity> findAll() {
        return null;
    }

    @Override
    public Iterable<UserEntity> findAllById(Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Long aLong) {
        String query
                = "DELETE FROM $database WHERE uId = $uId;";

        Params params = Params.of(
                "$database", PrimitiveValue.newText(database),
                "$uId", PrimitiveValue.newInt64(aLong)
        );

        TxControl txControl = TxControl.serializableRw().setCommitTx(true);

        DataQueryResult result = sessionRetryContext.supplyResult(session -> session.executeDataQuery(query, txControl, params)).join().getValue();

        System.out.println(result);
    }

    @Override
    public void delete(UserEntity entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {

    }

    @Override
    public void deleteAll(Iterable<? extends UserEntity> entities) {

    }

    @Override
    public void deleteAll() {

    }
}