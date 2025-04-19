package com.example.friend.service;

import com.example.common.file.OSSResult;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    OSSResult upload(MultipartFile file);
}
