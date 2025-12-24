package it.sky.keplero;

import it.sky.keplero.controller.ContractController;
import it.sky.keplero.service.ContractProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.List;

@SpringBootApplication
public class MassiveRepricingApplication implements CommandLineRunner {
	private static final Logger log = LoggerFactory.getLogger(MassiveRepricingApplication.class);

	@Autowired
	private ContractProcessingService contractProcessingService;

	public static void main(String[] args) {
		SpringApplication.run(MassiveRepricingApplication.class, args);
	}

	@Override
	public void run(String... args) {
		Instant startTime = Instant.now();
		contractProcessingService.processContracts();

		log.info("Task processing completed in {} seconds", Instant.now().getEpochSecond() - startTime.getEpochSecond());
		System.exit(0);
	}
}
