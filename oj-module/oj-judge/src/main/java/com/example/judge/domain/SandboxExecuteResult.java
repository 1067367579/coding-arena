package com.example.judge.domain;

import lombok.Data;

import java.util.List;

@Data
public class SandboxExecuteResult {

    private CodeRunStatus runStatus;

    private String exeMessage;

    private List<String> outputList;

    private Long useMemory; //实际消耗的空间

    private Long useTime; //实际消耗的时间

    public static SandboxExecuteResult fail(CodeRunStatus runStatus,String errorMsg) {
        SandboxExecuteResult result = new SandboxExecuteResult();
        result.setRunStatus(runStatus);
        result.setExeMessage(errorMsg);
        return result;
    }

    public static SandboxExecuteResult fail(CodeRunStatus codeRunStatus) {
        SandboxExecuteResult result = new SandboxExecuteResult();
        result.setRunStatus(codeRunStatus);
        return result;
    }

    public static SandboxExecuteResult fail(CodeRunStatus codeRunStatus,List<String> outputList,
                                            Long useMemory,Long useTime) {
        SandboxExecuteResult result = new SandboxExecuteResult();
        result.setRunStatus(codeRunStatus);
        result.setOutputList(outputList);
        result.setUseMemory(useMemory);
        result.setUseTime(useTime);
        return result;
    }

    public static SandboxExecuteResult success(CodeRunStatus codeRunStatus,List<String> outputList,
                                            Long useMemory,Long useTime) {
        return fail(codeRunStatus, outputList, useMemory, useTime);
    }
}
