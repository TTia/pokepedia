package com.ttia.pokepedia;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(
		title = "Pokepedia API",
		version = "1.0.0",
		description = "A Pokedex REST API serving Pokemon info with optional fun translations (Yoda/Shakespeare)"
))
@SpringBootApplication
public class PokepediaApplication {

	public static void main(String[] args) {
		SpringApplication.run(PokepediaApplication.class, args);
	}

}
