package com.example.core.controller;

import com.example.core.domain.Result;

public class BaseController {

    //对于sql增删改操作成功与否的判断 返回相对应的结果对象
    public Result<?> responseByService(int rows) {
        return rows > 0 ? Result.ok() : Result.fail();
    }

    public Result<?> responseByService(boolean result) {
        return result ? Result.ok() : Result.fail();
    }
}
