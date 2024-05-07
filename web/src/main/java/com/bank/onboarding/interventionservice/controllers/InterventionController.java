package com.bank.onboarding.interventionservice.controllers;

import com.bank.onboarding.commonslib.persistence.models.Intervention;
import com.bank.onboarding.commonslib.persistence.services.InterventionService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/intervention")
@AllArgsConstructor
public class InterventionController {

    private final InterventionService interventionService;

    @GetMapping("/test")
    public List<Intervention> getInterventions() {
        return interventionService.getAllInterventions();
    }
}
