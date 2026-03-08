package com.vocaldoc.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.vocaldoc.config.AliyunOssConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class OssUtil {

    private final AliyunOssConfig ossConfig;
    private final OSS ossClient;

    @Autowired
    public OssUtil(AliyunOssConfig ossConfig, OSS ossClient) {
        this.ossConfig = ossConfig;
        this.ossClient = ossClient;
    }

    /**
     * 上传文件
     */
    public String uploadFile(MultipartFile file) {
        return uploadFile(file, "files");
    }

    /**
     * 上传文件到指定目录
     */
    public String uploadFile(MultipartFile file, String dir) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String fileName = generateFileName(extension);

        String objectName = dir + "/" + fileName;

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(getContentType(extension));
            metadata.setContentLength(file.getSize());

            ossClient.putObject(
                    ossConfig.getBucketName(),
                    objectName,
                    file.getInputStream(),
                    metadata
            );

            return getFileUrl(objectName);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 上传字节数组
     */
    public String uploadBytes(byte[] bytes, String fileName) {
        return uploadBytes(bytes, fileName, "files");
    }

    /**
     * 上传字节数组到指定目录
     */
    public String uploadBytes(byte[] bytes, String fileName, String dir) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("数据不能为空");
        }

        String extension = getFileExtension(fileName);
        String newFileName = generateFileName(extension);
        String objectName = dir + "/" + newFileName;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(getContentType(extension));
        metadata.setContentLength(bytes.length);

        ossClient.putObject(
                ossConfig.getBucketName(),
                objectName,
                new java.io.ByteArrayInputStream(bytes),
                metadata
        );

        return getFileUrl(objectName);
    }

    /**
     * 获取文件访问URL
     */
    public String getFileUrl(String objectName) {
        if (StringUtils.isBlank(ossConfig.getBaseUrl())) {
            return "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint() + "/" + objectName;
        }
        return ossConfig.getBaseUrl() + "/" + objectName;
    }

    /**
     * 删除文件
     */
    public void deleteFile(String fileUrl) {
        if (StringUtils.isBlank(fileUrl)) {
            return;
        }

        String objectName = extractObjectName(fileUrl);
        if (StringUtils.isNotBlank(objectName)) {
            ossClient.deleteObject(ossConfig.getBucketName(), objectName);
        }
    }

    /**
     * 从URL中提取objectName
     */
    private String extractObjectName(String fileUrl) {
        if (StringUtils.isBlank(fileUrl)) {
            return null;
        }

        int index = fileUrl.indexOf(ossConfig.getBucketName());
        if (index > 0) {
            return fileUrl.substring(index + ossConfig.getBucketName().length() + 1);
        }

        return fileUrl;
    }

    /**
     * 生成唯一文件名
     */
    private String generateFileName(String extension) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return datePath + "/" + uuid + extension;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return "";
        }
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return fileName.substring(lastIndexOf);
    }

    /**
     * 根据文件扩展名获取ContentType
     */
    private String getContentType(String extension) {
        if (StringUtils.isBlank(extension)) {
            return "application/octet-stream";
        }

        extension = extension.toLowerCase();
        switch (extension) {
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".png":
                return "image/png";
            case ".gif":
                return "image/gif";
            case ".wav":
                return "audio/wav";
            case ".mp3":
                return "audio/mpeg";
            case ".flac":
                return "audio/flac";
            case ".m4a":
                return "audio/mp4";
            case ".pdf":
                return "application/pdf";
            case ".txt":
                return "text/plain";
            case ".json":
                return "application/json";
            case ".xml":
                return "application/xml";
            default:
                return "application/octet-stream";
        }
    }
}
