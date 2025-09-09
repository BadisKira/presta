package com.presta;

import org.springframework.boot.SpringApplication;

public class PrestaApplicationTests {

	public static void main(String[] args) {
		SpringApplication.from(PrestaApplicationTests::main).with(TestcontainersConfiguration.class).run(args);
	}

}
