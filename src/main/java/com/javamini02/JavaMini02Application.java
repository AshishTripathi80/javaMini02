package com.javamini02;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class JavaMini02Application {

	public static void main(String[] args) {
		SpringApplication.run(JavaMini02Application.class, args);
	}

}
