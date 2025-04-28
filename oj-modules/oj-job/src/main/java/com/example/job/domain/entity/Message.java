package com.example.job.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("tb_message")
public class Message extends BaseEntity {
    @TableId(value = "message_id",type = IdType.ASSIGN_ID)
    private Long messageId;
    private Long textId;
    private Long receiver;
    private Long sender;
}
