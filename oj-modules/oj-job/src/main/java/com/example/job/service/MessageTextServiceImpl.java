package com.example.job.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.job.domain.entity.MessageText;
import com.example.job.mapper.MessageTextMapper;
import org.springframework.stereotype.Service;

@Service
public class MessageTextServiceImpl extends ServiceImpl<MessageTextMapper,MessageText>  {
}
