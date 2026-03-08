"""Integration tests for voice analysis API."""

import pytest
from fastapi.testclient import TestClient
from unittest.mock import patch, MagicMock
import sys
import os

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from main import app

client = TestClient(app)


class TestHealthEndpoint:
    """Test health check endpoints."""

    def test_root(self):
        """Test root endpoint."""
        response = client.get("/")
        assert response.status_code == 200
        assert "message" in response.json()

    def test_health(self):
        """Test health check endpoint."""
        response = client.get("/health")
        assert response.status_code == 200
        data = response.json()
        assert data["status"] == "ok"
        assert data["service"] == "vocaldoc-analysis"


class TestAnalysisEndpoint:
    """Test analysis endpoints."""

    @patch('service.voice_service.VoiceService')
    def test_analyze_invalid_voice_type(self, mock_service):
        """Test analyze with invalid voice type."""
        response = client.post(
            "/api/v1/analysis/analyze?voice_type=invalid",
            files={"file": ("test.wav", b"fake audio", "audio/wav")}
        )
        assert response.status_code == 400

    @patch('service.voice_service.VoiceService')
    @patch('service.voice_service.Sound')
    def test_analyze_invalid_format(self, mock_sound, mock_service):
        """Test analyze with invalid file format."""
        response = client.post(
            "/api/v1/analysis/analyze?voice_type=female",
            files={"file": ("test.txt", b"text content", "text/plain")}
        )
        assert response.status_code == 400


class TestComparisonEndpoint:
    """Test comparison endpoints."""

    def test_compare_missing_data(self):
        """Test compare with missing data."""
        response = client.post("/api/v1/analysis/compare", json={})
        assert response.status_code == 200
        data = response.json()
        assert "error" in data["data"] or "success" in data

    def test_trend_insufficient_data(self):
        """Test trend with insufficient data."""
        response = client.post(
            "/api/v1/analysis/trend?metric=meanF0",
            json=[]
        )
        assert response.status_code == 200

    def test_progress_insufficient_data(self):
        """Test progress with insufficient data."""
        response = client.post("/api/v1/analysis/progress", json=[])
        assert response.status_code == 200


class TestAIEndpoint:
    """Test AI analysis endpoints."""

    def test_timbre_analysis(self):
        """Test timbre analysis endpoint."""
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
        
        response = client.post("/api/v1/analysis/ai/timbre", json=analysis)
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "data" in data

    def test_recommendations(self):
        """Test recommendations endpoint."""
        analysis = {
            'hnr': 8.0,
            'jitter_local': 2.0,
            'shimmer_local': 5.0,
            'f1_mean': 250.0,
            'f2_mean': 1800.0,
        }
        
        response = client.post("/api/v1/analysis/ai/recommendations", json=analysis)
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "overall_score" in data["data"]


class TestAPIResponseFormat:
    """Test API response format consistency."""

    def test_error_response_format(self):
        """Test error response has consistent format."""
        response = client.post(
            "/api/v1/analysis/analyze?voice_type=invalid",
            files={"file": ("test.wav", b"fake", "audio/wav")}
        )
        assert response.status_code == 400
        
    def test_success_response_format(self):
        """Test success response has consistent format."""
        response = client.get("/health")
        assert response.status_code == 200
        data = response.json()
        assert "status" in data or "success" in data


if __name__ == '__main__':
    pytest.main([__file__, '-v'])
