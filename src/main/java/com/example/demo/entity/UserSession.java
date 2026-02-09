package com.example.demo.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ConversationState state;

    @Column
    private String selectedRole; // TUTOR or STUDENT

    @Column
    private String tempFullName; // Temporary storage during registration

    @Column
    private String tempEmail; // Temporary storage during registration

    @Column
    private String tempPassword; // Temporary storage during registration

    // Booking flow temporary data
    @Column
    private Long tempSubjectId;

    @Column
    private Long tempTutorId;

    @Column
    private Long tempSessionId;

    @Column
    private String tempSessionType; // ONLINE or IN_PERSON

    @Column
    private String tempDateTime;

    @Column
    private String tempNotes;

    @Column(nullable = false)
    private LocalDateTime lastInteractionAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public enum ConversationState {
        INITIAL, // Just started, no interaction yet
        AWAITING_ROLE, // Waiting for user to select TUTOR or STUDENT
        AWAITING_ACTION, // Waiting for REGISTER, LOGIN, or STATUS
        REGISTER_NAME, // Registration: waiting for full name
        REGISTER_EMAIL, // Registration: waiting for email
        REGISTER_PASSWORD, // Registration: waiting for password
        LOGIN_PASSWORD, // Login: waiting for password
        AUTHENTICATED, // User is logged in
        
        // Student booking flow
        SELECTING_SUBJECT, // Selecting subject for tutoring
        SELECTING_TUTOR, // Browsing and selecting tutor
        SELECTING_DATETIME, // Choosing session date/time
        SELECTING_DURATION, // Choosing session duration
        SELECTING_TYPE, // Online or in-person
        CONFIRMING_BOOKING, // Confirming booking details
        
        // Session management
        VIEWING_SESSIONS, // Viewing session list
        CANCELING_SESSION, // Canceling a session
        RESCHEDULING_SESSION, // Rescheduling a session
        
        // Rating flow
        RATING_SESSION, // Rating a completed session
        WRITING_REVIEW, // Writing review text
        
        // Tutor flows
        MANAGING_SESSIONS, // Managing tutor sessions
        UPDATING_AVAILABILITY, // Updating availability
        SETTING_SUBJECTS, // Setting subjects and rates
        RESPONDING_TO_BOOKING // Accepting/declining booking
    }

    // Constructors
    public UserSession() {
        this.createdAt = LocalDateTime.now();
        this.lastInteractionAt = LocalDateTime.now();
        this.state = ConversationState.INITIAL;
    }

    public UserSession(String phoneNumber) {
        this();
        this.phoneNumber = phoneNumber;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public ConversationState getState() {
        return state;
    }

    public void setState(ConversationState state) {
        this.state = state;
        this.lastInteractionAt = LocalDateTime.now();
    }

    public String getSelectedRole() {
        return selectedRole;
    }

    public void setSelectedRole(String selectedRole) {
        this.selectedRole = selectedRole;
    }

    public String getTempFullName() {
        return tempFullName;
    }

    public void setTempFullName(String tempFullName) {
        this.tempFullName = tempFullName;
    }

    public String getTempEmail() {
        return tempEmail;
    }

    public void setTempEmail(String tempEmail) {
        this.tempEmail = tempEmail;
    }

    public String getTempPassword() {
        return tempPassword;
    }

    public void setTempPassword(String tempPassword) {
        this.tempPassword = tempPassword;
    }

    public LocalDateTime getLastInteractionAt() {
        return lastInteractionAt;
    }

    public void setLastInteractionAt(LocalDateTime lastInteractionAt) {
        this.lastInteractionAt = lastInteractionAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void updateLastInteraction() {
        this.lastInteractionAt = LocalDateTime.now();
    }

    public void clearTempData() {
        this.tempFullName = null;
        this.tempEmail = null;
        this.tempPassword = null;
        this.tempSubjectId = null;
        this.tempTutorId = null;
        this.tempSessionId = null;
        this.tempSessionType = null;
        this.tempDateTime = null;
        this.tempNotes = null;
    }

    // Booking flow getters and setters
    public Long getTempSubjectId() {
        return tempSubjectId;
    }

    public void setTempSubjectId(Long tempSubjectId) {
        this.tempSubjectId = tempSubjectId;
    }

    public Long getTempTutorId() {
        return tempTutorId;
    }

    public void setTempTutorId(Long tempTutorId) {
        this.tempTutorId = tempTutorId;
    }

    public Long getTempSessionId() {
        return tempSessionId;
    }

    public void setTempSessionId(Long tempSessionId) {
        this.tempSessionId = tempSessionId;
    }

    public String getTempSessionType() {
        return tempSessionType;
    }

    public void setTempSessionType(String tempSessionType) {
        this.tempSessionType = tempSessionType;
    }

    public String getTempDateTime() {
        return tempDateTime;
    }

    public void setTempDateTime(String tempDateTime) {
        this.tempDateTime = tempDateTime;
    }

    public String getTempNotes() {
        return tempNotes;
    }

    public void setTempNotes(String tempNotes) {
        this.tempNotes = tempNotes;
    }
}
