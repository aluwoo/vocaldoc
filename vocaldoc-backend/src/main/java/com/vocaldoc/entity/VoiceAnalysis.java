package com.vocaldoc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vocaldoc.common.enums.VowelType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("voice_analysis")
public class VoiceAnalysis {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long recordId;

    private VowelType vowelType;

    private BigDecimal meanF0;
    private BigDecimal minF0;
    private BigDecimal maxF0;
    private BigDecimal f0Range;

    private BigDecimal f1Mean;
    private BigDecimal f2Mean;
    private BigDecimal f3Mean;
    private BigDecimal f4Mean;

    private BigDecimal meanDB;
    private BigDecimal maxDB;

    private BigDecimal jitterLocal;
    private BigDecimal jitterRap;
    private BigDecimal jitterPpq5;
    private BigDecimal jitterDdp;

    private BigDecimal shimmerLocal;
    private BigDecimal shimmerLocalDB;
    private BigDecimal shimmerApq3;
    private BigDecimal shimmerApq5;

    private BigDecimal hnr;
    private BigDecimal cpp;

    private BigDecimal tremoloRate;
    private BigDecimal tremoloExtent;

    private BigDecimal overallScore;
    private BigDecimal pitchScore;
    private BigDecimal timbreScore;
    private BigDecimal stabilityScore;

    private String aiSuggestion;
    private String aiComparison;

    private String rawPitchContour;
    private String rawIntensityContour;
    private String rawSpectrogram;

    private Integer status;
    private String errorMessage;
    private LocalDateTime analyzedAt;
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

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public VowelType getVowelType() {
        return vowelType;
    }

    public void setVowelType(VowelType vowelType) {
        this.vowelType = vowelType;
    }

    public BigDecimal getMeanF0() {
        return meanF0;
    }

    public void setMeanF0(BigDecimal meanF0) {
        this.meanF0 = meanF0;
    }

    public BigDecimal getMinF0() {
        return minF0;
    }

    public void setMinF0(BigDecimal minF0) {
        this.minF0 = minF0;
    }

    public BigDecimal getMaxF0() {
        return maxF0;
    }

    public void setMaxF0(BigDecimal maxF0) {
        this.maxF0 = maxF0;
    }

    public BigDecimal getF0Range() {
        return f0Range;
    }

    public void setF0Range(BigDecimal f0Range) {
        this.f0Range = f0Range;
    }

    public BigDecimal getF1Mean() {
        return f1Mean;
    }

    public void setF1Mean(BigDecimal f1Mean) {
        this.f1Mean = f1Mean;
    }

    public BigDecimal getF2Mean() {
        return f2Mean;
    }

    public void setF2Mean(BigDecimal f2Mean) {
        this.f2Mean = f2Mean;
    }

    public BigDecimal getF3Mean() {
        return f3Mean;
    }

    public void setF3Mean(BigDecimal f3Mean) {
        this.f3Mean = f3Mean;
    }

    public BigDecimal getF4Mean() {
        return f4Mean;
    }

    public void setF4Mean(BigDecimal f4Mean) {
        this.f4Mean = f4Mean;
    }

    public BigDecimal getMeanDB() {
        return meanDB;
    }

    public void setMeanDB(BigDecimal meanDB) {
        this.meanDB = meanDB;
    }

    public BigDecimal getMaxDB() {
        return maxDB;
    }

    public void setMaxDB(BigDecimal maxDB) {
        this.maxDB = maxDB;
    }

    public BigDecimal getJitterLocal() {
        return jitterLocal;
    }

    public void setJitterLocal(BigDecimal jitterLocal) {
        this.jitterLocal = jitterLocal;
    }

    public BigDecimal getJitterRap() {
        return jitterRap;
    }

    public void setJitterRap(BigDecimal jitterRap) {
        this.jitterRap = jitterRap;
    }

    public BigDecimal getJitterPpq5() {
        return jitterPpq5;
    }

    public void setJitterPpq5(BigDecimal jitterPpq5) {
        this.jitterPpq5 = jitterPpq5;
    }

    public BigDecimal getJitterDdp() {
        return jitterDdp;
    }

    public void setJitterDdp(BigDecimal jitterDdp) {
        this.jitterDdp = jitterDdp;
    }

    public BigDecimal getShimmerLocal() {
        return shimmerLocal;
    }

    public void setShimmerLocal(BigDecimal shimmerLocal) {
        this.shimmerLocal = shimmerLocal;
    }

    public BigDecimal getShimmerLocalDB() {
        return shimmerLocalDB;
    }

    public void setShimmerLocalDB(BigDecimal shimmerLocalDB) {
        this.shimmerLocalDB = shimmerLocalDB;
    }

    public BigDecimal getShimmerApq3() {
        return shimmerApq3;
    }

    public void setShimmerApq3(BigDecimal shimmerApq3) {
        this.shimmerApq3 = shimmerApq3;
    }

    public BigDecimal getShimmerApq5() {
        return shimmerApq5;
    }

    public void setShimmerApq5(BigDecimal shimmerApq5) {
        this.shimmerApq5 = shimmerApq5;
    }

    public BigDecimal getHnr() {
        return hnr;
    }

    public void setHnr(BigDecimal hnr) {
        this.hnr = hnr;
    }

    public BigDecimal getCpp() {
        return cpp;
    }

    public void setCpp(BigDecimal cpp) {
        this.cpp = cpp;
    }

    public BigDecimal getTremoloRate() {
        return tremoloRate;
    }

    public void setTremoloRate(BigDecimal tremoloRate) {
        this.tremoloRate = tremoloRate;
    }

    public BigDecimal getTremoloExtent() {
        return tremoloExtent;
    }

    public void setTremoloExtent(BigDecimal tremoloExtent) {
        this.tremoloExtent = tremoloExtent;
    }

    public BigDecimal getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(BigDecimal overallScore) {
        this.overallScore = overallScore;
    }

    public BigDecimal getPitchScore() {
        return pitchScore;
    }

    public void setPitchScore(BigDecimal pitchScore) {
        this.pitchScore = pitchScore;
    }

    public BigDecimal getTimbreScore() {
        return timbreScore;
    }

    public void setTimbreScore(BigDecimal timbreScore) {
        this.timbreScore = timbreScore;
    }

    public BigDecimal getStabilityScore() {
        return stabilityScore;
    }

    public void setStabilityScore(BigDecimal stabilityScore) {
        this.stabilityScore = stabilityScore;
    }

    public String getAiSuggestion() {
        return aiSuggestion;
    }

    public void setAiSuggestion(String aiSuggestion) {
        this.aiSuggestion = aiSuggestion;
    }

    public String getAiComparison() {
        return aiComparison;
    }

    public void setAiComparison(String aiComparison) {
        this.aiComparison = aiComparison;
    }

    public String getRawPitchContour() {
        return rawPitchContour;
    }

    public void setRawPitchContour(String rawPitchContour) {
        this.rawPitchContour = rawPitchContour;
    }

    public String getRawIntensityContour() {
        return rawIntensityContour;
    }

    public void setRawIntensityContour(String rawIntensityContour) {
        this.rawIntensityContour = rawIntensityContour;
    }

    public String getRawSpectrogram() {
        return rawSpectrogram;
    }

    public void setRawSpectrogram(String rawSpectrogram) {
        this.rawSpectrogram = rawSpectrogram;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
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
}
