package com.bank.onboarding.interventionservice.services;

import com.bank.onboarding.commonslib.utils.kafka.models.CreateAccountEvent;
import com.bank.onboarding.commonslib.utils.kafka.models.ErrorEvent;

public interface InterventionService {

    void createInterventionForCreateAccountOperation(CreateAccountEvent createAccountEvent);
    void handleErrorEvent(ErrorEvent errorEvent);
}
