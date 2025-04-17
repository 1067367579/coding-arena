package com.example.core.domain;

import com.example.core.enums.ResultCode;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PageResult {
    private long total;
    private List<?> rows;
    private int code;
    private String msg;

    //没有查到数据 返回的状态码和状态信息依然要设置为成功
    public static PageResult empty() {
        PageResult pageResult = new PageResult();
        pageResult.setCode(ResultCode.SUCCESS.getCode());
        pageResult.setMsg(ResultCode.SUCCESS.getMsg());
        pageResult.setTotal(0);
        pageResult.setRows(new ArrayList<>());
        return pageResult;
    }

    public static <T> PageResult success(List<T> rows, long total) {
        PageResult pageResult = new PageResult();
        pageResult.setCode(ResultCode.SUCCESS.getCode());
        pageResult.setMsg(ResultCode.SUCCESS.getMsg());
        pageResult.setTotal(total);
        pageResult.setRows(rows);
        return pageResult;
    }
}
