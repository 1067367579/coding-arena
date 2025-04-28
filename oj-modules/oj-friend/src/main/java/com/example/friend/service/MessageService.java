package com.example.friend.service;

import com.example.common.core.domain.PageQueryDTO;
import com.example.common.core.domain.PageResult;

public interface MessageService {
    PageResult getMessages(PageQueryDTO pageQueryDTO);

}
