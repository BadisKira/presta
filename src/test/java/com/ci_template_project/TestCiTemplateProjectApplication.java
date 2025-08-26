package com.ci_template_project;

import org.springframework.boot.SpringApplication;

public class TestCiTemplateProjectApplication {

	public static void main(String[] args) {
		SpringApplication.from(CiTemplateProjectApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
