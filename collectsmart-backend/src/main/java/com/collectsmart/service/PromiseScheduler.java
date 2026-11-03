package com.collectsmart.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PromiseScheduler {

    private final PromiseToPayService promiseService;

    public PromiseScheduler(PromiseToPayService promiseService) {
        this.promiseService = promiseService;
    }

    // Run every day at 7:05 AM
    @Scheduled(cron = "0 5 7 * * *")
    public void checkBrokenPromises() {

        int count = promiseService.markBrokenPromises();

        System.out.println(
                "Broken promises automatically updated: " + count
        );
    }
}