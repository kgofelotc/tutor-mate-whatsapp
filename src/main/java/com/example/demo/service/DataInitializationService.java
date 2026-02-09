package com.example.demo.service;

import com.example.demo.entity.Subject;
import com.example.demo.entity.TutorSubject;
import com.example.demo.entity.User;
import com.example.demo.repository.SubjectRepository;
import com.example.demo.repository.TutorSubjectRepository;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Data initialization service to populate the database with sample data
 * Comment out @Component annotation to disable auto-initialization
 */
//@Component
public class DataInitializationService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializationService.class);

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private TutorSubjectRepository tutorSubjectRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) {
        logger.info("Initializing sample data...");

        initializeSubjects();
        // Add more initialization methods as needed

        logger.info("Sample data initialization complete!");
    }

    private void initializeSubjects() {
        if (subjectRepository.count() > 0) {
            logger.info("Subjects already exist, skipping initialization");
            return;
        }

        List<Subject> subjects = Arrays.asList(
                // Mathematics
                createSubject("Algebra", "Basic to advanced algebra concepts", "MATHEMATICS"),
                createSubject("Calculus", "Differential and integral calculus", "MATHEMATICS"),
                createSubject("Statistics", "Probability and statistical analysis", "MATHEMATICS"),
                createSubject("Geometry", "Shapes, angles, and spatial reasoning", "MATHEMATICS"),
                createSubject("Trigonometry", "Sin, cos, tan and their applications", "MATHEMATICS"),

                // Science
                createSubject("Physics", "Mechanics, thermodynamics, and more", "SCIENCE"),
                createSubject("Chemistry", "Organic, inorganic, and physical chemistry", "SCIENCE"),
                createSubject("Biology", "Life sciences and biological systems", "SCIENCE"),

                // Languages
                createSubject("English", "Grammar, literature, and writing", "LANGUAGE"),
                createSubject("Afrikaans", "Language and literature", "LANGUAGE"),
                createSubject("Zulu", "Language basics to advanced", "LANGUAGE"),

                // Computer Science
                createSubject("Programming", "Java, Python, JavaScript and more", "COMPUTER_SCIENCE"),
                createSubject("Web Development", "HTML, CSS, React, Spring Boot", "COMPUTER_SCIENCE"),
                createSubject("Database", "SQL, database design, and optimization", "COMPUTER_SCIENCE"),

                // Business
                createSubject("Accounting", "Financial accounting and bookkeeping", "BUSINESS"),
                createSubject("Economics", "Micro and macro economics", "BUSINESS"),

                // Arts
                createSubject("Music", "Theory, instruments, and composition", "ARTS"),
                createSubject("Art & Design", "Drawing, painting, and digital art", "ARTS")
        );

        subjectRepository.saveAll(subjects);
        logger.info("Created {} subjects", subjects.size());
    }

    private Subject createSubject(String name, String description, String category) {
        Subject subject = new Subject(name, category);
        subject.setDescription(description);
        subject.setActive(true);
        return subject;
    }

    /**
     * Example method to set up a sample tutor with subjects
     * Uncomment and modify as needed
     */
    /*
    private void setupSampleTutor() {
        // Find or create tutor user
        User tutor = userRepository.findByPhoneNumber("+27123456789")
                .orElseGet(() -> {
                    User newTutor = new User("+27123456789", "Jane Smith", User.UserRole.TUTOR, "password123");
                    newTutor.setEmail("jane.smith@example.com");
                    newTutor.setStatus(User.UserStatus.ACTIVE);
                    return userRepository.save(newTutor);
                });

        // Set up subjects this tutor teaches
        Subject math = subjectRepository.findByName("Algebra").orElseThrow();
        Subject physics = subjectRepository.findByName("Physics").orElseThrow();

        TutorSubject tutorMath = new TutorSubject(tutor, math, new BigDecimal("250.00"));
        tutorMath.setQualifications("BSc Mathematics, 5 years experience");
        tutorMath.setYearsOfExperience(5);

        TutorSubject tutorPhysics = new TutorSubject(tutor, physics, new BigDecimal("300.00"));
        tutorPhysics.setQualifications("MSc Physics, 3 years experience");
        tutorPhysics.setYearsOfExperience(3);

        tutorSubjectRepository.saveAll(Arrays.asList(tutorMath, tutorPhysics));

        logger.info("Set up sample tutor: {}", tutor.getFullName());
    }
    */
}
