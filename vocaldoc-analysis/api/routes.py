"""FastAPI routes for voice analysis service."""

from pathlib import Path
from typing import Optional, Dict, List

from fastapi import APIRouter, File, HTTPException, UploadFile
from pydantic import BaseModel

from service.voice_service import VoiceService, VoiceResult
from service.visualization import VisualizationService
from service.comparison import ComparisonService
from service.ai_analysis import TimbreAnalysisService, AIRecommendationService


router = APIRouter(prefix="/api/v1/analysis", tags=["analysis"])


class AnalysisResponse(BaseModel):
    """Response model for voice analysis."""
    success: bool
    data: Optional[VoiceResult] = None
    error: Optional[str] = None


class VisualizationResponse(BaseModel):
    """Response model for visualization."""
    success: bool
    charts: Optional[Dict[str, str]] = None
    error: Optional[str] = None


@router.post("/analyze", response_model=AnalysisResponse)
async def analyze_voice(
    voice_type: str = "female",
    file: UploadFile = File(...)
):
    """
    Analyze voice recording and extract acoustic parameters.
    
    Args:
        voice_type: Voice type - 'child', 'female', or 'male'
        file: Audio file upload
        
    Returns:
        VoiceResult with all analysis parameters
    """
    if voice_type not in ['child', 'female', 'male']:
        raise HTTPException(
            status_code=400,
            detail="voice_type must be 'child', 'female', or 'male'"
        )
    
    filename: str = file.filename or "temp_audio.wav"
    
    # Validate file extension
    file_ext = Path(filename).suffix.lower().lstrip('.')
    if file_ext not in ['wav', 'flac', 'mp3', 'm4a']:
        raise HTTPException(
            status_code=400,
            detail="Unsupported file format. Supported: wav, flac, mp3, m4a"
        )
    
    # Save uploaded file temporarily
    temp_path = Path(f"/tmp/{filename}")
    try:
        content = await file.read()
        temp_path.write_bytes(content)
        
        # Perform analysis
        service = VoiceService(voice_type=voice_type)
        result = service.analyze(str(temp_path))
        
        return AnalysisResponse(success=True, data=result)
        
    except Exception as e:
        return AnalysisResponse(
            success=False,
            error=str(e)
        )
    finally:
        # Cleanup temp file
        if temp_path.exists():
            temp_path.unlink()


@router.get("/health")
async def health():
    """Health check endpoint."""
    return {"status": "ok", "service": "voice-analysis"}


@router.post("/visualize", response_model=VisualizationResponse)
async def visualize_voice(
    voice_type: str = "female",
    file: UploadFile = File(...)
):
    """
    Generate visualization charts for voice recording.
    
    Args:
        voice_type: Voice type - 'child', 'female', or 'male'
        file: Audio file upload
        
    Returns:
        Dict of base64 encoded chart images
    """
    if voice_type not in ['child', 'female', 'male']:
        raise HTTPException(
            status_code=400,
            detail="voice_type must be 'child', 'female', or 'male'"
        )
    
    filename: str = file.filename or "temp_audio.wav"
    file_ext = Path(filename).suffix.lower().lstrip('.')
    if file_ext not in ['wav', 'flac', 'mp3', 'm4a']:
        raise HTTPException(
            status_code=400,
            detail="Unsupported file format"
        )
    
    temp_path = Path(f"/tmp/{filename}")
    try:
        content = await file.read()
        temp_path.write_bytes(content)
        
        service = VoiceService(voice_type=voice_type)
        result = service.analyze(str(temp_path))
        
        charts = VisualizationService.generate_all_charts(
            audio_path=str(temp_path),
            pitch_values=result.pitch_contour,
            intensity_values=result.intensity_contour,
            mean_f0=result.mean_f0,
            min_f0=result.min_f0,
            max_f0=result.max_f0,
            mean_dB=result.mean_dB,
            max_dB=result.max_dB,
            f1=result.f1_mean,
            f2=result.f2_mean,
            f3=result.f3_mean,
            f4=result.f4_mean,
            jitter_local=result.jitter_local,
            shimmer_local=result.shimmer_local,
            hnr=result.hnr
        )
        
        return VisualizationResponse(success=True, charts=charts)
        
    except Exception as e:
        return VisualizationResponse(success=False, error=str(e))
    finally:
        if temp_path.exists():
            temp_path.unlink()


@router.post("/compare")
async def compare_recordings(
    analysis1: Dict,
    analysis2: Dict
):
    """
    Compare two voice analysis results.
    
    Args:
        analysis1: First analysis result (older)
        analysis2: Second analysis result (newer)
        
    Returns:
        Comparison result with metrics differences
    """
    try:
        service = ComparisonService()
        result = service.compare_records(analysis1, analysis2)
        return {"success": True, "data": result}
    except Exception as e:
        return {"success": False, "error": str(e)}


@router.post("/trend")
async def analyze_trend(
    analyses: List[Dict],
    metric: str
):
    """
    Analyze trend for a specific metric across multiple recordings.
    
    Args:
        analyses: List of analysis results ordered by date
        metric: Metric key to analyze
        
    Returns:
        Trend analysis result
    """
    try:
        service = ComparisonService()
        result = service.analyze_trend(analyses, metric)
        return {"success": True, "data": result}
    except Exception as e:
        return {"success": False, "error": str(e)}


@router.post("/progress")
async def check_progress(
    analyses: List[Dict]
):
    """
    Check overall progress across all key metrics.
    
    Args:
        analyses: List of analysis results
        
    Returns:
        Progress status for each metric
    """
    try:
        service = ComparisonService()
        result = service.check_progress(analyses)
        return {"success": True, "data": result}
    except Exception as e:
        return {"success": False, "error": str(e)}


@router.post("/ai/timbre")
async def analyze_timbre(analysis: Dict):
    """
    Analyze timbre features from voice analysis.
    
    Args:
        analysis: Voice analysis result
        
    Returns:
        Timbre features and voice type matching
    """
    try:
        service = TimbreAnalysisService()
        result = service.match_voice_type(analysis)
        return {"success": True, "data": result}
    except Exception as e:
        return {"success": False, "error": str(e)}


@router.post("/ai/compare-standard")
async def compare_with_standard(analysis: Dict, voice_type: str):
    """
    Compare voice with standard timbre.
    
    Args:
        analysis: Voice analysis result
        voice_type: Standard voice type to compare against
        
    Returns:
        Comparison result with recommendations
    """
    try:
        service = TimbreAnalysisService()
        result = service.compare_with_standard(analysis, voice_type)
        return {"success": True, "data": result}
    except Exception as e:
        return {"success": False, "error": str(e)}


@router.post("/ai/recommendations")
async def get_recommendations(analysis: Dict, history: List[Dict] = None):
    """
    Generate personalized AI recommendations.
    
    Args:
        analysis: Current voice analysis
        history: Optional historical analyses for trend
        
    Returns:
        Personalized recommendations
    """
    try:
        service = AIRecommendationService()
        result = service.generate_recommendations(analysis, history)
        return {"success": True, "data": result}
    except Exception as e:
        return {"success": False, "error": str(e)}
