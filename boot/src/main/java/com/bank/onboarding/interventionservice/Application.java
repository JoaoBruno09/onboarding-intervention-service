package com.bank.onboarding.interventionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = "com.bank.onboarding")
@EnableMongoRepositories(basePackages = "com.bank.onboarding.commonslib.persistence.repositories")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
