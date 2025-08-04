package com.beautiflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.beautiflow.**.repository")
public class BeautiflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeautiflowApplication.class, args);
	}

}
