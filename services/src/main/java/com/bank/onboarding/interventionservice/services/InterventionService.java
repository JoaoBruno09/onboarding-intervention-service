package com.bank.onboarding.interventionservice.services;

import com.bank.onboarding.commonslib.utils.kafka.models.CreateAccountEvent;
import com.bank.onboarding.commonslib.utils.kafka.models.CreateIntervenientEvent;
import com.bank.onboarding.commonslib.utils.kafka.models.ErrorEvent;

public interface InterventionService {

    void createInterventionForCreateAccountOperation(CreateAccountEvent createAccountEvent, String operationType);
    void handleErrorEvent(ErrorEvent errorEvent);
    void addCustomerIntervention(CreateIntervenientEvent createIntervenientEvent, String operationType);
}
