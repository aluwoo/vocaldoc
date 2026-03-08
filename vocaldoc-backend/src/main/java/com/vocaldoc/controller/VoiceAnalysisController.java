package com.vocaldoc.controller;

import com.vocaldoc.common.result.Result;
import com.vocaldoc.entity.VoiceAnalysis;
import com.vocaldoc.service.VoiceAnalysisService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analysis")
public class VoiceAnalysisController {

    private final VoiceAnalysisService voiceAnalysisService;

    public VoiceAnalysisController(VoiceAnalysisService voiceAnalysisService) {
        this.voiceAnalysisService = voiceAnalysisService;
    }

    @PostMapping("/save/{recordId}")
    public Result<Void> saveAnalysis(
            @PathVariable Long recordId,
            @RequestBody Map<String, Object> analysisData) {
        
        VoiceAnalysis analysis = new VoiceAnalysis();
        
        if (analysisData.get("meanF0") != null) {
            analysis.setMeanF0(toBigDecimal(analysisData.get("meanF0")));
        }
        if (analysisData.get("minF0") != null) {
            analysis.setMinF0(toBigDecimal(analysisData.get("minF0")));
        }
        if (analysisData.get("maxF0") != null) {
            analysis.setMaxF0(toBigDecimal(analysisData.get("maxF0")));
        }
        if (analysisData.get("f0Range") != null) {
            analysis.setF0Range(toBigDecimal(analysisData.get("f0Range")));
        }
        if (analysisData.get("f1Mean") != null) {
            analysis.setF1Mean(toBigDecimal(analysisData.get("f1Mean")));
        }
        if (analysisData.get("f2Mean") != null) {
            analysis.setF2Mean(toBigDecimal(analysisData.get("f2Mean")));
        }
        if (analysisData.get("f3Mean") != null) {
            analysis.setF3Mean(toBigDecimal(analysisData.get("f3Mean")));
        }
        if (analysisData.get("f4Mean") != null) {
            analysis.setF4Mean(toBigDecimal(analysisData.get("f4Mean")));
        }
        if (analysisData.get("meanDB") != null) {
            analysis.setMeanDB(toBigDecimal(analysisData.get("meanDB")));
        }
        if (analysisData.get("maxDB") != null) {
            analysis.setMaxDB(toBigDecimal(analysisData.get("maxDB")));
        }
        if (analysisData.get("jitterLocal") != null) {
            analysis.setJitterLocal(toBigDecimal(analysisData.get("jitterLocal")));
        }
        if (analysisData.get("jitterRap") != null) {
            analysis.setJitterRap(toBigDecimal(analysisData.get("jitterRap")));
        }
        if (analysisData.get("jitterPpq5") != null) {
            analysis.setJitterPpq5(toBigDecimal(analysisData.get("jitterPpq5")));
        }
        if (analysisData.get("jitterDdp") != null) {
            analysis.setJitterDdp(toBigDecimal(analysisData.get("jitterDdp")));
        }
        if (analysisData.get("shimmerLocal") != null) {
            analysis.setShimmerLocal(toBigDecimal(analysisData.get("shimmerLocal")));
        }
        if (analysisData.get("shimmerLocalDB") != null) {
            analysis.setShimmerLocalDB(toBigDecimal(analysisData.get("shimmerLocalDB")));
        }
        if (analysisData.get("shimmerApq3") != null) {
            analysis.setShimmerApq3(toBigDecimal(analysisData.get("shimmerApq3")));
        }
        if (analysisData.get("shimmerApq5") != null) {
            analysis.setShimmerApq5(toBigDecimal(analysisData.get("shimmerApq5")));
        }
        if (analysisData.get("hnr") != null) {
            analysis.setHnr(toBigDecimal(analysisData.get("hnr")));
        }
        
        if (analysisData.get("pitchContour") != null) {
            analysis.setRawPitchContour(toJson(analysisData.get("pitchContour")));
        }
        if (analysisData.get("intensityContour") != null) {
            analysis.setRawIntensityContour(toJson(analysisData.get("intensityContour")));
        }
        
        voiceAnalysisService.saveAnalysis(recordId, analysis);
        return Result.success(null);
    }

    @GetMapping("/get/{recordId}")
    public Result<VoiceAnalysis> getAnalysis(@PathVariable Long recordId) {
        VoiceAnalysis analysis = voiceAnalysisService.getByRecordId(recordId);
        return Result.success(analysis);
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return new BigDecimal(value.toString());
    }

    private String toJson(Object value) {
        if (value == null) return null;
        if (value instanceof List) {
            return value.toString();
        }
        return value.toString();
    }
}
