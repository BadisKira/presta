package com.presta;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ComponentScan(basePackages = {
        "com.presta.infrastructure",
        "com.presta.application",
})
class PrestaProjectApplicationTests {


	void contextLoads() {
	}

}
