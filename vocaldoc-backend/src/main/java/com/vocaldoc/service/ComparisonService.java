package com.vocaldoc.service;

import com.vocaldoc.entity.VoiceAnalysis;
import java.util.List;
import java.util.Map;

public interface ComparisonService {

    Map<String, Object> compareTwoRecords(Long recordId1, Long recordId2);

    Map<String, Object> analyzeTrend(Long userId, String metric);

    Map<String, Object> checkProgress(Long userId);
}
