package com.mindskip.xzs.service.impl;

import com.mindskip.xzs.configuration.property.SystemConfig;
import com.mindskip.xzs.service.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileUploadImpl implements FileUpload {
    private final Logger logger = LoggerFactory.getLogger(FileUpload.class);
    private final SystemConfig systemConfig;
    private final ResourceLoader resourceLoader;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.url-prefix}")
    private String urlPrefix;

    @Autowired
    public FileUploadImpl(SystemConfig systemConfig, ResourceLoader resourceLoader) {
        this.systemConfig = systemConfig;
        this.resourceLoader = resourceLoader;
        logger.info("FileUploadImpl initialized with uploadPath: {}", uploadPath);
    }

    @Override
    public String uploadFile(InputStream inputStream, long size, String extName) {
        try {
            logger.info("Starting file upload to path: {}", uploadPath);
            
            // 确保上传目录存在
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                logger.info("Creating upload directory: {}", uploadPath);
                Files.createDirectories(uploadDir);
            }

            // 处理文件扩展名
            if (!StringUtils.hasText(extName)) {
                extName = ".jpg"; // 默认扩展名
            }
            if (!extName.startsWith(".")) {
                extName = "." + extName;
            }

            // 生成唯一的文件名
            String fileName = UUID.randomUUID().toString() + extName;
            Path filePath = uploadDir.resolve(fileName);
            logger.info("Saving file to: {}", filePath);

            // 保存文件
            Files.copy(inputStream, filePath);
            logger.info("File saved successfully");

            // 返回可访问的URL
            String url = urlPrefix + "/" + fileName;
            logger.info("Returning URL: {}", url);
            return url;
        } catch (IOException e) {
            logger.error("Failed to upload file", e);
            return null;
        }
    }
}
