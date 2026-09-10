package com.example.OnlineTestManagement.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.OnlineTestManagement.entity.Tag;
import com.example.OnlineTestManagement.repository.TagRepository;

@Configuration
public class DataSeeding {

    @Bean
    CommandLineRunner seedDefaultTags(TagRepository tagRepository) {
        return args -> {
            for (int i = 1; i <= 12; i++) {
                String nameTag = "grade-" + i;
                tagRepository.findByNameIgnoreCase(nameTag).orElseGet(() -> tagRepository.save(new Tag(nameTag, null)));
            }
        };
    }
}
