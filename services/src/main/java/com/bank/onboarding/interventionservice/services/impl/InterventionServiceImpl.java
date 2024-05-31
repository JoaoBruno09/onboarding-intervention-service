package com.bank.onboarding.interventionservice.services.impl;

import com.bank.onboarding.commonslib.persistence.exceptions.OnboardingException;
import com.bank.onboarding.commonslib.persistence.models.Intervention;
import com.bank.onboarding.commonslib.persistence.services.AccountRefRepoService;
import com.bank.onboarding.commonslib.persistence.services.InterventionRepoService;
import com.bank.onboarding.commonslib.utils.OnboardingUtils;
import com.bank.onboarding.commonslib.utils.kafka.CreateAccountEvent;
import com.bank.onboarding.commonslib.utils.kafka.ErrorEvent;
import com.bank.onboarding.commonslib.utils.mappers.AccountMapper;
import com.bank.onboarding.commonslib.web.dtos.account.AccountRefDTO;
import com.bank.onboarding.commonslib.web.dtos.account.CreateAccountRequestDTO;
import com.bank.onboarding.commonslib.web.dtos.customer.CustomerRefDTO;
import com.bank.onboarding.interventionservice.services.InterventionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.bank.onboarding.commonslib.persistence.constants.OnboardingConstants.INTERVENTIONS_TYPES;
import static com.bank.onboarding.commonslib.persistence.enums.OperationType.CREATE_ACCOUNT;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterventionServiceImpl implements InterventionService {

    private final InterventionRepoService interventionRepoService;
    private final AccountRefRepoService accountRefRepoService;
    private final OnboardingUtils onboardingUtils;

    @Value("${spring.kafka.producer.customer.topic-name}")
    private String customerTopicName;

    @Value("${spring.kafka.producer.account.topic-name}")
    private String accountTopicName;

    @Value("${spring.kafka.producer.document.topic-name}")
    private String documentTopicName;

    @Override
    public void createInterventionForCreateAccountOperation(CreateAccountEvent createAccountEvent) {
        String interventionType = Optional.ofNullable(createAccountEvent.getCreateAccountRequestDTO()).map(CreateAccountRequestDTO::getCustomerInterventionType).orElse("");
        if(!INTERVENTIONS_TYPES.contains(interventionType)){
            sendCreationEventErrors(createAccountEvent.getAccountRefDTO(), createAccountEvent.getCustomerRefDTO());
            throw new OnboardingException("O tipo de intervenção introduzido é inválido");
        }

        interventionRepoService.saveInterventionDB(Intervention.builder()
                .creationTime(LocalDateTime.now())
                .description(onboardingUtils.getInterventionTypeValue(interventionType))
                .lastUpdateTime(LocalDateTime.now())
                .interventionType(interventionType)
                .accountId(createAccountEvent.getAccountRefDTO().getAccountId())
                .customerId(createAccountEvent.getCustomerRefDTO().getCustomerId())
                .build());

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

    private void sendCreationEventErrors(AccountRefDTO accountRefDTO, CustomerRefDTO customerRefDTO) {
        onboardingUtils.sendErrorEvent(customerTopicName, accountRefDTO, customerRefDTO, CREATE_ACCOUNT);
        onboardingUtils.sendErrorEvent(accountTopicName, accountRefDTO, customerRefDTO, CREATE_ACCOUNT);
        onboardingUtils.sendErrorEvent(documentTopicName, accountRefDTO, customerRefDTO, CREATE_ACCOUNT);

        String accountId = accountRefDTO.getAccountId();
        accountRefRepoService.deleteAccountById(accountId);
        interventionRepoService.findAndDeleteInterventionByAccountId(accountId);
    }
}
