package com.example.friend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.friend.domain.entity.Message;
import com.example.friend.domain.vo.MessageTextVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    List<MessageTextVO> selectUserMessageList(Long userId);
}
