package com.example.OnlineTestManagement.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "test_answers")
public class TestAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private TestAttempt attempt;

    @Column(nullable = false)
    private Long questionId;

    @Column(nullable = false, columnDefinition = "text")
    private String questionText;

    @Column(nullable = false, columnDefinition = "text")
    private String choiceA;
    @Column(nullable = false, columnDefinition = "text")
    private String choiceB;
    @Column(nullable = false, columnDefinition = "text")
    private String choiceC;
    @Column(nullable = false, columnDefinition = "text")
    private String choiceD;

    @Column(nullable = false)
    private Integer studentAnswerIndex;

    @Column(nullable = false)
    private Integer correctAnswerIndex;

    public Long getId() {
        return id;
    }

    public TestAttempt getAttempt() {
        return attempt;
    }

    public void setAttempt(TestAttempt attempt) {
        this.attempt = attempt;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getChoiceA() {
        return choiceA;
    }

    public void setChoiceA(String choiceA) {
        this.choiceA = choiceA;
    }

    public String getChoiceB() {
        return choiceB;
    }

    public void setChoiceB(String choiceB) {
        this.choiceB = choiceB;
    }

    public String getChoiceC() {
        return choiceC;
    }

    public void setChoiceC(String choiceC) {
        this.choiceC = choiceC;
    }

    public String getChoiceD() {
        return choiceD;
    }

    public void setChoiceD(String choiceD) {
        this.choiceD = choiceD;
    }

    public Integer getStudentAnswerIndex() {
        return studentAnswerIndex;
    }

    public void setStudentAnswerIndex(Integer studentAnswerIndex) {
        this.studentAnswerIndex = studentAnswerIndex;
    }

    public Integer getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public void setCorrectAnswerIndex(Integer correctAnswerIndex) {
        this.correctAnswerIndex = correctAnswerIndex;
    }
}
