package com.example.core.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageQueryDTO {
    //设置默认值 避免前端不传值导致后端代码逻辑错误或者一次性请求大量数据导致数据库瘫痪
    //页面大小
    private Integer pageSize;
    //当前页码
    private Integer pageNum;
}
