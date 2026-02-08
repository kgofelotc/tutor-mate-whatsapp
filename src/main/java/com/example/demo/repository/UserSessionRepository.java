package com.example.demo.repository;

import com.example.demo.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    
    Optional<UserSession> findByPhoneNumber(String phoneNumber);
    
    void deleteByPhoneNumber(String phoneNumber);
}
