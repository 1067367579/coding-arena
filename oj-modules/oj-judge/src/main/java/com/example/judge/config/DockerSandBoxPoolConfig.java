package com.example.judge.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DockerSandBoxPoolConfig {

    @Value("${sandbox.docker.host}")
    private String dockerHost;

    @Value("${sandbox.docker.memoryLimit}")
    private Long memoryLimit;

    @Value("${sandbox.docker.memorySwapLimit}")
    private Long memorySwapLimit;

    @Value("${sandbox.docker.cpuLimit}")
    private Long cpuLimit;

    @Value("${sandbox.docker.volumeDir}")
    private String volumeDir;

    @Value("${sandbox.docker.poolSize}")
    private Integer poolSize;

    @Value("${sandbox.docker.imageName}")
    private String imageName;

    @Value("${sandbox.docker.namePrefix}")
    private String containerNamePrefix;

    //创建连接Docker的客户端
    @Bean
    public DockerClient dockerClient() {
        //设置客户端连接的IP地址及端口信息
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build();
        return DockerClientBuilder
                .getInstance(config)
                .withDockerCmdExecFactory(new NettyDockerCmdExecFactory())
                .build();
    }

    //创建容器池 通过Spring初始化回调
    @Bean
    public DockerSandBoxPool dockerSandBoxPool(DockerClient dockerClient) {
        DockerSandBoxPool dockerSandBoxPool = new DockerSandBoxPool(dockerClient, imageName,volumeDir,
                memoryLimit,memorySwapLimit,cpuLimit,poolSize,
                containerNamePrefix);
        dockerSandBoxPool.initDockerPool();
        return dockerSandBoxPool;
    }
}
