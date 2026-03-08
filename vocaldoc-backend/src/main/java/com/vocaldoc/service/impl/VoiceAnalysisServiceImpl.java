package com.vocaldoc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vocaldoc.entity.VoiceAnalysis;
import com.vocaldoc.mapper.VoiceAnalysisMapper;
import com.vocaldoc.service.VoiceAnalysisService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VoiceAnalysisServiceImpl extends ServiceImpl<VoiceAnalysisMapper, VoiceAnalysis> implements VoiceAnalysisService {

    @Override
    public void saveAnalysis(Long recordId, VoiceAnalysis analysis) {
        analysis.setRecordId(recordId);
        analysis.setStatus(1);
        analysis.setAnalyzedAt(LocalDateTime.now());
        analysis.setCreatedAt(LocalDateTime.now());
        analysis.setUpdatedAt(LocalDateTime.now());
        
        LambdaQueryWrapper<VoiceAnalysis> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VoiceAnalysis::getRecordId, recordId);
        VoiceAnalysis existing = this.getOne(wrapper);
        
        if (existing != null) {
            analysis.setId(existing.getId());
            this.updateById(analysis);
        } else {
            this.save(analysis);
        }
    }

    @Override
    public VoiceAnalysis getByRecordId(Long recordId) {
        LambdaQueryWrapper<VoiceAnalysis> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VoiceAnalysis::getRecordId, recordId);
        wrapper.eq(VoiceAnalysis::getStatus, 1);
        return this.getOne(wrapper);
    }
}
