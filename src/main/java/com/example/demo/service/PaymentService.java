package com.example.demo.service;

import com.example.demo.entity.Payment;
import com.example.demo.entity.TutoringSession;
import com.example.demo.entity.User;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    // Commission tiers
    private static final BigDecimal TIER1_THRESHOLD = new BigDecimal("5000");  // R5,000
    private static final BigDecimal TIER2_THRESHOLD = new BigDecimal("15000"); // R15,000
    private static final BigDecimal TIER1_COMMISSION = new BigDecimal("0.20"); // 20%
    private static final BigDecimal TIER2_COMMISSION = new BigDecimal("0.15"); // 15%
    private static final BigDecimal TIER3_COMMISSION = new BigDecimal("0.10"); // 10%

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TwilioService twilioService;

    /**
     * Create payment for a confirmed session
     */
    @Transactional
    public Payment createPayment(TutoringSession session) {
        // Check if payment already exists
        if (paymentRepository.findBySession(session).isPresent()) {
            throw new RuntimeException("Payment already exists for this session");
        }
        
        // Calculate commission based on tutor's earnings
        BigDecimal totalEarnings = calculateTutorEarnings(session.getTutor());
        BigDecimal commissionRate = getCommissionRate(totalEarnings);
        BigDecimal commission = session.getPrice().multiply(commissionRate);
        
        // Create payment
        Payment payment = new Payment(session, session.getStudent(), session.getTutor(), 
                                     session.getPrice(), commission);
        
        // Generate payment link
        String paymentLink = generatePaymentLink(payment);
        payment.setPaymentLink(paymentLink);
        payment.setPaymentReference("PAY-" + System.currentTimeMillis());
        
        payment = paymentRepository.save(payment);
        
        // Send payment link to student
        sendPaymentLinkToStudent(payment);
        
        // Check if tutor reached a new commission tier
        checkAndNotifyCommissionTierChange(session.getTutor(), totalEarnings);
        
        logger.info("Created payment {} for session {}", payment.getId(), session.getId());
        
        return payment;
    }

    /**
     * Mark payment as completed
     */
    @Transactional
    public void completePayment(Long paymentId, String transactionReference) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        if (payment.getStatus() == Payment.PaymentStatus.PAID) {
            throw new RuntimeException("Payment already completed");
        }
        
        payment.setStatus(Payment.PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        payment.setPaymentReference(transactionReference);
        
        // Generate receipt
        String receiptUrl = generateReceipt(payment);
        payment.setReceiptUrl(receiptUrl);
        
        paymentRepository.save(payment);
        
        // Notify student
        sendReceiptToStudent(payment);
        
        // Notify tutor of earnings
        notifyTutorOfEarnings(payment);
        
        logger.info("Payment {} completed", paymentId);
    }

    /**
     * Handle refund for cancelled session
     */
    @Transactional
    public void handleRefund(TutoringSession session) {
        Payment payment = paymentRepository.findBySession(session).orElse(null);
        
        if (payment == null) {
            return; // No payment to refund
        }
        
        if (payment.getStatus() == Payment.PaymentStatus.PAID) {
            payment.setStatus(Payment.PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            
            // Process refund
            String message = String.format(
                    "💰 *Refund Processed*\n\n" +
                    "Your payment of R%.2f has been refunded for the cancelled session.\n\n" +
                    "Session: %s\n" +
                    "Tutor: %s\n\n" +
                    "The refund will reflect in your account within 3-5 business days.",
                    payment.getTotalAmount(),
                    session.getSubject().getName(),
                    session.getTutor().getFullName());
            
            twilioService.sendTextMessage(payment.getStudent().getPhoneNumber(), message);
            
            logger.info("Refund processed for payment {}", payment.getId());
        }
    }

    /**
     * Get tutor's total earnings
     */
    public BigDecimal calculateTutorEarnings(User tutor) {
        BigDecimal earnings = paymentRepository.calculateTotalEarnings(tutor, Payment.PaymentStatus.PAID);
        return earnings != null ? earnings : BigDecimal.ZERO;
    }

    /**
     * Get tutor's earnings for current month
     */
    public BigDecimal calculateMonthlyEarnings(User tutor) {
        LocalDateTime startOfMonth = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0);
        LocalDateTime endOfMonth = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59);
        
        BigDecimal earnings = paymentRepository.calculateEarningsInPeriod(tutor, Payment.PaymentStatus.PAID, 
                                                                          startOfMonth, endOfMonth);
        return earnings != null ? earnings : BigDecimal.ZERO;
    }

    /**
     * Send monthly earnings summary
     */
    public void sendMonthlyEarningsSummary(User tutor) {
        BigDecimal monthlyEarnings = calculateMonthlyEarnings(tutor);
        BigDecimal totalEarnings = calculateTutorEarnings(tutor);
        BigDecimal commissionRate = getCommissionRate(totalEarnings);
        
        List<Payment> monthPayments = paymentRepository.findByTutorAndStatus(tutor, Payment.PaymentStatus.PAID);
        
        String message = String.format(
                "📊 *Monthly Earnings Summary*\n\n" +
                "Month: %s\n" +
                "Earnings: R%.2f\n" +
                "Total Lifetime Earnings: R%.2f\n\n" +
                "Current Commission Rate: %.0f%%\n" +
                "Sessions Completed: %d\n\n" +
                "%s",
                LocalDateTime.now().getMonth().name(),
                monthlyEarnings,
                totalEarnings,
                commissionRate.multiply(new BigDecimal("100")),
                monthPayments.size(),
                getCommissionTierMessage(totalEarnings));
        
        twilioService.sendTextMessage(tutor.getPhoneNumber(), message);
    }

    /**
     * Get commission rate based on earnings tier
     */
    private BigDecimal getCommissionRate(BigDecimal totalEarnings) {
        if (totalEarnings.compareTo(TIER2_THRESHOLD) >= 0) {
            return TIER3_COMMISSION; // 10% for over R15k
        } else if (totalEarnings.compareTo(TIER1_THRESHOLD) >= 0) {
            return TIER2_COMMISSION; // 15% for R5k-R15k
        } else {
            return TIER1_COMMISSION; // 20% for under R5k
        }
    }

    private String getCommissionTierMessage(BigDecimal totalEarnings) {
        if (totalEarnings.compareTo(TIER2_THRESHOLD) >= 0) {
            return "🎉 You're at the highest tier! Keep up the great work!";
        } else if (totalEarnings.compareTo(TIER1_THRESHOLD) >= 0) {
            BigDecimal remaining = TIER2_THRESHOLD.subtract(totalEarnings);
            return String.format("💪 Earn R%.2f more to reach the top tier (10%% commission)!", remaining);
        } else {
            BigDecimal remaining = TIER1_THRESHOLD.subtract(totalEarnings);
            return String.format("💪 Earn R%.2f more to reach Tier 2 (15%% commission)!", remaining);
        }
    }

    private void checkAndNotifyCommissionTierChange(User tutor, BigDecimal previousEarnings) {
        BigDecimal currentEarnings = calculateTutorEarnings(tutor);
        BigDecimal previousRate = getCommissionRate(previousEarnings);
        BigDecimal currentRate = getCommissionRate(currentEarnings);
        
        if (!previousRate.equals(currentRate)) {
            String message = String.format(
                    "🎊 *Commission Tier Upgrade!*\n\n" +
                    "Congratulations! Your commission rate has been reduced to %.0f%%!\n\n" +
                    "Total Earnings: R%.2f\n" +
                    "New Rate: %.0f%%\n\n" +
                    "%s",
                    currentRate.multiply(new BigDecimal("100")),
                    currentEarnings,
                    currentRate.multiply(new BigDecimal("100")),
                    getCommissionTierMessage(currentEarnings));
            
            twilioService.sendTextMessage(tutor.getPhoneNumber(), message);
        }
    }

    private void sendPaymentLinkToStudent(Payment payment) {
        String message = String.format(
                "💳 *Payment Required*\n\n" +
                "Your session has been confirmed! Please complete payment to secure your booking.\n\n" +
                "Amount: R%.2f\n" +
                "Session: %s\n" +
                "Tutor: %s\n" +
                "Date: %s\n\n" +
                "Reference: %s",
                payment.getTotalAmount(),
                payment.getSession().getSubject().getName(),
                payment.getTutor().getFullName(),
                payment.getSession().getSessionDateTime().toLocalDate(),
                payment.getPaymentReference());
        
        twilioService.sendMessageWithLink(payment.getStudent().getPhoneNumber(), 
                                         message, 
                                         payment.getPaymentLink(), 
                                         "Pay Now");
    }

    private void sendReceiptToStudent(Payment payment) {
        String message = String.format(
                "✅ *Payment Confirmed*\n\n" +
                "Thank you for your payment!\n\n" +
                "Amount Paid: R%.2f\n" +
                "Platform Fee: R%.2f (%.0f%%)\n" +
                "Tutor Receives: R%.2f\n\n" +
                "Session: %s\n" +
                "Tutor: %s\n" +
                "Date: %s\n\n" +
                "Reference: %s",
                payment.getTotalAmount(),
                payment.getPlatformCommission(),
                getCommissionRate(calculateTutorEarnings(payment.getTutor())).multiply(new BigDecimal("100")),
                payment.getTutorEarnings(),
                payment.getSession().getSubject().getName(),
                payment.getTutor().getFullName(),
                payment.getSession().getSessionDateTime().toLocalDate(),
                payment.getPaymentReference());
        
        if (payment.getReceiptUrl() != null) {
            twilioService.sendMessageWithLink(payment.getStudent().getPhoneNumber(), 
                                             message, 
                                             payment.getReceiptUrl(), 
                                             "Download Receipt");
        } else {
            twilioService.sendTextMessage(payment.getStudent().getPhoneNumber(), message);
        }
    }

    private void notifyTutorOfEarnings(Payment payment) {
        String message = String.format(
                "💰 *Payment Received*\n\n" +
                "Good news! Payment received for your session.\n\n" +
                "Your Earnings: R%.2f\n" +
                "Platform Fee: R%.2f (%.0f%%)\n\n" +
                "Session: %s\n" +
                "Student: %s\n" +
                "Date: %s\n\n" +
                "Total Earnings: R%.2f",
                payment.getTutorEarnings(),
                payment.getPlatformCommission(),
                getCommissionRate(calculateTutorEarnings(payment.getTutor())).multiply(new BigDecimal("100")),
                payment.getSession().getSubject().getName(),
                payment.getStudent().getFullName(),
                payment.getSession().getSessionDateTime().toLocalDate(),
                calculateTutorEarnings(payment.getTutor()));
        
        twilioService.sendTextMessage(payment.getTutor().getPhoneNumber(), message);
    }

    private String generatePaymentLink(Payment payment) {
        // In production, integrate with payment gateway (PayFast, Stripe, etc.)
        return "https://pay.tutormate.com/pay/" + payment.getPaymentReference();
    }

    private String generateReceipt(Payment payment) {
        // In production, generate PDF receipt and upload to cloud storage
        return "https://receipts.tutormate.com/receipt-" + payment.getId() + ".pdf";
    }
}
