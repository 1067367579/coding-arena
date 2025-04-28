package com.example.friend.controller;

import com.example.common.core.domain.Result;
import com.example.common.file.OSSResult;
import com.example.friend.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    FileService fileService;

    @PostMapping("/upload")
    public Result<OSSResult> uploadAvatar(@RequestParam("file") MultipartFile file) {
        log.info("上传文件接口");
        return Result.ok(fileService.upload(file));
    }
}
