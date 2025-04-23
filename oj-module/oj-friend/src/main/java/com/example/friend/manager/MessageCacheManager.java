package com.example.friend.manager;

import cn.hutool.core.collection.CollUtil;
import com.example.common.core.constants.CacheConstants;
import com.example.common.core.domain.PageQueryDTO;
import com.example.common.redis.service.RedisService;
import com.example.friend.domain.vo.MessageTextVO;
import com.example.friend.mapper.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MessageCacheManager {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private RedisService redisService;

    public List<MessageTextVO> getVOList(PageQueryDTO pageQueryDTO,Long userId) {
        int start = (pageQueryDTO.getPageNum()-1) * pageQueryDTO.getPageSize();
        int end = start + pageQueryDTO.getPageSize()-1;
        String userMessageListKey = getUserMessageListKey(userId);
        List<Long> textIds = redisService.getCacheListByRange(userMessageListKey, start, end, Long.class);
        //将vo逐个获取出来即可
        List<String> messageDetailKeys = textIds.stream().map(this::getMessageDetailKey).toList();
        List<MessageTextVO> voList = redisService.multiGet(messageDetailKeys,MessageTextVO.class);
        CollUtil.removeNull(voList);
        //有错误 返回 空白列表
        if(CollectionUtils.isEmpty(voList) || textIds.size() != voList.size()){
            return List.of();
        }
        return voList;
    }

    public Long getListSize(Long userId) {
        return redisService.getListSize(getUserMessageListKey(userId));
    }

    public String getUserMessageListKey(Long userId){
        return CacheConstants.USER_MESSAGE_KEY_PREFIX+userId;
    }

    public String getMessageDetailKey(Long messageTextId) {
        return CacheConstants.MESSAGE_DETAIL_KEY_PREFIX+messageTextId;
    }

    public void refreshCache(Long userId) {
        //刷新缓存 全量从mysql中取出 然后刷新
        List<MessageTextVO> messageTextVOS = messageMapper.selectUserMessageList(userId);
        if(CollectionUtils.isEmpty(messageTextVOS)){
            return;
        }
        Map<String,MessageTextVO> textVOMap = new HashMap<>();
        for (MessageTextVO messageTextVO : messageTextVOS) {
            textVOMap.put(getMessageDetailKey(messageTextVO.getMessageTextId()),
                    messageTextVO);
        }
        List<Long> textIds = messageTextVOS.stream().map(MessageTextVO::getMessageTextId).toList();
        redisService.multiSet(textVOMap);
        redisService.deleteObject(getUserMessageListKey(userId));
        redisService.rightPushAll(getUserMessageListKey(userId),textIds);
    }
}
