package com.example.common.core.controller;

import com.example.common.core.constants.HttpConstants;
import com.example.common.core.domain.PageQueryDTO;
import com.example.common.core.domain.PageResult;
import com.example.common.core.domain.Result;
import com.github.pagehelper.PageInfo;

import java.util.List;

public class BaseController {

    //对于增删改操作成功与否的判断 返回相对应的结果对象
    public Result<?> responseByService(int rows) {
        return rows > 0 ? Result.ok() : Result.fail();
    }

    //去除前端传过来的令牌前缀
    public String processToken(String token) {
        return token.replaceFirst(HttpConstants.AUTHENTICATION_PREFIX,"");
    }

    //根据service的boolean类型返回值给出不带数据的响应
    public Result<?> responseByService(boolean result) {
        return result ? Result.ok() : Result.fail();
    }

    //分页查询之后 统一获取到查询到信息的总个数
    public PageResult getPageResult(List<?> rows) {
        long total = new PageInfo<>(rows).getTotal();
        return PageResult.success(rows,total);
    }

    //当前端没有传入分页参数时 默认给出的分页参数 避免没有分页参数导致数据库压力过大崩溃
    public void processPageArgs(PageQueryDTO pageQueryDTO) {
        if(pageQueryDTO.getPageNum() == null) {
            pageQueryDTO.setPageNum(1);
        }
        if(pageQueryDTO.getPageSize() == null) {
            pageQueryDTO.setPageSize(10);
        }
        if(pageQueryDTO.getPageNum() <= 0) {
            pageQueryDTO.setPageNum(1);
        }
        if(pageQueryDTO.getPageSize() <= 0) {
            pageQueryDTO.setPageSize(10);
        }
    }
}
