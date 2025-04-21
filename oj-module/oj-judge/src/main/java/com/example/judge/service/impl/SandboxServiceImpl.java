package com.example.judge.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.example.common.core.constants.Constants;
import com.example.common.core.constants.JudgeConstants;
import com.example.common.core.enums.CodeRunStatus;
import com.example.judge.callback.DockerStartResultCallBack;
import com.example.judge.callback.StatisticsCallback;
import com.example.judge.domain.result.CompileResult;
import com.example.judge.domain.result.SandboxExecuteResult;
import com.example.judge.service.SandboxService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RefreshScope
@Slf4j
public class SandboxServiceImpl implements SandboxService {

    @Value("${sandbox.docker.host}")
    private String dockerHost;

    @Value("${sandbox.docker.memoryLimit}")
    private Long memoryLimit;

    @Value("${sandbox.docker.memorySwapLimit}")
    private Long memorySwapLimit;

    @Value("${sandbox.docker.cpuLimit}")
    private Long cpuLimit;

    private String userCodeDir;

    private String userCodeFileName;

    private String containerId;

    private DockerClient dockerClient;

    @Value("${sandbox.docker.timeLimit}")
    private long timeLimit;

    @Override
    public SandboxExecuteResult exeJavaCode(Long userId, String userCode, List<String> inputList) {
        //写入文件
        createUserCodeFile(userId,userCode);
        //初始化容器
        initDockerSandBox();
        //编译代码
        CompileResult compileResult = compileCodeByDocker();
        if(!compileResult.isCompiled()) {
            deleteContainer();
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
                dockerClient.execStartCmd(cmdId)
                        .exec(resultCallBack)
                        .awaitCompletion(timeLimit, TimeUnit.SECONDS);
                if(CodeRunStatus.FAILED.equals(resultCallBack.getCodeRunStatus())) {
                    return SandboxExecuteResult.fail(CodeRunStatus.NOT_ALL_PASSED);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
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
        deleteContainer();
        deleteUserCodeFile();
        return getSandBoxResult(inputList,outList,maxMemory,maxUseTime);
    }

    private SandboxExecuteResult getSandBoxResult(List<String> inputList, List<String> outList, long maxMemory, long maxUseTime) {
        if(inputList.size() != outList.size()) {
            //如果大小不等 一定有某些用例没有通过
            return SandboxExecuteResult.fail(CodeRunStatus.NOT_ALL_PASSED,outList,maxMemory,maxUseTime);
        }
        return SandboxExecuteResult.success(CodeRunStatus.SUCCEED,outList,maxMemory,maxUseTime);
    }

    private void createUserCodeFile(Long userId, String userCode) {
        String examCodeDir = System.getProperty("user.dir")+ File.separator+ JudgeConstants.EXAM_CODE_DIR;
        if(!FileUtil.exist(examCodeDir)) {
            FileUtil.mkdir(examCodeDir);
        }
        String time = LocalDateTimeUtil.format(LocalDateTime.now(), DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        //拼接用户代码文件格式
        userCodeDir = examCodeDir + File.separator + userId + Constants.UNDERLINE + time;
        userCodeFileName = userCodeDir+File.separator+JudgeConstants.USER_CODE_JAVA_CLASS_NAME;
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
                compileResult.setExeMessage(resultCallBack.getMessage());
            } else {
                compileResult.setCompiled(true);
            }
            return compileResult;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteUserCodeFile() {
        //删除掉文件 整个文件夹直接删除
        FileUtil.del(userCodeDir);
    }

    private void deleteContainer() {
        //执行完成删除容器 释放资源 先停止容器
        dockerClient.stopContainerCmd(containerId).exec();
        //删除容器
        dockerClient.removeContainerCmd(containerId).exec();
        try {
            dockerClient.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    //初始化docker容器 docker-api 动态操作Docker
    private void initDockerSandBox() {
        //初始化连接Docker的客户端
        initClient();
        //拉取Java环境镜像
        pullJavaEnvImage();
        //创建容器 限制资源 控制权限
        HostConfig hostConfig = getHostConfig();
        //创建容器命令
        CreateContainerCmd containerCmd = dockerClient
                .createContainerCmd(JudgeConstants.JAVA_ENV_IMAGE)
                .withName(JudgeConstants.JAVA_CONTAINER_NAME);
        //创建容器的具体操作
        CreateContainerResponse container = containerCmd.withHostConfig(hostConfig)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true)
                .exec();
        containerId = container.getId();
        //启动容器
        dockerClient.startContainerCmd(containerId).exec();
    }

    private HostConfig getHostConfig() {
        HostConfig hostConfig = new HostConfig();
        //设置文件挂载目录
        hostConfig.setBinds(new Bind(userCodeDir,new Volume(JudgeConstants.DOCKER_USER_CODE_DIR)));
        //限制内存资源
        hostConfig.withMemory(memoryLimit);
        hostConfig.withMemorySwap(memorySwapLimit);
        hostConfig.withCpuCount(cpuLimit);
        hostConfig.withNetworkMode("none");
        hostConfig.withReadonlyRootfs(true);
        return hostConfig;
    }

    private void initClient() {
        //设置客户端连接的IP地址及端口信息
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build();
        dockerClient = DockerClientBuilder
                .getInstance(config)
                .withDockerCmdExecFactory(new NettyDockerCmdExecFactory())
                .build();
    }

    private void pullJavaEnvImage() {
        //先拿到所有的镜像 看是否本地已有
        ListImagesCmd listImagesCmd = dockerClient.listImagesCmd();
        List<Image> images = listImagesCmd.exec();
        for (Image image : images) {
            String[] repoTags = image.getRepoTags();
            if(repoTags != null && repoTags.length > 0 &&
            JudgeConstants.JAVA_ENV_IMAGE.equals(repoTags[0])) {
                return;
            }
        }
        //如果没有镜像 拉取
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(JudgeConstants.JAVA_ENV_IMAGE);
        try {
            pullImageCmd.exec(new PullImageResultCallback()).awaitCompletion();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}