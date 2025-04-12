package com.example.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.security.exception.ServiceException;
import com.example.common.security.service.TokenService;
import com.example.core.constants.RedisConstants;
import com.example.core.domain.LoginUser;
import com.example.core.domain.Result;
import com.example.core.enums.ResultCode;
import com.example.system.domain.user.dto.LoginDTO;
import com.example.system.domain.user.dto.SysUserDTO;
import com.example.system.domain.user.entity.SysUser;
import com.example.system.domain.user.vo.LoginUserVO;
import com.example.system.mapper.SysUserMapper;
import com.example.system.service.SysUserService;
import com.example.system.utils.BCryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RefreshScope //nacos实时刷新
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Value("${jwt.secret}")
    private String secret;

    @Autowired
    private TokenService tokenService;

    @Override
    public Result<String> login(LoginDTO loginDTO) {
        //参数合法性校验通过Validation依赖完成
        String userAccount = loginDTO.getUserAccount();
        String password = loginDTO.getPassword();
        //需要获取用户ID 用于redis存储
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .select(SysUser::getUserId,SysUser::getPassword,SysUser::getNickName).eq(SysUser::getUserAccount,userAccount));
        if(user == null){
            return Result.fail(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        if(!BCryptUtils.matchesPassword(password,user.getPassword())){
            return Result.fail(ResultCode.FAILED_LOGIN);
        }
        return Result.ok(tokenService.createToken(user.getUserId(),user.getNickName(),
                RedisConstants.LOGIN_IDENTITY_ADMIN,secret));
    }

    @Override
    public int add(SysUserDTO userDTO) {
        //参数合法性校验 此处使用Validation依赖采用注解方式处理
        //添加之前先要进行验证 数据库中是否已经有该用户名
        if(!StringUtils.hasLength(userDTO.getUserAccount()) ||
            !StringUtils.hasLength(userDTO.getPassword())) {
            //自定义异常
            throw new ServiceException(ResultCode.FAILED_PARAMS_VALIDATE);
        }
        //到数据库中查找
        List<SysUser> userList = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserAccount,userDTO.getUserAccount()));
        if(!CollectionUtils.isEmpty(userList)) {
            //自定义异常
            throw new ServiceException(ResultCode.FAILED_USER_EXISTS);
        }
        //将DTO对象转换为实体
        SysUser sysUser = new SysUser();
        sysUser.setUserAccount(userDTO.getUserAccount());
        sysUser.setPassword(userDTO.getPassword());
        sysUser.setNickName("管理员"+ UUID.randomUUID().toString().substring(0,6));
        return sysUserMapper.insert(sysUser);
    }

    @Override
    public Result<LoginUserVO> info(String token) {
        LoginUserVO loginUserVO = new LoginUserVO();
        try {
            LoginUser loginUser = tokenService.getLoginUser(token, secret);
            String nickName = loginUser.getNickName();
            loginUserVO.setNickName(nickName);
        } catch (Exception e) {
            throw new ServiceException(ResultCode.FAILED);
        }
        if(loginUserVO.getNickName() == null){
            throw new ServiceException(ResultCode.FAILED);
        }
        return Result.ok(loginUserVO);
    }

    @Override
    public boolean logout(String token) {
        boolean res;
        try {
             res = tokenService.deleteLoginUser(token,secret);
        } catch (Exception e) {
            throw new ServiceException(ResultCode.FAILED);
        }
        return res;
    }
}
