package com.example.OnlineTestManagement.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.OnlineTestManagement.dto.QuestionDTOs;
import com.example.OnlineTestManagement.entity.Tag;
import com.example.OnlineTestManagement.entity.User;
import com.example.OnlineTestManagement.exception.ApiException;
import com.example.OnlineTestManagement.repository.TagRepository;

@Service
public class TagService {
    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<QuestionDTOs.TagResponse> list() {
        return tagRepository.findAllByOrderByNameAsc().stream()
                .map(e -> new QuestionDTOs.TagResponse(e.getId(), e.getName())).toList();
    }

    public QuestionDTOs.TagResponse create(User teacher, QuestionDTOs.TagRequest tagRequest) {
        String name = tagRequest.name().trim();
        if (tagRepository.findByNameIgnoreCase(name).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Tag already exista");
        }
        Tag t = tagRepository.save(new Tag(name, teacher));
        return new QuestionDTOs.TagResponse(t.getId(), t.getName());
    }
}
