package com.hisaab_khata.hisaab_khata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class HisaabKhataApplication {

	public static void main(String[] args) {
		SpringApplication.run(HisaabKhataApplication.class, args);
	}

}
