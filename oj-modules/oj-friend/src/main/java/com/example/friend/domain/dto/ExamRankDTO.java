package com.example.friend.domain.dto;

import com.example.common.core.domain.PageQueryDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExamRankDTO extends PageQueryDTO {
    private Long examId;
}
