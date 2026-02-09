package com.example.demo.repository;

import com.example.demo.entity.Rating;
import com.example.demo.entity.TutoringSession;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    
    Optional<Rating> findBySession(TutoringSession session);
    
    List<Rating> findByTutor(User tutor);
    
    List<Rating> findByStudent(User student);
    
    @Query("SELECT AVG(r.stars) FROM Rating r WHERE r.tutor = :tutor")
    Double calculateAverageRating(@Param("tutor") User tutor);
    
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.tutor = :tutor")
    Long countRatingsForTutor(@Param("tutor") User tutor);
}
