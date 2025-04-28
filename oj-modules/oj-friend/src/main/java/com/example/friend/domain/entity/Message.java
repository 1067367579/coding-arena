package com.example.friend.domain.entity;

import com.example.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message extends BaseEntity {
    private Long messageId;
    private Long textId;
    private Long receiver;
    private Long sender;
}
