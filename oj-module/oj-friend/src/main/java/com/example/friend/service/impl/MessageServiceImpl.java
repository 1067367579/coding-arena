package com.example.friend.service.impl;

import com.example.common.core.constants.JwtConstants;
import com.example.common.core.domain.PageQueryDTO;
import com.example.common.core.domain.PageResult;
import com.example.common.core.utils.ThreadLocalUtil;
import com.example.friend.domain.vo.MessageTextVO;
import com.example.friend.manager.MessageCacheManager;
import com.example.friend.mapper.MessageMapper;
import com.example.friend.rabbit.CacheRefreshProducer;
import com.example.friend.service.MessageService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private MessageCacheManager messageCacheManager;

    @Autowired
    private CacheRefreshProducer cacheRefreshProducer;

    @Override
    public PageResult getMessages(PageQueryDTO pageQueryDTO) {
        Long userId = (Long)ThreadLocalUtil.getLocalMap().get(JwtConstants.USER_ID);
        Long listSize = messageCacheManager.getListSize(userId);
        if(listSize == null || listSize == 0) {
            List<MessageTextVO> messageTextVOS = pageQueryDB(pageQueryDTO, userId);
            return getPageResult(messageTextVOS);
        }
        //有缓存 直接拿
        List<MessageTextVO> voList = messageCacheManager.getVOList(pageQueryDTO,userId);
        if(voList == null || voList.isEmpty()) {
            //数目对不上 缓存不一致的情况 也是先sql查询 然后异步刷新缓存
            List<MessageTextVO> messageTextVOS = pageQueryDB(pageQueryDTO, userId);
            return getPageResult(messageTextVOS);
        }
        return PageResult.success(voList,listSize);
    }

    private List<MessageTextVO> pageQueryDB(PageQueryDTO pageQueryDTO, Long userId) {
        //没有数据的时候 去数据库中分页查询 同时异步刷新缓存
        PageHelper.startPage(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());
        List<MessageTextVO> messageTextVOS = messageMapper.selectUserMessageList(userId);
        //发送消息异步刷新缓存
        cacheRefreshProducer.produceMessageRefresh(userId);
        return messageTextVOS;
    }

    //分页查询之后 统一获取到查询到信息的总个数
    public PageResult getPageResult(List<?> rows) {
        long total = new PageInfo<>(rows).getTotal();
        return PageResult.success(rows,total);
    }
}
