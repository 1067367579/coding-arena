package com.example.api.domain.vo;

import com.example.api.domain.UserExeResult;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
//展示给friend服务的VO对象
public class UserQuestionResultVO {
    //0未通过 1通过 2请先执行（数据库中查不到记录） 3正在执行
    private Integer pass;
    //执行的结果信息
    private String exeMessage;
    //执行用例结果集比对信息
    private List<UserExeResult> userExeResultList;
    //该题所得分数信息
    private Integer score;
}
