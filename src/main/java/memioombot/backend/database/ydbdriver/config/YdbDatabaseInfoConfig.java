package memioombot.backend.database.ydbdriver.config;

import memioombot.backend.database.ydbdriver.annotations.YdbAnnotationScanner;
import memioombot.backend.database.ydbdriver.util.YdbDatabaseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class YdbDatabaseInfoConfig {
    @Autowired
    YdbAnnotationScanner ydbAnnotationScanner;

    @Bean
    public YdbDatabaseInfo ydbDatabaseInfo () {
        return new YdbDatabaseInfo(ydbAnnotationScanner.getDatabase());
    }
}
