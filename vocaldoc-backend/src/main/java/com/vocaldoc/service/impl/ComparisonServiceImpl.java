package com.vocaldoc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vocaldoc.entity.VoiceAnalysis;
import com.vocaldoc.entity.VoiceRecord;
import com.vocaldoc.mapper.VoiceAnalysisMapper;
import com.vocaldoc.mapper.VoiceRecordMapper;
import com.vocaldoc.service.ComparisonService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ComparisonServiceImpl implements ComparisonService {

    private final VoiceAnalysisMapper voiceAnalysisMapper;
    private final VoiceRecordMapper voiceRecordMapper;

    public ComparisonServiceImpl(VoiceAnalysisMapper voiceAnalysisMapper,
                                  VoiceRecordMapper voiceRecordMapper) {
        this.voiceAnalysisMapper = voiceAnalysisMapper;
        this.voiceRecordMapper = voiceRecordMapper;
    }

    @Override
    public Map<String, Object> compareTwoRecords(Long recordId1, Long recordId2) {
        VoiceAnalysis a1 = getAnalysisByRecordId(recordId1);
        VoiceAnalysis a2 = getAnalysisByRecordId(recordId2);
        
        if (a1 == null || a2 == null) {
            return Map.of("error", "Analysis not found");
        }
        
        List<Map<String, Object>> comparisons = new ArrayList<>();
        
        compareMetric(comparisons, "平均基频(Hz)", a1.getMeanF0(), a2.getMeanF0(), true);
        compareMetric(comparisons, "F1共振峰", a1.getF1Mean(), a2.getF1Mean(), true);
        compareMetric(comparisons, "F2共振峰", a1.getF2Mean(), a2.getF2Mean(), true);
        compareMetric(comparisons, "平均响度(dB)", a1.getMeanDB(), a2.getMeanDB(), true);
        compareMetric(comparisons, "Jitter(%)", a1.getJitterLocal(), a2.getJitterLocal(), false);
        compareMetric(comparisons, "Shimmer(%)", a1.getShimmerLocal(), a2.getShimmerLocal(), false);
        compareMetric(comparisons, "谐噪比(dB)", a1.getHnr(), a2.getHnr(), true);
        
        int improved = 0, declined = 0, stable = 0;
        for (Map<String, Object> c : comparisons) {
            String status = (String) c.get("status");
            if ("improved".equals(status)) improved++;
            else if ("declined".equals(status)) declined++;
            else stable++;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("recordId1", recordId1);
        result.put("recordId2", recordId2);
        result.put("comparisons", comparisons);
        result.put("improved", improved);
        result.put("declined", declined);
        result.put("stable", stable);
        
        return result;
    }

    @Override
    public Map<String, Object> analyzeTrend(Long userId, String metric) {
        LambdaQueryWrapper<VoiceRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(VoiceRecord::getUserId, userId)
                    .eq(VoiceRecord::getStatus, 1)
                    .orderByAsc(VoiceRecord::getRecordAt);
        List<VoiceRecord> records = voiceRecordMapper.selectList(recordWrapper);
        
        if (records.size() < 2) {
            return Map.of("trend", "insufficient_data", "message", "需要至少2条录音记录");
        }
        
        List<Map<String, Object>> dataPoints = new ArrayList<>();
        for (VoiceRecord record : records) {
            VoiceAnalysis analysis = getAnalysisByRecordId(record.getId());
            if (analysis != null) {
                Map<String, Object> point = new HashMap<>();
                point.put("recordId", record.getId());
                point.put("recordAt", record.getRecordAt());
                point.put("value", getMetricValue(analysis, metric));
                dataPoints.add(point);
            }
        }
        
        if (dataPoints.size() < 2) {
            return Map.of("trend", "insufficient_data");
        }
        
        String trend = calculateTrend(dataPoints, metric);
        
        Map<String, Object> result = new HashMap<>();
        result.put("metric", metric);
        result.put("trend", trend);
        result.put("dataPoints", dataPoints);
        
        return result;
    }

    @Override
    public Map<String, Object> checkProgress(Long userId) {
        LambdaQueryWrapper<VoiceRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(VoiceRecord::getUserId, userId)
                    .eq(VoiceRecord::getStatus, 1)
                    .orderByAsc(VoiceRecord::getRecordAt);
        List<VoiceRecord> records = voiceRecordMapper.selectList(recordWrapper);
        
        if (records.size() < 3) {
            return Map.of("status", "insufficient_data", "message", "需要至少3条录音记录");
        }
        
        List<String> warnings = new ArrayList<>();
        String[] metrics = {"meanF0", "f1Mean", "f2Mean", "hnr", "jitterLocal", "shimmerLocal"};
        
        for (String metric : metrics) {
            Map<String, Object> trendResult = analyzeTrend(userId, metric);
            String trend = (String) trendResult.get("trend");
            
            if ("declining".equals(trend) || "stagnant".equals(trend)) {
                warnings.add("指标" + metric + "需要关注");
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalRecords", records.size());
        result.put("warnings", warnings);
        result.put("status", warnings.isEmpty() ? "good" : "attention_needed");
        
        return result;
    }

    private void compareMetric(List<Map<String, Object>> comparisons, 
                               String name, BigDecimal v1, BigDecimal v2, 
                               boolean higherIsBetter) {
        if (v1 == null || v2 == null) return;
        
        double diff = v2.subtract(v1).doubleValue();
        double percent = v1.doubleValue() != 0 ? diff / v1.doubleValue() * 100 : 0;
        
        String status;
        if (higherIsBetter) {
            if (diff > 0.1) status = "improved";
            else if (diff < -0.1) status = "declined";
            else status = "stable";
        } else {
            if (diff < -0.1) status = "improved";
            else if (diff > 0.1) status = "declined";
            else status = "stable";
        }
        
        Map<String, Object> comp = new HashMap<>();
        comp.put("metric", name);
        comp.put("value1", v1);
        comp.put("value2", v2);
        comp.put("difference", diff);
        comp.put("percentChange", percent);
        comp.put("status", status);
        comparisons.add(comp);
    }

    private VoiceAnalysis getAnalysisByRecordId(Long recordId) {
        LambdaQueryWrapper<VoiceAnalysis> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VoiceAnalysis::getRecordId, recordId)
               .eq(VoiceAnalysis::getStatus, 1);
        return voiceAnalysisMapper.selectOne(wrapper);
    }

    private BigDecimal getMetricValue(VoiceAnalysis analysis, String metric) {
        if ("meanF0".equals(metric)) return analysis.getMeanF0();
        if ("f1Mean".equals(metric)) return analysis.getF1Mean();
        if ("f2Mean".equals(metric)) return analysis.getF2Mean();
        if ("meanDB".equals(metric)) return analysis.getMeanDB();
        if ("jitterLocal".equals(metric)) return analysis.getJitterLocal();
        if ("shimmerLocal".equals(metric)) return analysis.getShimmerLocal();
        if ("hnr".equals(metric)) return analysis.getHnr();
        return null;
    }

    private String calculateTrend(List<Map<String, Object>> dataPoints, String metric) {
        if (dataPoints.size() < 2) return "unknown";
        
        double first = ((Number) dataPoints.get(0).get("value")).doubleValue();
        double last = ((Number) dataPoints.get(dataPoints.size() - 1).get("value")).doubleValue();
        
        if (first == 0) return "unknown";
        
        double change = (last - first) / first * 100;
        
        if (change > 10) return "improving";
        if (change < -10) return "declining";
        return "stable";
    }
}
