package com.example.common.file;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.ObjectId;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.example.common.core.constants.CacheConstants;
import com.example.common.core.constants.JwtConstants;
import com.example.common.redis.service.RedisService;
import com.example.common.security.exception.ServiceException;
import com.example.common.core.enums.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import com.example.common.core.utils.ThreadLocalUtil;

@Component
@RefreshScope
@Slf4j
public class OSSService {

    @Autowired
    private OSSClient ossClient;

    @Autowired
    private RedisService redisService;

    @Autowired
    private OSSProperties prop;

    @Value("${file.max-time}")
    private int maxTime;

    public OSSResult uploadFile(MultipartFile file) throws Exception {
        checkUploadCount();
        //获取输入流 前端传过来的MultipartFile输入流
        InputStream inputStream = null;
        try {
            String fileName;
            if (file.getOriginalFilename() != null) {
                fileName = file.getOriginalFilename().toLowerCase();
            } else {
                //获取文件的原名 如果没有 就是a.png
                fileName = "a.png";
            }
            //获取文件的拓展名 .xxx
            String extName = fileName.substring(fileName.lastIndexOf(".") + 1);
            //获取文件的输入流
            inputStream = file.getInputStream();
            //传递输入流和 文件拓展名到upload方法
            return upload(extName, inputStream);
        } catch (Exception e) {
            log.error("OSS upload file error", e);
            throw new ServiceException(ResultCode.FAILED_FILE_UPLOAD);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    //检查上传次数的方法 避免恶意攻击
    private void checkUploadCount() {
        //获取到当前用户的userId 如果没有就直接报错
        Object userIdObject = ThreadLocalUtil.getLocalMap().get(JwtConstants.USER_ID);
        if (userIdObject == null) {
            throw new ServiceException(ResultCode.FAILED_FILE_UPLOAD);
        }
        Long userId = (Long) userIdObject;
        //从缓存中获取出计数值 此处是hash结构
        Long times =
                redisService.getCacheMapValue(CacheConstants.USER_UPLOAD_TIMES_KEY,
                        String.valueOf(userId), Long.class);
        //如果次数不符合限制要求 报错
        if (times != null && times >= maxTime) {
            throw new
                    ServiceException(ResultCode.FAILED_FILE_UPLOAD_TIME_LIMIT);
        }
        //增加计数器的值
        redisService.incrementHashValue(CacheConstants.USER_UPLOAD_TIMES_KEY,
                String.valueOf(userId), 1);
        //如果次数为第一次 那么要设置明天的过期时间 之后如果增加 还是会沿用这个值 刷新次数限制
        if (times == null || times == 0) {
            long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(),
                    LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
            redisService.expire(CacheConstants.USER_UPLOAD_TIMES_KEY, seconds,
                    TimeUnit.SECONDS);
        }
    }

    private OSSResult upload(String fileType, InputStream inputStream) {
        //key的模式 /pathPrefix/randomId.xxx 使用ObjectId生成不会重复的随机字符串
        String key = prop.getPathPrefix() + ObjectId.next() + "." + fileType;
        //设置对象的元数据
        ObjectMetadata objectMetadata = new ObjectMetadata();
        //设置访问权限 公共读
        objectMetadata.setObjectAcl(CannedAccessControlList.PublicRead);
        //上传文件请求 上传到指定的bucket，将文件输入流，对象的元数据设置传入
        PutObjectRequest request = new PutObjectRequest(prop.getBucketName(),
                key, inputStream, objectMetadata);
        //获取返回的响应
        PutObjectResult putObjectResult;
        try {
            //发出上传文件请求 等待返回响应
            putObjectResult = ossClient.putObject(request);
        } catch (Exception e) {
            log.error("OSS put object error: {}",
                    ExceptionUtil.stacktraceToOneLineString(e, 500));
            throw new ServiceException(ResultCode.FAILED_FILE_UPLOAD);
        }
        //返回响应成功 组装结果
        return assembleOSSResult(key, putObjectResult);
    }

    //文件的key 与 上传返回结果
    private OSSResult assembleOSSResult(String key, PutObjectResult
            putObjectResult) {
        OSSResult ossResult = new OSSResult();
        if (putObjectResult == null ||
                StrUtil.isBlank(putObjectResult.getRequestId())) {
            //最终结果上传失败 设置返回结果success字段为false
            ossResult.setSuccess(false);
        } else {
            //上传成功 设置返回结果success字段为true
            ossResult.setSuccess(true);
            //设置文件名
            ossResult.setName(getURL(key));
        }
        return ossResult;
    }

    private String getURL(String key) {
        return "https://" + prop.getBucketName() + "." + prop.getEndpoint() + "/" + key;
    }

}
