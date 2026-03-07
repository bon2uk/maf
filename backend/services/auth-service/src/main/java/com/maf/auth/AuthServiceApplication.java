package com.maf.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthServiceApplication {

	public static void main(String[] args) {
		System.out.println("SPRING_PROFILES_ACTIVE = " + System.getenv("SPRING_PROFILES_ACTIVE"));

		SpringApplication.run(AuthServiceApplication.class, args);
	}

}
