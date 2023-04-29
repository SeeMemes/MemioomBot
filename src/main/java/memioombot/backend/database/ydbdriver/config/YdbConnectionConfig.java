package memioombot.backend.database.ydbdriver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import tech.ydb.core.grpc.GrpcTransport;
import tech.ydb.table.SessionRetryContext;
import tech.ydb.table.TableClient;

@Configuration
@PropertySource("classpath:application.properties")
public class YdbConnectionConfig {

    @Value("${ydb.datasource.url}")
    private String endpoint;

    @Bean
    public GrpcTransport grpcTransportSetup() {
        return GrpcTransport.forConnectionString(endpoint)
                .build();
    }

    @Bean
    public TableClient tableSetup(GrpcTransport grpcTransport) {
        return TableClient.newClient(grpcTransport)
                .build();
    }

    @Bean
    public SessionRetryContext sessionSetup(TableClient tableClient) {
        return SessionRetryContext.create(tableClient)
                .build();
    }
}
