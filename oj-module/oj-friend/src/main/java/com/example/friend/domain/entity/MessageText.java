package com.example.friend.domain.entity;

import com.example.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageText extends BaseEntity {
    private Long messageTextId;
    private String title;
    private String content;
}
