package com.example.system.domain.user.dto;

import com.example.common.core.domain.PageQueryDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserQueryDTO extends PageQueryDTO {
    private Long userId;
    private String nickName;
}
