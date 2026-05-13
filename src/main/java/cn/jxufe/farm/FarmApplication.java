package cn.jxufe.farm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ConfigurationPropertiesScan("cn.jxufe.farm.config.properties")
@SpringBootApplication
@EntityScan(basePackages = "cn.jxufe.farm.model.entity")
@EnableJpaRepositories(basePackages = "cn.jxufe.farm.model.dao")
public class FarmApplication {

    public static void main(String[] args) {
        SpringApplication.run(FarmApplication.class, args);
    }

}
