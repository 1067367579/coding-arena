package com.example.job.handler;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.core.constants.CacheConstants;
import com.example.common.core.constants.Constants;
import com.example.common.core.enums.ExamListType;
import com.example.common.redis.service.RedisService;
import com.example.job.domain.dto.ExamQueryDTO;
import com.example.job.domain.entity.Exam;
import com.example.job.domain.entity.Message;
import com.example.job.domain.entity.MessageText;
import com.example.job.domain.entity.UserScore;
import com.example.job.domain.vo.ExamQueryVO;
import com.example.job.domain.vo.MessageTextVO;
import com.example.job.mapper.ExamMapper;
import com.example.job.mapper.UserSubmitMapper;
import com.example.job.service.MessageServiceImpl;
import com.example.job.service.MessageTextServiceImpl;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class XxlJobHandler {

    @Autowired
    RedisService redisService;

    @Autowired
    ExamMapper examMapper;

    @Autowired
    UserSubmitMapper userSubmitMapper;

    @Autowired
    MessageServiceImpl messageService;

    @Autowired
    MessageTextServiceImpl messageTextService;

    @XxlJob("examListOrganizeHandler")
    public void examListOrganizeHandler(){
        log.info("执行定时刷新缓存操作～");
        //从数据库中查未完赛和历史竞赛的数据 刷新缓存
        refreshCache(0);
        refreshCache(1);
    }

    @XxlJob("examResultHandler")
    public void examResultHandler(){
        log.info("执行竞赛结果统计操作~");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minusDay = now.minusDays(1);
        //先到竞赛表中查询结束时间在该时间段的竞赛
        List<Exam> exams = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                .select(Exam::getExamId, Exam::getTitle)
                .ge(Exam::getStartTime, minusDay)
                .le(Exam::getEndTime, minusDay)
        );
        List<Long> examIds = exams.stream().map(Exam::getExamId).toList();
        //去submit表根据examIds找到对应的记录 然后分组取出来
        List<UserScore> userScoreList = userSubmitMapper.getUserScoreList(examIds);
        //拿到结果之后进行分组 放到map当中好管理 按照竞赛进行隔离 之后要插入消息
        Map<Long,List<UserScore>> userScoreMap = userScoreList.stream().collect(Collectors.groupingBy(UserScore::getExamId));
        createMessage(exams,userScoreMap);
    }

    public void createMessage(List<Exam> exams,Map<Long,List<UserScore>> userScoreMap){
        //创建消息ID列表 和 消息内容列表
        List<MessageText> messageTextList = new ArrayList<>();
        List<Message> messageList = new ArrayList<>();
        //按照竞赛进行遍历 制造消息
        for (Exam exam : exams) {
            Long examId = exam.getExamId();
            List<UserScore> userScoreList = userScoreMap.get(examId);
            int totalUser = userScoreList.size();
            int examRank = 1;
            //遍历用户分数集合 构造消息
            for (UserScore userScore : userScoreList) {
                String title = exam.getTitle() + "——排名情况";
                String messageContent = "您所参与的竞赛："+exam.getTitle()+",本次" +
                        "参与竞赛一共"+totalUser+"人, 您排名第"+examRank+"名!";
                userScore.setExamRank(examRank);
                //构造响应的MessageText列表插入
                MessageText messageText = new MessageText();
                messageText.setTitle(title);
                messageText.setContent(messageContent);
                messageText.setCreateBy(Constants.SYS_USER);
                messageText.setCreateTime(LocalDateTime.now());
                messageTextList.add(messageText);
                //构造信息对象 设置发送者 接受者 以及text_id
                Message message = new Message();
                message.setSender(Constants.SYS_USER);
                message.setReceiver(userScore.getUserId());
                message.setCreateBy(Constants.SYS_USER);
                message.setCreateTime(LocalDateTime.now());
                messageList.add(message);
                examRank++;
            }
        }
        //构造完消息以及消息文本对象之后 插入 获取到text_id
        messageTextService.saveBatch(messageTextList);
        Map<String,MessageTextVO> messageTextVOMap = new HashMap<>();
        //然后对TextVO进行操作
        for (int i = 0; i < messageTextList.size(); i++) {
            MessageText messageText = messageTextList.get(i);
            //拿出来 赋值给Message
            Message message = messageList.get(i);
            message.setTextId(messageText.getMessageTextId());
            //构造VO
            MessageTextVO messageTextVO = new MessageTextVO();
            BeanUtil.copyProperties(messageText,messageTextVO);
            //存到暂时的map中，接下来存入redis
            messageTextVOMap.put(getMessageDetailKey(messageText.getMessageTextId()),messageTextVO);
        }
        //message正式批量存入
        messageService.saveBatch(messageList);
        //操作redis 按照用户粒度
        Map<Long,List<Message>> userMessageMap = messageList.stream().collect(Collectors.groupingBy(Message::getReceiver));
        userMessageMap.forEach((userId,messages)-> {
            //按照用户逐个插入redis缓存中
            List<Long> textIds = messages.stream().map(Message::getTextId).toList();
            redisService.rightPushAll(getUserMessageListKey(userId),textIds);
        });
        //文本
        redisService.multiSet(messageTextVOMap);
    }

    public String getUserMessageListKey(Long userId){
        return CacheConstants.USER_MESSAGE_KEY_PREFIX+userId;
    }

    public String getMessageDetailKey(Long messageTextId) {
        return CacheConstants.MESSAGE_DETAIL_KEY_PREFIX+messageTextId;
    }

    public void refreshCache(Integer type) {
        //数据库操作
        ExamQueryDTO examQueryDTO = new ExamQueryDTO();
        examQueryDTO.setType(type);
        List<ExamQueryVO> examList = examMapper.getExamList(examQueryDTO);

        //如果数据库中没有数据 直接返回 无需后续操作
        if(CollectionUtils.isEmpty(examList)) {
            return;
        }
        //有数据 创建一个HashMap 批量插入exam examId到list结构中
        Map<String, ExamQueryVO> examMap = new HashMap<>();
        List<Long> examIds = new ArrayList<>();
        //遍历返回数据
        for (ExamQueryVO vo : examList) {
            examMap.put(getDetailKey(vo.getExamId()),vo);
            examIds.add(vo.getExamId());
        }
        //批量插入
        redisService.multiSet(examMap);
        //先要对list进行删除操作
        redisService.deleteObject(getExamListKey(type));
        //插入list 尾插法
        redisService.rightPushAll(getExamListKey(type),examIds);
    }

    public String getExamListKey(Integer type) {
        //判断是查未完赛 还是 历史竞赛 来进行查询
        if(ExamListType.EXAM_UN_FINISH_LIST.getValue().equals(type)) {
            return CacheConstants.EXAM_UNFINISHED_LIST_KEY;
        } else if(ExamListType.EXAM_HISTORY_LIST.getValue().equals(type)) {
            return CacheConstants.EXAM_HISTORY_LIST_KEY;
        }
        return null;
    }

    //获取redis中的exam数据对应的key
    public String getDetailKey(Long examId) {
        return CacheConstants.EXAM_DETAIL_KEY_PREFIX+examId;
    }
}
