package com.example.demo.repository;

import com.example.demo.entity.Payment;
import com.example.demo.entity.TutoringSession;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findBySession(TutoringSession session);
    
    List<Payment> findByStudent(User student);
    
    List<Payment> findByTutor(User tutor);
    
    List<Payment> findByStatus(Payment.PaymentStatus status);
    
    List<Payment> findByStudentAndStatus(User student, Payment.PaymentStatus status);
    
    List<Payment> findByTutorAndStatus(User tutor, Payment.PaymentStatus status);
    
    @Query("SELECT SUM(p.tutorEarnings) FROM Payment p WHERE p.tutor = :tutor AND p.status = :status")
    BigDecimal calculateTotalEarnings(@Param("tutor") User tutor, @Param("status") Payment.PaymentStatus status);
    
    @Query("SELECT SUM(p.tutorEarnings) FROM Payment p WHERE p.tutor = :tutor AND p.status = :status AND p.paidAt BETWEEN :start AND :end")
    BigDecimal calculateEarningsInPeriod(@Param("tutor") User tutor, @Param("status") Payment.PaymentStatus status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
