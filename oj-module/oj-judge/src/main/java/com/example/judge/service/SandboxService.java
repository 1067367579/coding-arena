package com.example.judge.service;

import com.example.judge.domain.SandboxExecuteResult;

import java.util.List;

public interface SandboxService {
    SandboxExecuteResult exeJavaCode(Long userId, String userCode, List<String> inputList);
}
