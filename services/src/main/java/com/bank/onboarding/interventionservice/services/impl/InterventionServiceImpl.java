package com.bank.onboarding.interventionservice.services.impl;

import com.bank.onboarding.commonslib.persistence.exceptions.OnboardingException;
import com.bank.onboarding.commonslib.persistence.models.Intervention;
import com.bank.onboarding.commonslib.persistence.services.AccountRefRepoService;
import com.bank.onboarding.commonslib.persistence.services.CustomerRefRepoService;
import com.bank.onboarding.commonslib.persistence.services.InterventionRepoService;
import com.bank.onboarding.commonslib.utils.OnboardingUtils;
import com.bank.onboarding.commonslib.utils.kafka.models.CreateAccountEvent;
import com.bank.onboarding.commonslib.utils.kafka.models.CreateIntervenientEvent;
import com.bank.onboarding.commonslib.utils.kafka.models.ErrorEvent;
import com.bank.onboarding.commonslib.utils.mappers.AccountMapper;
import com.bank.onboarding.commonslib.utils.mappers.CustomerMapper;
import com.bank.onboarding.commonslib.web.dtos.account.AccountRefDTO;
import com.bank.onboarding.commonslib.web.dtos.account.CreateAccountRequestDTO;
import com.bank.onboarding.commonslib.web.dtos.customer.CreateIntervenientDTO;
import com.bank.onboarding.commonslib.web.dtos.customer.CustomerRefDTO;
import com.bank.onboarding.commonslib.web.dtos.customer.CustomerRequestDTO;
import com.bank.onboarding.interventionservice.services.InterventionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.bank.onboarding.commonslib.persistence.constants.OnboardingConstants.INTERVENTIONS_TYPES;
import static com.bank.onboarding.commonslib.persistence.enums.OperationType.ADD_INTERVENIENT;
import static com.bank.onboarding.commonslib.persistence.enums.OperationType.CREATE_ACCOUNT;
import static com.bank.onboarding.commonslib.persistence.enums.OperationType.DELETE_INTERVENIENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterventionServiceImpl implements InterventionService {

    private final InterventionRepoService interventionRepoService;
    private final AccountRefRepoService accountRefRepoService;
    private final CustomerRefRepoService customerRefRepoService;
    private final OnboardingUtils onboardingUtils;

    @Value("${spring.kafka.producer.customer.topic-name}")
    private String customerTopicName;

    @Value("${spring.kafka.producer.account.topic-name}")
    private String accountTopicName;

    @Value("${spring.kafka.producer.document.topic-name}")
    private String documentTopicName;

    @Value("${spring.kafka.producer.relation.topic-name}")
    private String relationTopicName;

    @Override
    public void createInterventionForCreateAccountOperation(CreateAccountEvent createAccountEvent, String operationType) {
        String interventionType = Optional.ofNullable(createAccountEvent.getCreateAccountRequestDTO())
                .map(CreateAccountRequestDTO::getCustomerIntervenient).map(CustomerRequestDTO::getCustomerInterventionType).orElse("");
        saveIntervention(interventionType, createAccountEvent.getAccountRefDTO(), createAccountEvent.getCustomerRefDTO(), operationType, true);
        accountRefRepoService.saveAccountRefDB(AccountMapper.INSTANCE.toAccountRef(createAccountEvent.getAccountRefDTO()));
    }

    @Override
    public void handleErrorEvent(ErrorEvent errorEvent) {
        if(CREATE_ACCOUNT.equals(errorEvent.getOperationType())){
            String accountId = errorEvent.getAccountRefDTO().getAccountId();
            accountRefRepoService.deleteAccountById(accountId);
            interventionRepoService.findAndDeleteInterventionByAccountId(accountId);
        }
    }

    @Override
    public void addCustomerIntervention(CreateIntervenientEvent createIntervenientEvent, String operationType) {
        String interventionType = Optional.ofNullable(createIntervenientEvent.getCreateIntervenientDTO()).map(CreateIntervenientDTO::getIntervenient)
                .map(CustomerRequestDTO::getCustomerType).orElse("");

        saveIntervention(interventionType, createIntervenientEvent.getAccountRefDTO(),
                createIntervenientEvent.getCustomerRefDTO(), operationType, createIntervenientEvent.isNewCustomer());
    }

    @Override
    public void deleteIntervention(String interventionId) {
        Intervention intervention = interventionRepoService.getInterventionByInterventionId(interventionId);
        interventionRepoService.deleteIntervention(intervention);
        String customerId = intervention.getCustomerId();

        CustomerRefDTO customerRefDTO = CustomerMapper.INSTANCE.toCustomerRefDTO(customerRefRepoService.findCustomerRefByCustomerId(customerId));

        if(getInterventionsSizeForCustomer(customerId) == 0) onboardingUtils.sendErrorEvent(customerTopicName, null, customerRefDTO, DELETE_INTERVENIENT);
    }

    private int getInterventionsSizeForCustomer(String customerId) {
        return interventionRepoService.getAllInterventionsByCustomerId(customerId).size();
    }

    private void saveIntervention(String interventionType, AccountRefDTO accountRefDTO, CustomerRefDTO customerRefDTO, String operationType, boolean isNewCustomer) {
        if(!INTERVENTIONS_TYPES.contains(interventionType)){
            sendEventErrors(accountRefDTO, customerRefDTO, operationType, isNewCustomer);
            throw new OnboardingException("O tipo de intervenção introduzido é inválido");
        }

        interventionRepoService.saveInterventionDB(Intervention.builder()
                .creationTime(LocalDateTime.now())
                .description(onboardingUtils.getInterventionTypeValue(interventionType))
                .lastUpdateTime(LocalDateTime.now())
                .interventionType(interventionType)
                .accountId(accountRefDTO.getAccountId())
                .customerId(customerRefDTO.getCustomerId())
                .build());

    }

    private void sendEventErrors(AccountRefDTO accountRefDTO, CustomerRefDTO customerRefDTO, String operationType, boolean isNewCustomer) {
        if(CREATE_ACCOUNT.name().equals(operationType)){
            onboardingUtils.sendErrorEvent(customerTopicName, accountRefDTO, customerRefDTO, CREATE_ACCOUNT);
            onboardingUtils.sendErrorEvent(accountTopicName, accountRefDTO, customerRefDTO, CREATE_ACCOUNT);
            onboardingUtils.sendErrorEvent(documentTopicName, accountRefDTO, customerRefDTO, CREATE_ACCOUNT);

            String accountId = accountRefDTO.getAccountId();
            accountRefRepoService.deleteAccountById(accountId);
            interventionRepoService.findAndDeleteInterventionByAccountId(accountId);
        }else if (ADD_INTERVENIENT.name().equals(operationType)){
            if(Boolean.TRUE.equals(isNewCustomer)){
                onboardingUtils.sendErrorEvent(accountTopicName, accountRefDTO, customerRefDTO, ADD_INTERVENIENT, true);
                onboardingUtils.sendErrorEvent(documentTopicName, accountRefDTO, customerRefDTO, ADD_INTERVENIENT, true);
                onboardingUtils.sendErrorEvent(relationTopicName, accountRefDTO, customerRefDTO, ADD_INTERVENIENT, true);
            }
            if (getInterventionsSizeForCustomer(customerRefDTO.getCustomerId()) == 0)
                onboardingUtils.sendErrorEvent(customerTopicName, accountRefDTO, customerRefDTO, ADD_INTERVENIENT, isNewCustomer);
        }
    }
}
