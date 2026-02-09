package com.example.demo.repository;

import com.example.demo.entity.TutorAvailability;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface TutorAvailabilityRepository extends JpaRepository<TutorAvailability, Long> {
    
    List<TutorAvailability> findByTutor(User tutor);
    
    List<TutorAvailability> findByTutorAndAvailableTrue(User tutor);
    
    List<TutorAvailability> findByTutorAndDayOfWeek(User tutor, DayOfWeek dayOfWeek);
    
    void deleteByTutor(User tutor);
}
