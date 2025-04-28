package com.example.friend.aspect;

import com.example.common.core.constants.JwtConstants;
import com.example.common.core.enums.ResultCode;
import com.example.common.core.enums.UserStatus;
import com.example.common.core.utils.ThreadLocalUtil;
import com.example.common.security.exception.ServiceException;
import com.example.friend.domain.vo.UserVO;
import com.example.friend.manager.UserCacheManager;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class UserStatusCheckAspect {

    @Autowired
    private UserCacheManager userCacheManager;

    @Before("@annotation(com.example.friend.annotation.CheckUserStatus)")
    public void checkUserStatus(JoinPoint joinPoint) {
        Long userId = (Long)ThreadLocalUtil.getLocalMap().get(JwtConstants.USER_ID);
        UserVO userVO = userCacheManager.getUserDetail(userId);
        log.info("检查用户状态: {}", userVO);
        if(userVO == null) {
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        if(!UserStatus.NORMAL.getStatus().equals(userVO.getStatus())) {
            throw new ServiceException(ResultCode.FAILED_USER_STATUS_FROZEN);
        }
    }
}
