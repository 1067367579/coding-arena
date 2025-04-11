package com.example.core.controller;

import com.example.core.domain.PageQueryDTO;
import com.example.core.domain.PageResult;
import com.example.core.domain.Result;
import com.github.pagehelper.PageInfo;

import java.util.List;

public class BaseController {

    //对于sql增删改操作成功与否的判断 返回相对应的结果对象
    public Result<?> responseByService(int rows) {
        return rows > 0 ? Result.ok() : Result.fail();
    }

    public Result<?> responseByService(boolean result) {
        return result ? Result.ok() : Result.fail();
    }

    public PageResult getPageResult(List<?> rows) {
        long total = new PageInfo<>(rows).getTotal();
        return PageResult.success(rows,total);
    }
    
    public void processPageArgs(PageQueryDTO pageQueryDTO) {
        if(pageQueryDTO.getPageNum() == null) {
            pageQueryDTO.setPageNum(1);
        }
        if(pageQueryDTO.getPageSize() == null) {
            pageQueryDTO.setPageSize(10);
        }
    }
}
