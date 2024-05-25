package com.bank.onboarding.interventionservice.services;

import com.bank.onboarding.commonslib.persistence.exceptions.OnboardingException;
import com.bank.onboarding.commonslib.persistence.models.Intervention;
import com.bank.onboarding.commonslib.persistence.services.AccountRefRepoService;
import com.bank.onboarding.commonslib.persistence.services.InterventionRepoService;
import com.bank.onboarding.commonslib.utils.OnboardingUtils;
import com.bank.onboarding.commonslib.utils.kafka.CreateAccountEvent;
import com.bank.onboarding.commonslib.utils.mappers.AccountMapper;
import com.bank.onboarding.commonslib.web.dtos.account.CreateAccountRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.bank.onboarding.commonslib.persistence.constants.OnboardingConstants.INTERVENTIONS_TYPES;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterventionServiceImpl implements InterventionService{

    private final InterventionRepoService interventionRepoService;
    private final AccountRefRepoService accountRefRepoService;
    private final OnboardingUtils onboardingUtils;

    @Override
    public void createInterventionForCreateAccountOperation(CreateAccountEvent createAccountEvent) {
        String interventionType = Optional.ofNullable(createAccountEvent.getCreateAccountRequestDTO()).map(CreateAccountRequestDTO::getCustomerInterventionType).orElse("");
        if(!INTERVENTIONS_TYPES.contains(interventionType))
            //TODO -> Enviar evento para eliminar conta e retirar a conta do cliente
            throw new OnboardingException("O tipo de intervenção introduzido é inválido");

        interventionRepoService.saveInterventionDB(Intervention.builder()
                .creationTime(LocalDateTime.now())
                .description(onboardingUtils.getInterventionTypeValue(interventionType))
                .lastUpdateTime(LocalDateTime.now())
                .interventionType(interventionType)
                .accountId(createAccountEvent.getAccountRefDTO().getAccountId())
                .build());

        accountRefRepoService.saveAccountRefDB(AccountMapper.INSTANCE.toAccountRef(createAccountEvent.getAccountRefDTO()));
    }
}
