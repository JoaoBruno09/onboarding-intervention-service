package com.bank.onboarding.interventionservice.services;

import com.bank.onboarding.commonslib.persistence.services.AccountRefRepoService;
import com.bank.onboarding.commonslib.utils.kafka.CreateAccountEvent;
import com.bank.onboarding.commonslib.utils.kafka.ErrorEvent;
import com.bank.onboarding.commonslib.utils.kafka.EventSeDeserializer;
import com.bank.onboarding.commonslib.utils.mappers.AccountMapper;
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
    private final AccountRefRepoService accountRefRepoService;

    @KafkaListener(topics = "${spring.kafka.consumer.topic-name}",  groupId = "${spring.kafka.consumer.group-id}")
    public void consumeEvent(ConsumerRecord event){
        switch (event.key().toString()) {
            case "CREATE_ACCOUNT" -> {
                CreateAccountEvent createAccountEvent = (CreateAccountEvent) eventSeDeserializer.deserialize(event.value().toString(), CreateAccountEvent.class);
                log.info("Event received for customer number {}", Optional.ofNullable(createAccountEvent.getCustomerRefDTO()).map(CustomerRefDTO::getCustomerNumber).orElse(""));
                interventionService.createInterventionForCreateAccountOperation(createAccountEvent);
            }
            case "UPDATE_ACCOUNT_REF" -> {
                AccountRefDTO accountRefDTO = (AccountRefDTO) eventSeDeserializer.deserialize(event.value().toString(), AccountRefDTO.class);
                log.info("Event received to update Account Ref with number {}", accountRefDTO.getAccountNumber());
                accountRefRepoService.saveAccountRefDB(AccountMapper.INSTANCE.toAccountRef(accountRefDTO));
            }
            default -> {
                ErrorEvent errorEvent = (ErrorEvent) eventSeDeserializer.deserialize(event.value().toString(), ErrorEvent.class);
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
