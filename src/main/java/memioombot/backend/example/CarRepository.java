package memioombot.backend.example;

import memioombot.backend.database.ydbdriver.annotations.YdbStorage;
import memioombot.backend.database.ydbdriver.repository.QueryBuilder;
import memioombot.backend.example.CarEntity;
import memioombot.backend.database.ydbdriver.repository.YdbRepository;
import org.springframework.stereotype.Component;
import tech.ydb.table.result.ResultSetReader;

@YdbStorage
public class CarRepository extends YdbRepository<CarEntity, Long> {

    public CarEntity selectCarByNumber (String number) {
        try {
            QueryBuilder queryBuilder = QueryBuilder.newSingleParamBuilder(getDatabase())
                    .declareVariables(number, "number")
                    .addCommand("SELECT * FROM")
                    .variablesIn()
                    .build();
            ResultSetReader resultSetReader = getSessionRetryContext().supplyResult(session ->
                    session.executeDataQuery(queryBuilder.getQuery(), getTxControl(), queryBuilder.getParams())).join().getValue().getResultSet(0);
            return resultSetReader.next() ? createEntity(resultSetReader) : null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
