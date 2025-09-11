package com.presta;

import org.springframework.boot.SpringApplication;
import org.springframework.test.context.ActiveProfiles;



@ActiveProfiles("test")
public class PrestaApplicationTests {

    public static void main(String[] args) {
        SpringApplication.from(PrestaApplicationTests::main).with(TestcontainersConfiguration.class).run(args);
    }

}
