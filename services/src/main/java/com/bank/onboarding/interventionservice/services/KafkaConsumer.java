package com.bank.onboarding.interventionservice.services;

import com.bank.onboarding.commonslib.utils.kafka.EventSeDeserializer;
import com.bank.onboarding.commonslib.utils.kafka.models.CreateAccountEvent;
import com.bank.onboarding.commonslib.utils.kafka.models.CreateIntervenientEvent;
import com.bank.onboarding.commonslib.utils.kafka.models.ErrorEvent;
import com.bank.onboarding.commonslib.web.dtos.account.AccountRefDTO;
import com.bank.onboarding.commonslib.web.dtos.customer.CustomerRefDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

    private final EventSeDeserializer eventSeDeserializer;
    private final InterventionService interventionService;

    @KafkaListener(topics = "${spring.kafka.consumer.topic-name}",  groupId = "${spring.kafka.consumer.group-id}")
    public void consumeEvent(ConsumerRecord event){
        String eventValue = event.value().toString();
        String eventKey = event.key().toString();
        switch (eventKey) {
            case "CREATE_ACCOUNT" -> {
                CreateAccountEvent createAccountEvent = (CreateAccountEvent) eventSeDeserializer.deserialize(eventValue, CreateAccountEvent.class);
                log.info("Event received for customer number {}", Optional.ofNullable(createAccountEvent.getCustomerRefDTO()).map(CustomerRefDTO::getCustomerNumber).orElse(""));
                interventionService.createInterventionForCreateAccountOperation(createAccountEvent, eventKey);
            }
            case "ADD_INTERVENIENT" -> {
                CreateIntervenientEvent createIntervenientEvent = (CreateIntervenientEvent) eventSeDeserializer.deserialize(eventValue, CreateIntervenientEvent.class);
                log.info("Event received to add intervenient {}", createIntervenientEvent.getCustomerRefDTO().getCustomerNumber());
                interventionService.addCustomerIntervention(createIntervenientEvent, eventKey);
            }
            default -> {
                ErrorEvent errorEvent = (ErrorEvent) eventSeDeserializer.deserialize(eventValue, ErrorEvent.class);
                log.info("Error event {} received for account number {}", errorEvent, Optional.ofNullable(errorEvent.getAccountRefDTO()).map(AccountRefDTO::getAccountNumber).orElse(""));
                interventionService.handleErrorEvent(errorEvent);
            }
        }
    }

    @Bean
    public DefaultErrorHandler errorHandler() {
        return new DefaultErrorHandler(new FixedBackOff(0, 0));
    }
}
