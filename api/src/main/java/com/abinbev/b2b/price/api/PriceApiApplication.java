package com.abinbev.b2b.price.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableWebSecurity
public class PriceApiApplication {

	public static void main(final String[] args) {

		SpringApplication.run(PriceApiApplication.class, args);
	}

}
