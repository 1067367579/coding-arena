package com.example.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResultCode {

    //操作成功
    SUCCESS(1000,"操作成功"),
    //服务器内部错误，友好提⽰
    ERROR (2000, "服务繁忙请稍后重试"),
    //操作失败，但是服务器不存在异常
    FAILED (3000, "操作失败"),
    FAILED_UNAUTHORIZED (3001, "未授权"),
    FAILED_PARAMS_VALIDATE (3002, "参数校验失败"),
    FAILED_NOT_EXISTS (3003, "资源不存在"),
    FAILED_ALREADY_EXISTS (3004, "资源已存在"),
    FAILED_USER_EXISTS(3101, "⽤户已存在"),
    FAILED_USER_NOT_EXISTS (3102, "⽤户不存在"),
    FAILED_LOGIN (3103, "账号或密码错误"),
    FAILED_USER_BANNED (3104, "您已被列⼊⿊名单, 请联系管理员"),
    FAILED_EXAM_TIME (3201,"竞赛时间设置错误"),
    FAILED_EXAM_NOT_EXISTS(3202,"竞赛不存在"),
    FAILED_QUESTION_NOT_EXISTS(3203,"题目不存在"),
    FAILED_START_TIME_PASSED(3204,"竞赛已开始"),
    FAILED_EXAM_HAS_NO_QUESTION(3205,"竞赛中没有题目"),
    FAILED_CODE_FREQUENT(3301,"获取验证码太过频繁，稍后重试"),
    FAILED_CODE_INVALID(3302,"验证码失效"),
    FAILED_CODE_WRONG(3303,"验证码错误"),
    FAILED_END_TIME_PASSED(3204,"竞赛已结束");

    //状态码
    private final int code;
    //对应的错误信息
    private final String msg;


}
