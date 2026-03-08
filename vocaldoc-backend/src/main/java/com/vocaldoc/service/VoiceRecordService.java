package com.vocaldoc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vocaldoc.entity.VoiceRecord;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VoiceRecordService {
    IPage<VoiceRecord> page(com.baomidou.mybatisplus.extension.plugins.pagination.Page<VoiceRecord> page, String recordType, Integer status);
    
    VoiceRecord getById(Long id);
    
    VoiceRecord saveRecord(MultipartFile file, String description, String recordType) throws Exception;
    
    void deleteById(Long id);
    
    List<VoiceRecord> getByUserId(Long userId);
}
