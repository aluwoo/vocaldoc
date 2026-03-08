"""Unit tests for voice analysis service."""

import pytest
import sys
import os

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))


class TestVoiceService:
    """Test cases for VoiceService."""

    def test_voice_service_import(self):
        """Test that VoiceService can be imported."""
        from service.voice_service import VoiceService, VoiceResult
        assert VoiceService is not None
        assert VoiceResult is not None

    def test_voice_service_init(self):
        """Test VoiceService initialization."""
        from service.voice_service import VoiceService
        
        service = VoiceService(voice_type='female')
        assert service.voice_type == 'female'
        assert service.ceiling == 5000
        
        service_male = VoiceService(voice_type='male')
        assert service_male.ceiling == 5500
        
        service_child = VoiceService(voice_type='child')
        assert service_child.ceiling == 8000

    def test_voice_result_defaults(self):
        """Test VoiceResult default values."""
        from service.voice_service import VoiceResult
        
        result = VoiceResult()
        assert result.mean_f0 == 0.0
        assert result.jitter_local == 0.0
        assert result.shimmer_local == 0.0
        assert result.hnr == 0.0
        assert result.pitch_contour == []


class TestComparisonService:
    """Test cases for ComparisonService."""

    def test_comparison_service_import(self):
        """Test that ComparisonService can be imported."""
        from service.comparison import ComparisonService, compare_two_recordings
        assert ComparisonService is not None

    def test_compare_two_recordings(self):
        """Test comparison of two recordings."""
        from service.comparison import compare_two_recordings
        
        analysis1 = {
            'recordId': 1,
            'recordDate': '2024-01-01',
            'meanF0': 220.0,
            'f1Mean': 500.0,
            'f2Mean': 1500.0,
            'meanDB': 70.0,
            'jitterLocal': 1.0,
            'shimmerLocal': 3.0,
            'hnr': 12.0
        }
        
        analysis2 = {
            'recordId': 2,
            'recordDate': '2024-02-01',
            'meanF0': 230.0,
            'f1Mean': 510.0,
            'f2Mean': 1550.0,
            'meanDB': 72.0,
            'jitterLocal': 0.8,
            'shimmerLocal': 2.5,
            'hnr': 14.0
        }
        
        result = compare_two_recordings(analysis1, analysis2)
        
        assert result.record_id1 == 1
        assert result.record_id2 == 2
        assert result.overall_improvement > 0

    def test_calculate_trend(self):
        """Test trend calculation."""
        from service.comparison import calculate_trend
        
        analyses = [
            {'recordDate': '2024-01-01', 'meanF0': 200.0},
            {'recordDate': '2024-02-01', 'meanF0': 210.0},
            {'recordDate': '2024-03-01', 'meanF0': 220.0},
        ]
        
        result = calculate_trend(analyses, 'meanF0')
        
        assert result['trend'] in ['improving', 'stable']


class TestAIRecommendationService:
    """Test cases for AI recommendation service."""

    def test_ai_service_import(self):
        """Test that AI services can be imported."""
        from service.ai_analysis import TimbreAnalysisService, AIRecommendationService
        assert TimbreAnalysisService is not None
        assert AIRecommendationService is not None

    def test_extract_timbre_features(self):
        """Test timbre feature extraction."""
        from service.ai_analysis import TimbreAnalysisService
        
        service = TimbreAnalysisService()
        
        analysis = {
            'f1_mean': 300.0,
            'f2_mean': 2000.0,
            'f3_mean': 2500.0,
            'mean_dB': 70.0,
            'jitter_local': 0.5,
            'shimmer_local': 2.0,
            'hnr': 15.0,
            'f0_range': 100.0,
            'max_dB': 80.0
        }
        
        features = service.extract_timbre_features(analysis)
        
        assert 0 <= features.brightness <= 1
        assert 0 <= features.openness <= 1
        assert 0 <= features.roughness <= 1

    def test_match_voice_type(self):
        """Test voice type matching."""
        from service.ai_analysis import TimbreAnalysisService
        
        service = TimbreAnalysisService()
        
        analysis = {
            'f1_mean': 280.0,
            'f2_mean': 2200.0,
            'f3_mean': 2800.0,
            'mean_dB': 70.0,
            'jitter_local': 0.5,
            'shimmer_local': 2.0,
            'hnr': 15.0,
            'f0_range': 150.0,
            'max_dB': 80.0
        }
        
        result = service.match_voice_type(analysis)
        
        assert result['primary_match'] is not None
        assert 'timbre_features' in result

    def test_generate_recommendations(self):
        """Test recommendation generation."""
        from service.ai_analysis import AIRecommendationService
        
        service = AIRecommendationService()
        
        analysis = {
            'hnr': 8.0,
            'jitter_local': 2.0,
            'shimmer_local': 5.0,
            'f1_mean': 250.0,
            'f2_mean': 1800.0,
        }
        
        result = service.generate_recommendations(analysis)
        
        assert 'overall_score' in result
        assert 'strengths' in result
        assert 'weaknesses' in result
        assert 'suggestions' in result


class TestVisualizationService:
    """Test cases for VisualizationService."""

    def test_visualization_import(self):
        """Test that visualization can be imported."""
        from service.visualization import VisualizationService
        assert VisualizationService is not None


if __name__ == '__main__':
    pytest.main([__file__, '-v'])
