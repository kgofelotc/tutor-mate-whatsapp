package com.example.demo.repository;

import com.example.demo.entity.Subject;
import com.example.demo.entity.TutorSubject;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TutorSubjectRepository extends JpaRepository<TutorSubject, Long> {
    
    List<TutorSubject> findByTutor(User tutor);
    
    List<TutorSubject> findByTutorAndActiveTrue(User tutor);
    
    List<TutorSubject> findBySubject(Subject subject);
    
    List<TutorSubject> findBySubjectAndActiveTrue(Subject subject);
    
    Optional<TutorSubject> findByTutorAndSubject(User tutor, Subject subject);
    
    @Query("SELECT ts FROM TutorSubject ts WHERE ts.subject = :subject AND ts.active = true ORDER BY ts.hourlyRate ASC")
    List<TutorSubject> findTutorsForSubjectOrderedByRate(@Param("subject") Subject subject);
}
