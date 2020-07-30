package com.wittsfamily.approximations.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.wittsfamily.approximations.finder")
@ComponentScan("com.wittsfamily.approximations.rest")
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
