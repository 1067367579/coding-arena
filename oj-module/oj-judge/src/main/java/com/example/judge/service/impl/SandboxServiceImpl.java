package com.example.judge.service.impl;

import com.example.judge.domain.SandboxExecuteResult;
import com.example.judge.service.SandboxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SandboxServiceImpl implements SandboxService {

    @Override
    public SandboxExecuteResult exeJavaCode(String userCode, List<String> inputList) {

        return null;
    }
}
