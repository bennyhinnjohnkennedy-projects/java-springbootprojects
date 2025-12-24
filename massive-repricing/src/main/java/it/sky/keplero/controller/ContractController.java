package it.sky.keplero.controller;

import it.sky.keplero.service.ContractProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.List;

// @RestController
// @RequestMapping("/contracts")
public class ContractController {
    private static final Logger log = LoggerFactory.getLogger(ContractController.class);

    private final ContractProcessingService contractProcessingService;

    public ContractController(ContractProcessingService contractProcessingService) {
        this.contractProcessingService = contractProcessingService;
    }

//    @PostMapping(value = "/process")
    public Flux<List<String>> processContractList() {
//        Flux<List<String>> result = contractProcessingService.processContracts();
//        return result;
        return null;
    }
}
