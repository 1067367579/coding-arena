package com.example.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.system.domain.user.dto.UserQueryDTO;
import com.example.system.domain.user.entity.User;
import com.example.system.domain.user.vo.UserQueryVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    List<UserQueryVO> getUserList(UserQueryDTO userQueryDTO);
}
