package com.nttdata.users.adapter.in.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

@RestController
@RequestMapping("/status")
public class StatusController {

    @GetMapping
    @SecurityRequirements
    public StatusResponse getStatus() {
        return new StatusResponse("ms1", "UP");
    }

    public record StatusResponse(String application, String status) {
    }
}
