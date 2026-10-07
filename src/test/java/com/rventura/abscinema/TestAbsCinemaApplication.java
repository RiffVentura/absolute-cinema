package com.rventura.abscinema;

import org.springframework.boot.SpringApplication;

public class TestAbsCinemaApplication {

	public static void main(String[] args) {
		SpringApplication.from(AbsCinemaApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
