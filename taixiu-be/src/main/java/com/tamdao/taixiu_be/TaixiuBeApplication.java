package com.tamdao.taixiu_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TaixiuBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaixiuBeApplication.class, args);
	}

}
