package com.bank.onboarding.interventionservice.services;

import com.bank.onboarding.commonslib.utils.kafka.CreateAccountEvent;

public interface InterventionService {

    void createInterventionForCreateAccountOperation(CreateAccountEvent createAccountEvent);
}
