package com.example.friend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.security.exception.ServiceException;
import com.example.common.core.constants.JwtConstants;
import com.example.common.core.domain.PageResult;
import com.example.common.core.enums.ResultCode;
import com.example.common.core.utils.ThreadLocalUtil;
import com.example.friend.domain.dto.ExamQueryDTO;
import com.example.friend.domain.dto.UserExamDTO;
import com.example.friend.domain.entity.Exam;
import com.example.friend.domain.entity.UserExam;
import com.example.friend.manager.ExamCacheManager;
import com.example.friend.mapper.ExamMapper;
import com.example.friend.mapper.UserExamMapper;
import com.example.friend.service.UserExamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class UserExamServiceImpl implements UserExamService {

    @Autowired
    UserExamMapper userExamMapper;
    @Autowired
    ExamMapper examMapper;
    @Autowired
    private ExamCacheManager examCacheManager;

    @Override
    public int enter(UserExamDTO userExamDTO) {
        //用户此时一定处于登录状态 通过了网关验证
        //判断竞赛是否存在
        Exam exam = examMapper.selectById(userExamDTO.getExamId());
        if (exam == null) {
            throw new ServiceException(ResultCode.FAILED_EXAM_NOT_EXISTS);
        }
        //竞赛是否已经开赛 开赛的无法报名
        if(exam.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(ResultCode.FAILED_START_TIME_PASSED);
        }

        //根据令牌获取出用户ID 用户端很多操作都需要获取到用户ID 网关处其实就已经获取出用户ID了
        //每次都要从请求对象中获取出userId太过繁琐
        //Long userId = tokenService.getUserKey(userExamDTO.getToken(), secret);
        Long userId = (Long) ThreadLocalUtil.getLocalMap().get(JwtConstants.USER_ID);

        //不能重复报名
        UserExam userExam = userExamMapper.selectOne(
                new LambdaQueryWrapper<UserExam>()
                .eq(UserExam::getExamId, userExamDTO.getExamId())
                .eq(UserExam::getUserId, userId)
        );
        if (userExam != null) {
            throw new ServiceException(ResultCode.FAILED_ALREADY_ENTER);
        }
        //将用户竞赛报名信息 存到redis中 选择头插法 用户更关心最近报名的竞赛
        examCacheManager.addUserExamCache(userId, userExamDTO.getExamId());
        userExam = new UserExam();
        userExam.setExamId(userExamDTO.getExamId());
        userExam.setUserId(userId);
        //存到数据库中
        return userExamMapper.insert(userExam);
    }

    @Override
    public PageResult list(ExamQueryDTO examQueryDTO) {
        //获取当前用户ID ThreadLocal获取
        Long userId =(Long) ThreadLocalUtil.getLocalMap().get(JwtConstants.USER_ID);
        //获取缓存列表的长度 如果长度为0 甚至没有列表 就需要刷新缓存 到数据库中
        Long listSize = examCacheManager.getListSize(examQueryDTO.getType(),userId);
        if(listSize == null || listSize == 0) {
            //没有数据的时候 去数据库里面刷新数据
            examCacheManager.refreshCache(examQueryDTO.getType(),userId);
        }
        return examCacheManager.getExamVOList(examQueryDTO,userId);
    }
}
