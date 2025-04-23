package com.example.judge.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.example.common.core.constants.Constants;
import com.example.common.core.constants.JudgeConstants;
import com.example.common.core.enums.CodeRunStatus;
import com.example.common.core.enums.ResultCode;
import com.example.judge.callback.DockerStartResultCallBack;
import com.example.judge.callback.StatisticsCallback;
import com.example.judge.config.DockerSandBoxPool;
import com.example.judge.domain.result.CompileResult;
import com.example.judge.domain.result.SandboxExecuteResult;
import com.example.judge.service.SandboxPoolService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.StatsCmd;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SandboxPoolServiceImpl implements SandboxPoolService {

    @Autowired
    private DockerSandBoxPool dockerSandBoxPool;

    @Autowired
    private DockerClient dockerClient;

    @Value("${sandbox.docker.timeLimit}")
    private long timeLimit;

    private String containerId;

    private String userCodeFileName;

    @Override
    public SandboxExecuteResult exeJavaCode(Long userId, String userCode, List<String> inputList) {
        //拿到containerId 获取容器资源
        containerId = dockerSandBoxPool.getContainer();
        //写入文件
        createUserCodeFile(userCode);
        //编译代码
        CompileResult compileResult = compileCodeByDocker();
        if(!compileResult.isCompiled()) {
            //归还容器池
            dockerSandBoxPool.returnContainer(containerId);
            deleteUserCodeFile();
            return SandboxExecuteResult.fail(CodeRunStatus.COMPILE_FAILED,compileResult.getExeMessage());
        }
        //执行代码
        return executeJavaCodeByDocker(inputList);
    }

    private SandboxExecuteResult executeJavaCodeByDocker(List<String> inputList) {
        //记录输出结果
        List<String> outList = new ArrayList<>();
        //最大占用内存
        long maxMemory = 0L;
        //最大运行时间
        long maxUseTime = 0L;
        //执行用户代码
        for (String inputArgs : inputList) {
            String cmdId = createExecCmd(JudgeConstants.DOCKER_JAVA_EXEC_CMD,inputArgs,containerId);
            //执行代码
            StopWatch stopWatch = new StopWatch();
            //执行情况监控
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            StatisticsCallback statisticsCallback = statsCmd.exec(new StatisticsCallback());
            stopWatch.start();
            DockerStartResultCallBack resultCallBack = new DockerStartResultCallBack();
            try {
                boolean completed = dockerClient.execStartCmd(cmdId)
                        .exec(resultCallBack)
                        .awaitCompletion(timeLimit, TimeUnit.SECONDS);
                if(!completed) {
                    return SandboxExecuteResult.fail(CodeRunStatus.UNKNOWN_FAILED,"执行超时");
                }
                if(CodeRunStatus.FAILED.equals(resultCallBack.getCodeRunStatus())) {
                    return SandboxExecuteResult.fail(CodeRunStatus.NOT_ALL_PASSED,CodeRunStatus.NOT_ALL_PASSED.getMsg());
                }
            } catch (InterruptedException e) {
                return SandboxExecuteResult.fail(CodeRunStatus.UNKNOWN_FAILED,"未知错误");
            }
            stopWatch.stop();
            statsCmd.close();
            long userTime = stopWatch.getLastTaskTimeMillis(); //执行耗时
            maxUseTime = Math.max(userTime,maxUseTime);
            Long memory = statisticsCallback.getMaxMemory();
            if(memory != null) {
                maxMemory = Math.max(maxMemory,memory);
            }
            outList.add(resultCallBack.getMessage().trim());
        }
        dockerSandBoxPool.returnContainer(containerId);
        deleteUserCodeFile();
        return getSandBoxResult(inputList,outList,maxMemory,maxUseTime);
    }

    private SandboxExecuteResult getSandBoxResult(List<String> inputList, List<String> outList, long maxMemory, long maxUseTime) {
        if(inputList.size() != outList.size()) {
            //如果大小不等 一定有某些用例没有通过
            return SandboxExecuteResult.fail(CodeRunStatus.NOT_ALL_PASSED,outList,maxMemory,maxUseTime,
                    CodeRunStatus.NOT_ALL_PASSED.getMsg());
        }
        return SandboxExecuteResult.success(CodeRunStatus.SUCCEED,outList,maxMemory,maxUseTime,
                CodeRunStatus.SUCCEED.getMsg());
    }

    private void createUserCodeFile(String userCode) {
        //每个容器都有一个对应的挂载路径
        String codeDir = dockerSandBoxPool.getCodeDir(containerId);
        userCodeFileName = codeDir + File.separator + JudgeConstants.USER_CODE_JAVA_CLASS_NAME;
        if(FileUtil.exist(userCodeFileName)) {
            //确保之前的代码文件已经被清除
            FileUtil.del(userCodeFileName);
        }
        FileUtil.writeString(userCode,userCodeFileName,Constants.UTF8);
    }

    private CompileResult compileCodeByDocker() {
        //编译的时候无需入参
        String cmdId = createExecCmd(JudgeConstants.DOCKER_JAVAC_CMD,null,containerId);
        //设置执行回调
        DockerStartResultCallBack resultCallBack = new DockerStartResultCallBack();
        CompileResult compileResult = new CompileResult();
        try {
            dockerClient.execStartCmd(cmdId)
                    .exec(resultCallBack)
                    .awaitCompletion(); //阻塞等待运行完成
            if(CodeRunStatus.FAILED.equals(resultCallBack.getCodeRunStatus())) {
                compileResult.setCompiled(false);
                compileResult.setExeMessage(resultCallBack.getErrorMessage());
            } else {
                compileResult.setCompiled(true);
            }
        } catch (InterruptedException e) {
            compileResult.setCompiled(false);
            compileResult.setExeMessage(CodeRunStatus.UNKNOWN_FAILED.getMsg());
        }
        return compileResult;
    }

    private void deleteUserCodeFile() {
        //删除掉文件 整个文件夹直接删除
        FileUtil.del(userCodeFileName);
    }

    //Java命令 参数 容器ID
    private String createExecCmd(String[] javaCmdArr, String inputArgs, String containerId) {
        if (!StrUtil.isEmpty(inputArgs)) {
            //当入参不为空时拼接入参
            String[] inputArray = inputArgs.split(" "); //入参
            javaCmdArr = ArrayUtil.append(JudgeConstants.DOCKER_JAVA_EXEC_CMD, inputArray);
        }
        ExecCreateCmdResponse cmdResponse = dockerClient.execCreateCmd(containerId)
                .withCmd(javaCmdArr)
                .withAttachStderr(true)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .exec();
        return cmdResponse.getId();
    }
}
