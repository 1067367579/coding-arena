package com.example.judge.config;

import cn.hutool.core.io.FileUtil;
import com.example.common.core.constants.JudgeConstants;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DockerSandBoxPool {

    private Long memoryLimit;

    private Long memorySwapLimit;

    private Long cpuLimit;

    private String volumeDir;

    private Integer poolSize;

    private String imageName;

    private String containerNamePrefix;

    private DockerClient dockerClient;

    private BlockingQueue<String> containerQueue;

    private Map<String,String> containerMap;

    public DockerSandBoxPool(DockerClient dockerClient, String imageName, String volumeDir,
                             Long memoryLimit, Long memorySwapLimit, Long cpuLimit,
                             Integer poolSize, String containerNamePrefix) {
        this.dockerClient = dockerClient;
        this.imageName = imageName;
        this.volumeDir = volumeDir;
        this.memoryLimit = memoryLimit;
        this.memorySwapLimit = memorySwapLimit;
        this.cpuLimit = cpuLimit;
        this.poolSize = poolSize;
        this.containerNamePrefix = containerNamePrefix;
        //设置固定大小的阻塞队列 就能实现生产者消费者模型 并且固定大小 池化思想
        this.containerQueue = new ArrayBlockingQueue<>(poolSize);
        this.containerMap = new HashMap<>();
    }

    public String getContainer() {
        try {
            return containerQueue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void returnContainer(String container) {
        containerQueue.add(container);
    }

    public void initDockerPool() {
        //创建一定大小的容器池
        for (int i = 0; i < poolSize; i++) {
            createContainer(containerNamePrefix+"-"+i);
        }
    }

    //初始化docker容器 docker-api 动态操作Docker
    private void createContainer(String containerName) {
        //创建镜像前 也检查一次 看有无镜像
        List<Container> containerList = dockerClient.listContainersCmd().
                withShowAll(true).exec();
        if (!CollectionUtils.isEmpty(containerList)) {
            //docker里面的容器名会加一个 /
            String names = JudgeConstants.JAVA_CONTAINER_PREFIX + containerName;
            for (Container container : containerList) {
                String[] containerNames = container.getNames();
                if(containerNames != null && containerNames.length > 0 &&
                names.equals(containerNames[0])) {
                    if("created".equals(container.getStatus())
                            || "exited".equals(container.getStatus())) {
                        //已经有容器 启动即可 此时处于非启动状态
                        dockerClient.startContainerCmd(container.getId()).exec();
                    }
                    containerQueue.add(container.getId());
                    containerMap.put(container.getId(), containerName);
                    return;
                }
            }
        }
        //拉取Java环境镜像
        pullJavaEnvImage();
        //创建容器 限制资源 控制权限
        HostConfig hostConfig = getHostConfig(containerName);
        //创建容器命令
        CreateContainerCmd containerCmd = dockerClient
                .createContainerCmd(imageName)
                .withName(containerName);
        //创建容器的具体操作
        CreateContainerResponse container = containerCmd.withHostConfig(hostConfig)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true)
                .exec();
        //容器ID是唯一标识 通过容器ID就可以唯一确定一个容器 容器池集合放容器ID
        String containerId = container.getId();
        //启动容器
        dockerClient.startContainerCmd(containerId).exec();
        containerQueue.add(container.getId());
        containerMap.put(container.getId(), containerName);
    }

    private HostConfig getHostConfig(String containerName) {
        HostConfig hostConfig = new HostConfig();
        String userCodeDir = createContainerDir(containerName);
        //设置文件挂载目录
        hostConfig.setBinds(new Bind(userCodeDir,new Volume(volumeDir)));
        //限制内存资源
        hostConfig.withMemory(memoryLimit);
        hostConfig.withMemorySwap(memorySwapLimit);
        hostConfig.withCpuCount(cpuLimit);
        hostConfig.withNetworkMode("none");
        hostConfig.withReadonlyRootfs(true);
        return hostConfig;
    }

    private void pullJavaEnvImage() {
        //先拿到所有的镜像 看是否本地已有
        ListImagesCmd listImagesCmd = dockerClient.listImagesCmd();
        List<Image> images = listImagesCmd.exec();
        for (Image image : images) {
            String[] repoTags = image.getRepoTags();
            if(repoTags != null && repoTags.length > 0 &&
                    imageName.equals(repoTags[0])) {
                return;
            }
        }
        //如果没有镜像 拉取
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(imageName);
        try {
            pullImageCmd.exec(new PullImageResultCallback()).awaitCompletion();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String createContainerDir(String containerName) {
        //一级目录
        String codeDir = System.getProperty("user.dir")+ File.separator+ JudgeConstants.CODE_DIR_POOL;
        if(!FileUtil.exist(codeDir)) {
            FileUtil.mkdir(codeDir);
        }
        //二级目录 某个容器专属的文件目录
        return codeDir+File.separator+containerName;
    }

    public String getCodeDir(String containerId) {
        String containerName = containerMap.get(containerId);
        return System.getProperty("user.dir") + File.separator +
                JudgeConstants.CODE_DIR_POOL + File.separator
                + containerName;
    }
}
