package com.vocaldoc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vocaldoc.common.enums.VoiceRecordType;

import java.time.LocalDateTime;

@TableName("voice_record")
public class VoiceRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private VoiceRecordType recordType;

    private String fileName;

    private String fileUrl;

    private Long fileSize;

    private Integer duration;

    private String description;

    private Integer status;

    private LocalDateTime recordAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private LocalDateTime deletedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public VoiceRecordType getRecordType() {
        return recordType;
    }

    public void setRecordType(VoiceRecordType recordType) {
        this.recordType = recordType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getRecordAt() {
        return recordAt;
    }

    public void setRecordAt(LocalDateTime recordAt) {
        this.recordAt = recordAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Override
    public String toString() {
        return "VoiceRecord{" +
                "id=" + id +
                ", userId=" + userId +
                ", recordType=" + recordType +
                ", fileName='" + fileName + '\'' +
                ", status=" + status +
                ", recordAt=" + recordAt +
                '}';
    }
}
