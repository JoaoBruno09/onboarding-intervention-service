package com.bank.onboarding.interventionservice.services;

import com.bank.onboarding.commonslib.utils.kafka.CreateAccountEvent;
import com.bank.onboarding.commonslib.utils.kafka.ErrorEvent;

public interface InterventionService {

    void createInterventionForCreateAccountOperation(CreateAccountEvent createAccountEvent);
    void handleErrorEvent(ErrorEvent errorEvent);
}
