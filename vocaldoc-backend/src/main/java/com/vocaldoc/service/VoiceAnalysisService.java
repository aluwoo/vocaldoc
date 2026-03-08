package com.vocaldoc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vocaldoc.entity.VoiceAnalysis;

public interface VoiceAnalysisService extends IService<VoiceAnalysis> {

    void saveAnalysis(Long recordId, VoiceAnalysis analysis);

    VoiceAnalysis getByRecordId(Long recordId);
}
