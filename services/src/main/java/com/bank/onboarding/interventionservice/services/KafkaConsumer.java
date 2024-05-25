package com.bank.onboarding.interventionservice.services;

import com.bank.onboarding.commonslib.utils.kafka.CreateAccountEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {
    @KafkaListener(topics = "${spring.kafka.consumer.customer.topic-name}",  groupId = "${spring.kafka.consumer.group-id}")
    public void consumeEvent(Object event){
        if (event instanceof CreateAccountEvent) {
            log.info("Event received is " + event);
        }
    }
}
