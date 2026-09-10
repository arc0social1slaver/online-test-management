package com.example.OnlineTestManagement.service;

import java.util.List;
import java.util.Comparator;
import java.util.HashSet;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.OnlineTestManagement.dto.QuestionDTOs;
import com.example.OnlineTestManagement.entity.Question;
import com.example.OnlineTestManagement.entity.Tag;
import com.example.OnlineTestManagement.entity.User;
import com.example.OnlineTestManagement.exception.ApiException;
import com.example.OnlineTestManagement.repository.QuestionRepository;
import com.example.OnlineTestManagement.repository.TagRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final TagRepository tagRepository;

    public QuestionService(QuestionRepository questionRepository, TagRepository tagRepository) {
        this.questionRepository = questionRepository;
        this.tagRepository = tagRepository;
    }

    public List<QuestionDTOs.QuestionResponse> list(User teacher) {
        return questionRepository.findAllByTeacherIdOrderByIdDesc(teacher.getId()).stream().map(this::toResponse)
                .toList();
    }

    @Transactional
    public QuestionDTOs.QuestionResponse create(User teacher, QuestionDTOs.QuestionRequest questionRequest) {
        Question q = new Question();

        q.setTeacher(teacher);
        q.setText(questionRequest.text().trim());
        q.setChoiceA(questionRequest.choiceA().trim());
        q.setChoiceB(questionRequest.choiceB().trim());
        q.setChoiceC(questionRequest.choiceC().trim());
        q.setChoiceD(questionRequest.choiceD().trim());
        q.setCorrectIndex(questionRequest.correctIndex());
        List<Tag> tags = tagRepository.findAllById(questionRequest.tagIds());
        if (tags.size() != new HashSet<>(questionRequest.tagIds()).size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "One or more tags do not exist");
        }
        q.setTags(new HashSet<>(tags));

        q = questionRepository.save(q);
        return toResponse(q);
    }

    @Transactional
    public QuestionDTOs.QuestionResponse update(User teacher, Long id, QuestionDTOs.QuestionRequest questionRequest) {
        Question q = questionRepository.findByIdAndTeacherId(id, teacher.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Question not found"));

        q.setTeacher(teacher);
        q.setText(questionRequest.text().trim());
        q.setChoiceA(questionRequest.choiceA().trim());
        q.setChoiceB(questionRequest.choiceB().trim());
        q.setChoiceC(questionRequest.choiceC().trim());
        q.setChoiceD(questionRequest.choiceD().trim());
        q.setCorrectIndex(questionRequest.correctIndex());
        List<Tag> tags = tagRepository.findAllById(questionRequest.tagIds());
        if (tags.size() != new HashSet<>(questionRequest.tagIds()).size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "One or more tags do not exist");
        }
        q.setTags(new HashSet<>(tags));

        q = questionRepository.save(q);

        return toResponse(q);
    }

    @Transactional
    public void delete(User teacher, Long id) {
        Question q = questionRepository.findByIdAndTeacherId(id, teacher.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Question not found"));
        q.setActive(false);
        questionRepository.save(q);
    }

    public QuestionDTOs.QuestionResponse toResponse(Question q) {
        return new QuestionDTOs.QuestionResponse(q.getId(), q.getText(),
                List.of(q.getChoiceA(), q.getChoiceB(), q.getChoiceC(), q.getChoiceD()), q.getCorrectIndex(),
                q.getTags().stream().map(e -> new QuestionDTOs.TagResponse(e.getId(), e.getName()))
                        .sorted(Comparator.comparing(QuestionDTOs.TagResponse::id)).toList());
    }
}
