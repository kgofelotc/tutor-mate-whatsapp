package com.example.demo.repository;

import com.example.demo.entity.TutoringSession;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TutoringSessionRepository extends JpaRepository<TutoringSession, Long> {
    
    List<TutoringSession> findByStudentOrderBySessionDateTimeDesc(User student);
    
    List<TutoringSession> findByTutorOrderBySessionDateTimeDesc(User tutor);
    
    List<TutoringSession> findByStatus(TutoringSession.SessionStatus status);
    
    List<TutoringSession> findByStudentAndStatus(User student, TutoringSession.SessionStatus status);
    
    List<TutoringSession> findByTutorAndStatus(User tutor, TutoringSession.SessionStatus status);
    
    @Query("SELECT s FROM TutoringSession s WHERE s.tutor = :tutor AND s.status = :status AND s.sessionDateTime > :now ORDER BY s.sessionDateTime ASC")
    List<TutoringSession> findUpcomingSessionsForTutor(@Param("tutor") User tutor, @Param("status") TutoringSession.SessionStatus status, @Param("now") LocalDateTime now);
    
    @Query("SELECT s FROM TutoringSession s WHERE s.student = :student AND s.status = :status AND s.sessionDateTime > :now ORDER BY s.sessionDateTime ASC")
    List<TutoringSession> findUpcomingSessionsForStudent(@Param("student") User student, @Param("status") TutoringSession.SessionStatus status, @Param("now") LocalDateTime now);
    
    @Query("SELECT s FROM TutoringSession s WHERE s.sessionDateTime BETWEEN :start AND :end AND s.status IN :statuses")
    List<TutoringSession> findSessionsInDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("statuses") List<TutoringSession.SessionStatus> statuses);
}
