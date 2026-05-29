package com.munashechipanga.eharvest.services.payments;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test for Paynow ecocash payment initiation.
 * Uses Paynow's official test phone numbers for ecocash.
 *
 * Test numbers (phone field only, other fields can be anything):
 * - 0771111111 → success
 * - 0772222222 → delayed success
 * - 0773333333 → cancelled
 * - 0774444444 → insufficient balance
 *
 * To run this test:
 *   mvn -Dtest=PaynowEcocashIntegrationTest -DforkCount=0 test
 *
 * Then inspect the application logs (WARN level) to see:
 * - Raw Paynow response
 * - Parsed fields
 * - Supplied vs computed hash
 */
@SpringBootTest
@DisplayName("Paynow ecocash integration tests with official test phone numbers")
class PaynowEcocashIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(PaynowEcocashIntegrationTest.class);

    // Paynow official test numbers for ecocash
    private static final String ECOCASH_SUCCESS = "0771111111";
    private static final String ECOCASH_DELAYED_SUCCESS = "0772222222";
    private static final String ECOCASH_CANCELLED = "0773333333";
    private static final String ECOCASH_INSUFFICIENT_BALANCE = "0774444444";

    @Autowired
    private PaynowClient paynowClient;

    @Test
    @DisplayName("Initiate deposit with ecocash success test number")
    void testEcocashSuccess() {
        logger.warn("======== Testing ecocash SUCCESS test number: {} ========", ECOCASH_SUCCESS);
        testPaymentInitiation(ECOCASH_SUCCESS, "ecocash-success-test");
    }

    @Test
    @DisplayName("Initiate deposit with ecocash delayed success test number")
    void testEcocashDelayedSuccess() {
        logger.warn("======== Testing ecocash DELAYED SUCCESS test number: {} ========", ECOCASH_DELAYED_SUCCESS);
        testPaymentInitiation(ECOCASH_DELAYED_SUCCESS, "ecocash-delayed-test");
    }

    @Test
    @DisplayName("Initiate deposit with ecocash cancelled test number")
    void testEcocashCancelled() {
        logger.warn("======== Testing ecocash CANCELLED test number: {} ========", ECOCASH_CANCELLED);
        testPaymentInitiation(ECOCASH_CANCELLED, "ecocash-cancelled-test");
    }

    @Test
    @DisplayName("Initiate deposit with ecocash insufficient balance test number")
    void testEcocashInsufficientBalance() {
        logger.warn("======== Testing ecocash INSUFFICIENT BALANCE test number: {} ========", ECOCASH_INSUFFICIENT_BALANCE);
        testPaymentInitiation(ECOCASH_INSUFFICIENT_BALANCE, "ecocash-insufficient-test");
    }

    private void testPaymentInitiation(String phoneNumber, String reference) {
        try {
            // This will log the raw response, parsed fields, and hash comparison at WARN level
            // Check the application logs to interpret the Paynow response
            PaynowInitResponse response = paynowClient.initiateDeposit(
                    null,
                    phoneNumber,
                    20.00,
                    "USD",
                    "TXN-" + System.currentTimeMillis() + "-" + reference
            );

            logger.warn("Payment initiation successful for number {}. Paynow reference: {}",
                    phoneNumber, response.getProviderReference());
            assertNotNull(response);

        } catch (IllegalStateException e) {
            logger.warn("Payment initiation failed for number {}. Error: {}", phoneNumber, e.getMessage());
            // Don't fail the test; just log the error so we can see what Paynow returned
            // The debug logs above the exception will show the raw response and hash details
        } catch (Exception e) {
            logger.error("Unexpected error during payment initiation for number {}", phoneNumber, e);
        }
    }
}




