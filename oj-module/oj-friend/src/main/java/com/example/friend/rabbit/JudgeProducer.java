package com.example.friend.rabbit;

import com.example.api.domain.dto.JudgeDTO;
import com.example.common.core.constants.RabbitMQConstants;
import com.example.common.core.enums.ResultCode;
import com.example.common.security.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JudgeProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void produceMessage(JudgeDTO judgeDTO) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConstants.OJ_WORK_QUEUE,judgeDTO);
        } catch (Exception e) {
            throw new ServiceException(ResultCode.FAILED_RABBIT_PRODUCER);
        }
    }


}
