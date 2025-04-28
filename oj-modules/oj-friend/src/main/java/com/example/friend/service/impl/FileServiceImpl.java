package com.example.friend.service.impl;

import com.example.common.core.enums.ResultCode;
import com.example.common.file.OSSResult;
import com.example.common.file.OSSService;
import com.example.common.security.exception.ServiceException;
import com.example.friend.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    OSSService ossService;

    @Override
    public OSSResult upload(MultipartFile file) {
        try {
            return ossService.uploadFile(file);
        } catch (ServiceException serviceException) {
            throw serviceException;
        } catch (Exception ex) {
            throw new ServiceException(ResultCode.FAILED_FILE_UPLOAD);
        }
    }

}
