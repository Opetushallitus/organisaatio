package fi.vm.sade.organisaatio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(servers = {@Server(url = "/organisaatio-service", description = "Organisaatiopalvelu")})
@SpringBootApplication
public class OrganisaatioApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrganisaatioApplication.class, args);
	}

}
