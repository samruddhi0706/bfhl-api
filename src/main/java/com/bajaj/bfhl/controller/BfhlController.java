package com.bajaj.bfhl.controller;

import com.bajaj.bfhl.dto.BfhlRequest;
import com.bajaj.bfhl.dto.BfhlResponse;
import com.bajaj.bfhl.service.BfhlService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bfhl")
public class BfhlController {

    private static final Logger log = LoggerFactory.getLogger(BfhlController.class);
    private final BfhlService bfhlService;

    public BfhlController(BfhlService bfhlService) {
        this.bfhlService = bfhlService;
    }

    @PostMapping
    public ResponseEntity<BfhlResponse> process(
            @RequestHeader(value = "X-Request-Id", defaultValue = "UNKNOWN") String requestId,
            @Valid @RequestBody BfhlRequest request) {

        log.info("Received POST /bfhl  X-Request-Id={}", requestId);
        BfhlResponse response = bfhlService.process(request, requestId);

        return ResponseEntity.ok()
                .header("X-Request-Id", requestId)
                .body(response);
    }
}
