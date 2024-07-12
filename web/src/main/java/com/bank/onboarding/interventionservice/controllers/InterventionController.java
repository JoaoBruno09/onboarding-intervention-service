package com.bank.onboarding.interventionservice.controllers;

import com.bank.onboarding.commonslib.persistence.exceptions.OnboardingException;
import com.bank.onboarding.commonslib.utils.OnboardingUtils;
import com.bank.onboarding.interventionservice.services.InterventionService;
import feign.Request;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("intervention")
@AllArgsConstructor
public class InterventionController {

    private final InterventionService interventionService;
    private final OnboardingUtils onboardingUtils;

    @DeleteMapping("/{interventionId}")
    public ResponseEntity<?> deleteIntervention(@PathVariable("interventionId") String interventionId){
        try {
            interventionService.deleteIntervention(interventionId);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch(OnboardingException e ) {
            return onboardingUtils.buildResponseEntity(Request.HttpMethod.DELETE.name(), e.getMessage());
        }
    }

}
