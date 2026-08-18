package com.project.eventreservation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    // In a production system, you would inject an Email/SMS client here

    public void sendPaymentLink(Long userId, Long reservationId,Long eventId){
        // You would query your UserRepository here to get the actual email address
        String mockUserEmail = "user_" + userId + "@example.com";

        // Generate the unique checkout URL
        String checkoutUrl = "http://localhost:8080/checkout?reservation=" + reservationId;

        // Simulate the external network call
        log.info("======================================================");
        log.info("📧 [OUTBOUND EMAIL] To: {}", mockUserEmail);
        log.info("Subject: Action Required: Your seat for Event {} is temporarily secured!", eventId);
        log.info("Body: You have exactly 10 minutes to complete your payment, or your seat will be given to the next person on the waitlist.");
        log.info("Link: {}", checkoutUrl);
        log.info("======================================================");
    }

    public void sendCancellationNotice(Long userId, Long reservationId) {
        log.info("📧 [OUTBOUND EMAIL] To: user_{}@example.com - Reservation {} was cancelled due to timeout or failed payment.", userId, reservationId);
    }
}
