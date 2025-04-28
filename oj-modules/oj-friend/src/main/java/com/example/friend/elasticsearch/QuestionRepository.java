package com.example.friend.elasticsearch;

import com.example.friend.domain.entity.QuestionES;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends ElasticsearchRepository<QuestionES,Long> {

    Page<QuestionES> findByDifficulty(Integer difficulty, Pageable pageable);

    @Query("{\"bool\": " +
            "{\"should\": " +
                "[{ \"match\": { \"title\": \"?0\" } }, " +
                "{ \"match\": { \"content\": \"?1\" } }], " +
            "\"minimum_should_match\": 1, " +
            "\"must\": [{\"term\": {\"difficulty\": \"?2\"}}]}}")
    Page<QuestionES> findByTitleOrContentAndDifficulty(String title, String content, Integer difficulty, Pageable pageable);

    @Query("{\"bool\": " +
            "{\"should\": " +
            "[{ \"match\": { \"title\": \"?0\" } }, " +
            "{ \"match\": { \"content\": \"?1\" } }], " +
            "\"minimum_should_match\": 1}}")
    Page<QuestionES> findByTitleOrContent(String title, String content, Pageable pageable);

}
