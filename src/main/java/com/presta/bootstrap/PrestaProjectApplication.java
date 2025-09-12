package com.presta.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.presta.infrastructure",
        "com.presta.application"
})
@EnableJpaRepositories("com.presta.infrastructure.persistence.repositories")
@EntityScan("com.presta.infrastructure.persistence.entities")
public class PrestaProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrestaProjectApplication.class, args);
	}

}
