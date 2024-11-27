package com.hibernate.learning.locking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Runner class to execute locking demonstrations
 * Activated when 'locking-demo' profile is active
 */
@Component
@Profile("locking-demo")
public class LockingDemoRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(LockingDemoRunner.class);
    private final LockingDemo lockingDemo;

    public LockingDemoRunner(LockingDemo lockingDemo) {
        this.lockingDemo = lockingDemo;
    }

    @Override
    public void run(String... args) {
        logger.info("Starting Locking Demonstration Runner");

        try {
            lockingDemo.runAllDemos();
        } catch (Exception e) {
            logger.error("Error running locking demonstrations", e);
        }

        logger.info("Completed Locking Demonstration Runner");
    }
}
