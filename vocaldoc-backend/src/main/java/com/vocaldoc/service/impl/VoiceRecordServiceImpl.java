package com.vocaldoc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vocaldoc.common.enums.VoiceRecordType;
import com.vocaldoc.entity.VoiceRecord;
import com.vocaldoc.mapper.VoiceRecordMapper;
import com.vocaldoc.service.VoiceRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class VoiceRecordServiceImpl implements VoiceRecordService {

    @Autowired
    private VoiceRecordMapper voiceRecordMapper;

    @Value("${app.upload.path:/uploads/records}")
    private String uploadPath;

    @Override
    public IPage<VoiceRecord> page(Page<VoiceRecord> page, String recordType, Integer status) {
        LambdaQueryWrapper<VoiceRecord> wrapper = new LambdaQueryWrapper<>();
        if (recordType != null && !recordType.isEmpty()) {
            wrapper.eq(VoiceRecord::getRecordType, VoiceRecordType.valueOf(recordType));
        }
        if (status != null) {
            wrapper.eq(VoiceRecord::getStatus, status);
        }
        wrapper.orderByDesc(VoiceRecord::getCreatedAt);
        return voiceRecordMapper.selectPage(page, wrapper);
    }

    @Override
    public VoiceRecord getById(Long id) {
        return voiceRecordMapper.selectById(id);
    }

    @Override
    public VoiceRecord saveRecord(MultipartFile file, String description, String recordType) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : ".wav";
        
        String fileName = UUID.randomUUID().toString() + extension;
        
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        File destFile = new File(uploadDir, fileName);
        file.transferTo(destFile);

        VoiceRecord record = new VoiceRecord();
        record.setUserId(1L);
        record.setFileName(originalFilename);
        record.setFileUrl("/uploads/records/" + fileName);
        record.setFileSize(file.getSize());
        record.setDescription(description);
        record.setRecordType(recordType != null ? VoiceRecordType.valueOf(recordType) : VoiceRecordType.VOWEL);
        record.setStatus(0);
        record.setRecordAt(LocalDateTime.now());
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        
        voiceRecordMapper.insert(record);
        return record;
    }

    @Override
    public void deleteById(Long id) {
        voiceRecordMapper.deleteById(id);
    }

    @Override
    public List<VoiceRecord> getByUserId(Long userId) {
        LambdaQueryWrapper<VoiceRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VoiceRecord::getUserId, userId);
        wrapper.orderByDesc(VoiceRecord::getCreatedAt);
        return voiceRecordMapper.selectList(wrapper);
    }
}
