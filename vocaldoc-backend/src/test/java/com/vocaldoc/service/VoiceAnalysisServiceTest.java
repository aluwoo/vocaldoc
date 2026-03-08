package com.vocaldoc.service;

import com.vocaldoc.entity.VoiceAnalysis;
import com.vocaldoc.mapper.VoiceAnalysisMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VoiceAnalysisServiceTest {

    @Mock
    private VoiceAnalysisMapper voiceAnalysisMapper;

    @InjectMocks
    private com.vocaldoc.service.impl.VoiceAnalysisServiceImpl voiceAnalysisService;

    @Test
    public void testSaveAnalysis() {
        VoiceAnalysis analysis = new VoiceAnalysis();
        analysis.setMeanF0(BigDecimal.valueOf(220.5));
        analysis.setMinF0(BigDecimal.valueOf(180.0));
        analysis.setMaxF0(BigDecimal.valueOf(280.0));
        analysis.setHnr(BigDecimal.valueOf(15.5));

        when(voiceAnalysisMapper.selectOne(any())).thenReturn(null);
        when(voiceAnalysisMapper.insert(any())).thenReturn(1);

        voiceAnalysisService.saveAnalysis(1L, analysis);

        verify(voiceAnalysisMapper, times(1)).insert(any());
    }

    @Test
    public void testGetByRecordId() {
        VoiceAnalysis analysis = new VoiceAnalysis();
        analysis.setId(1L);
        analysis.setRecordId(100L);
        analysis.setMeanF0(BigDecimal.valueOf(220.5));

        when(voiceAnalysisMapper.selectOne(any())).thenReturn(analysis);

        VoiceAnalysis result = voiceAnalysisService.getByRecordId(100L);

        assertNotNull(result);
        assertEquals(100L, result.getRecordId());
    }

    @Test
    public void testGetByRecordIdNotFound() {
        when(voiceAnalysisMapper.selectOne(any())).thenReturn(null);

        VoiceAnalysis result = voiceAnalysisService.getByRecordId(999L);

        assertNull(result);
    }
}
