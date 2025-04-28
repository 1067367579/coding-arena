package com.example.judge.rabbit;

import com.example.api.domain.dto.JudgeDTO;
import com.example.common.core.constants.RabbitMQConstants;
import com.example.judge.service.JudgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JudgeConsumer {

    @Autowired
    private JudgeService judgeService;

    @RabbitListener(queues = RabbitMQConstants.OJ_WORK_QUEUE)
    public void consume(JudgeDTO judgeDTO) {
        log.info("收到rabbitMQ消息:{}",judgeDTO);
        judgeService.doJudgeJavaCode(judgeDTO);
    }
}
