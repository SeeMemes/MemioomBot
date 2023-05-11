package memioombot.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class SpringApp {

    public static void main(String[] args) {
        SpringApplication.run(SpringApp.class);
    }
}