package com.example.job.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.job.domain.entity.Message;
import com.example.job.mapper.MessageMapper;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> {
}
