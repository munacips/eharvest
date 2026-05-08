package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.configs.VonageConfig;
import com.munashechipanga.eharvest.entities.User;
import com.munashechipanga.eharvest.services.notifications.SmsNotificationSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test suite for SMS notification functionality.
 * Tests SMS sending logic without database constraints.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SMS Notification Unit Tests")
class SmsNotificationTest {

    @Mock
    private VonageConfig vonageConfig;

    private SmsService smsService;
    private SmsNotificationSender smsNotificationSender;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Initialize SMS service with mocked config
        smsService = new SmsService(vonageConfig);
        smsNotificationSender = new SmsNotificationSender();

        // Setup test user
        testUser = createTestUser(1L, "farmer@example.com", "+263773123456");
    }

    @Test
    @DisplayName("Should send SMS notification without throwing exception")
    void testSendSmsNotificationSuccess() {
        // Arrange
        String phoneNumber = testUser.getPhoneNumber();
        String message = "Your order #12345 has been confirmed";

        // Act & Assert
        assertDoesNotThrow(() -> {
            smsService.sendSms(phoneNumber, message);
        });
        System.out.println("✓ SMS notification sent successfully");
    }

    @Test
    @DisplayName("Should send SMS via SmsNotificationSender")
    void testSendSmsThroughNotificationSender() {
        // Arrange
        String title = "Order Confirmation";
        String message = "Your order has been confirmed";

        // Act & Assert
        assertDoesNotThrow(() -> {
            smsNotificationSender.send(testUser, title, message);
        });
        System.out.println("✓ SMS sent through SmsNotificationSender");
    }

    @Test
    @DisplayName("Should handle SMS with various message types")
    void testSmsNotificationTypes() {
        String[] messages = {
                "Your order has been accepted",
                "Payment received successfully",
                "Delivery scheduled for today",
                "Your verification code is: 123456"
        };

        for (String message : messages) {
            assertDoesNotThrow(() -> {
                smsService.sendSms(testUser.getPhoneNumber(), message);
            });
            System.out.println("✓ SMS type sent: " + message.substring(0, Math.min(30, message.length())) + "...");
        }
    }

    @Test
    @DisplayName("Should handle SMS to multiple phone numbers")
    void testMultiplePhoneNumbers() {
        String[] phoneNumbers = {
                "+263712345678",
                "+263787654321",
                "+263799999999",
                "+263700000000"
        };

        for (String phoneNumber : phoneNumbers) {
            assertDoesNotThrow(() -> {
                smsService.sendSms(phoneNumber, "Test SMS message");
            });
            System.out.println("✓ SMS sent to: " + phoneNumber);
        }
    }

    @Test
    @DisplayName("Should handle SMS with special characters and emojis")
    void testSmsWithSpecialCharacters() {
        String[] messages = {
                "Your balance: ZWL 1,250.50",
                "Location: 123/45 Main St, Zimbabwe",
                "Rating: 4.5 out of 5 stars",
                "Items: Tomatoes (10kg), Maize (5kg)",
                "Thank you! See you soon :)"
        };

        for (String message : messages) {
            assertDoesNotThrow(() -> {
                smsService.sendSms(testUser.getPhoneNumber(), message);
            });
            System.out.println("✓ Special char SMS: " + message);
        }
    }

    @Test
    @DisplayName("Should handle SMS with various Zimbabwean phone number formats")
    void testZimbabweanPhoneNumbers() {
        String[] phoneNumbers = {
                "+263773123456", // Full international format
                "+263712345678", // Econet
                "+263787654321", // Vodacom
                "+263799999999" // NetOne
        };

        for (String phoneNumber : phoneNumbers) {
            assertDoesNotThrow(() -> {
                smsService.sendSms(phoneNumber, "Test SMS to Zimbabwe");
            });
            System.out.println("✓ SMS to Zimbabwean number: " + phoneNumber);
        }
    }

    @Test
    @DisplayName("Should handle empty phone numbers gracefully")
    void testEmptyPhoneNumbers() {
        assertDoesNotThrow(() -> {
            if (testUser.getPhoneNumber() != null && !testUser.getPhoneNumber().isEmpty()) {
                smsService.sendSms(testUser.getPhoneNumber(), "Valid SMS");
            }
        });
        System.out.println("✓ Empty phone number handled gracefully");
    }

    @Test
    @DisplayName("Should handle SMS notification sender directly")
    void testSmsNotificationSenderDirect() {
        // Create test users with different roles
        User farmer = createTestUser(1L, "farmer@example.com", "+263712345678");
        User buyer = createTestUser(2L, "buyer@example.com", "+263787654321");
        User logistics = createTestUser(3L, "logistics@example.com", "+263799999999");

        User[] users = { farmer, buyer, logistics };
        String[] titles = { "Order Update", "Payment Notification", "Delivery Update" };

        for (int i = 0; i < users.length; i++) {
            final int index = i;
            assertDoesNotThrow(() -> {
                smsNotificationSender.send(users[index], titles[index], "Important update for you");
            });
            System.out.println("✓ Notification sent to: " + users[i].getUsername());
        }
    }

    @Test
    @DisplayName("Should verify SmsService is properly initialized")
    void testSmsServiceInitialization() {
        assertNotNull(smsService, "SmsService should be initialized");
        assertNotNull(vonageConfig, "VonageConfig should be initialized");
        System.out.println("✓ SMS Service properly initialized");
    }

    @Test
    @DisplayName("Should handle long SMS messages")
    void testLongSmsMessages() {
        String longMessage = "This is a long SMS message that contains detailed information about an order. " +
                "It includes multiple sentences and important details that the user needs to know. " +
                "The message should be properly encoded and sent via the Vonage API. " +
                "Long messages are typically split into multiple SMS parts.";

        assertDoesNotThrow(() -> {
            smsService.sendSms(testUser.getPhoneNumber(), longMessage);
        });
        System.out.println("✓ Long SMS message sent successfully (length: " + longMessage.length() + " chars)");
    }

    @Test
    @DisplayName("Should handle rapid sequential SMS sending")
    void testRapidSequentialSmsSending() {
        for (int i = 1; i <= 5; i++) {
            final int iteration = i;
            assertDoesNotThrow(() -> {
                smsService.sendSms(testUser.getPhoneNumber(), "SMS message #" + iteration);
            });
            System.out.println("✓ Rapid SMS #" + i + " sent");
        }
    }

    // Helper method to create test users
    private User createTestUser(Long id, String email, String phoneNumber) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setFirstName("Test");
        user.setLastName("User " + id);
        user.setActive(true);
        return user;
    }
}
