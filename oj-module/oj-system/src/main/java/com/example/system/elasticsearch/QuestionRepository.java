package com.example.system.elasticsearch;

import com.example.system.domain.question.entity.Question;
import com.example.system.domain.question.entity.QuestionES;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends ElasticsearchRepository<QuestionES, Long> {
}
