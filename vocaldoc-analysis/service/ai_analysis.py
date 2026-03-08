"""AI-powered voice analysis and recommendation service."""

from dataclasses import dataclass
from typing import List, Dict, Optional
import numpy as np


@dataclass
class TimbreFeatures:
    """Extracted timbre features from voice analysis."""
    brightness: float      # 音色亮度 - high frequency energy ratio
    nasality: float       # 鼻音程度 - F1/F2 relationship
    openness: float       # 开口度 - F1 frequency
    frontness: float      # 舌位前后 - F2 frequency
    roughness: float      # 粗糙度 - jitter + shimmer
    breathiness: float    # 气息声 - hnr inverse
    strain: float         # 紧张度 - pitch range / intensity


@dataclass
class StandardTimbre:
    """Standard timbre reference."""
    voice_type: str       # 'soprano', 'alto', 'tenor', 'baritone', 'bass'
    name: str             # Reference name
    f1_range: tuple       # (min, max)
    f2_range: tuple
    f3_typical: float
    brightness_typical: float
    hnr_min: float


class TimbreAnalysisService:
    """AI-powered timbre analysis service."""
    
    STANDARD_TIMBRES = [
        StandardTimbre(
            voice_type='soprano', name='女高音',
            f1_range=(200, 350), f2_range=(2000, 2800), f3_typical=2900,
            brightness_typical=0.6, hnr_min=15
        ),
        StandardTimbre(
            voice_type='alto', name='女低音',
            f1_range=(180, 300), f2_range=(1600, 2400), f3_typical=2500,
            brightness_typical=0.5, hnr_min=12
        ),
        StandardTimbre(
            voice_type='tenor', name='男高音',
            f1_range=(160, 260), f2_range=(1400, 2200), f3_typical=2300,
            brightness_typical=0.45, hnr_min=12
        ),
        StandardTimbre(
            voice_type='baritone', name='男中音',
            f1_range=(140, 220), f2_range=(1200, 2000), f3_typical=2100,
            brightness_typical=0.4, hnr_min=10
        ),
        StandardTimbre(
            voice_type='bass', name='男低音',
            f1_range=(100, 180), f2_range=(800, 1500), f3_typical=1600,
            brightness_typical=0.35, hnr_min=8
        ),
    ]
    
    def extract_timbre_features(self, analysis: dict) -> TimbreFeatures:
        """
        Extract timbre features from voice analysis results.
        
        Args:
            analysis: Voice analysis result dict
            
        Returns:
            TimbreFeatures with extracted characteristics
        """
        f1 = analysis.get('f1_mean', 0) or 0
        f2 = analysis.get('f2_mean', 0) or 0
        f3 = analysis.get('f3_mean', 0) or 0
        mean_dB = analysis.get('mean_dB', 0) or 0
        jitter = analysis.get('jitter_local', 0) or 0
        shimmer = analysis.get('shimmer_local', 0) or 0
        hnr = analysis.get('hnr', 0) or 0
        
        brightness = self._calculate_brightness(f1, f2, f3, mean_dB)
        nasality = self._calculate_nasality(f1, f2)
        openness = self._calculate_openness(f1)
        frontness = self._calculate_frontness(f2)
        roughness = self._calculate_roughness(jitter, shimmer)
        breathiness = self._calculate_breathiness(hnr)
        strain = self._calculate_strain(analysis)
        
        return TimbreFeatures(
            brightness=brightness,
            nasality=nasality,
            openness=openness,
            frontness=frontness,
            roughness=roughness,
            breathiness=breathiness,
            strain=strain
        )
    
    def _calculate_brightness(self, f1, f2, f3, dB) -> float:
        """Calculate brightness score (0-1)."""
        if f1 == 0:
            return 0.5
        ratio = (f2 + f3) / (f1 * 4)
        db_factor = min(1.0, (dB + 60) / 60)
        return min(1.0, ratio * db_factor)
    
    def _calculate_nasality(self, f1, f2) -> float:
        """Calculate nasality score (0-1)."""
        if f1 == 0:
            return 0.5
        ratio = f1 / f2 if f2 != 0 else 0.5
        return min(1.0, ratio)
    
    def _calculate_openness(self, f1) -> float:
        """Calculate mouth openness (0-1, 0=closed, 1=open)."""
        if f1 == 0:
            return 0.5
        return min(1.0, f1 / 400)
    
    def _calculate_frontness(self, f2) -> float:
        """Calculate tongue position (0=back, 1=front)."""
        if f2 == 0:
            return 0.5
        return min(1.0, f2 / 2500)
    
    def _calculate_roughness(self, jitter, shimmer) -> float:
        """Calculate roughness from perturbation metrics."""
        j_factor = min(1.0, jitter / 5.0)
        s_factor = min(1.0, shimmer / 10.0)
        return (j_factor + s_factor) / 2
    
    def _calculate_breathiness(self, hnr) -> float:
        """Calculate breathiness (inverse of HNR)."""
        if hnr <= 0:
            return 1.0
        return max(0, 1.0 - hnr / 20.0)
    
    def _calculate_strain(self, analysis) -> float:
        """Calculate vocal strain from pitch range and intensity."""
        f0_range = analysis.get('f0_range', 0) or 0
        max_dB = analysis.get('max_dB', 0) or 0
        mean_dB = analysis.get('mean_dB', 0) or 0
        
        range_factor = min(1.0, f0_range / 400)
        intensity_factor = (max_dB - mean_dB) / 30 if max_dB > mean_dB else 0
        
        return (range_factor + intensity_factor) / 2
    
    def match_voice_type(self, analysis: dict) -> Dict:
        """
        Match voice to standard voice types.
        
        Returns:
            Dict with matched type and similarity scores
        """
        timbre = self.extract_timbre_features(analysis)
        scores = []
        
        for standard in self.STANDARD_TIMBRES:
            score = self._calculate_type_similarity(timbre, standard)
            scores.append({
                'voice_type': standard.voice_type,
                'name': standard.name,
                'score': score,
                'f1_match': self._in_range(analysis.get('f1_mean', 0), standard.f1_range),
                'f2_match': self._in_range(analysis.get('f2_mean', 0), standard.f2_range)
            })
        
        scores.sort(key=lambda x: x['score'], reverse=True)
        
        return {
            'primary_match': scores[0] if scores else None,
            'alternatives': scores[1:],
            'timbre_features': {
                'brightness': timbre.brightness,
                'nasality': timbre.nasality,
                'openness': timbre.openness,
                'frontness': timbre.frontness,
                'roughness': timbre.roughness,
                'breathiness': timbre.breathiness,
                'strain': timbre.strain
            }
        }
    
    def _calculate_type_similarity(self, timbre: TimbreFeatures, standard: StandardTimbre) -> float:
        """Calculate similarity score to standard voice type."""
        brightness_diff = abs(timbre.brightness - standard.brightness_typical)
        brightness_score = 1.0 - brightness_diff
        
        hnr_score = max(0, timbre.breathiness * 2) if timbre.breathiness < (1 - standard.hnr_min / 20) else 0
        
        return (brightness_score * 0.6 + hnr_score * 0.4) * 100
    
    def _in_range(self, value, range_tuple) -> bool:
        """Check if value is in range."""
        if not value or value == 0:
            return False
        return range_tuple[0] <= value <= range_tuple[1]
    
    def compare_with_standard(self, analysis: dict, voice_type: str) -> Dict:
        """
        Compare user's voice with standard timbre.
        
        Returns:
            Detailed comparison with differences and recommendations
        """
        standard = next((s for s in self.STANDARD_TIMBRES if s.voice_type == voice_type), None)
        if not standard:
            return {'error': 'Unknown voice type'}
        
        timbre = self.extract_timbre_features(analysis)
        
        comparison = {
            'standard_name': standard.name,
            'differences': [],
            'recommendations': []
        }
        
        if not self._in_range(analysis.get('f1_mean', 0), standard.f1_range):
            f1 = analysis.get('f1_mean', 0)
            if f1 < standard.f1_range[0]:
                comparison['differences'].append(f'F1偏低 ({f1:.0f}Hz < {standard.f1_range[0]}Hz)')
                comparison['recommendations'].append('建议：张大嘴巴练习，提高口腔共鸣')
            else:
                comparison['differences'].append(f'F1偏高 ({f1:.0f}Hz > {standard.f1_range[1]}Hz)')
                comparison['recommendations'].append('建议：放松下巴，减少口腔过度打开')
        
        if not self._in_range(analysis.get('f2_mean', 0), standard.f2_range):
            f2 = analysis.get('f2_mean', 0)
            if f2 < standard.f2_range[0]:
                comparison['differences'].append(f'F2偏低 ({f2:.0f}Hz < {standard.f2_range[0]}Hz)')
                comparison['recommendations'].append('建议：舌尖向前，抬高舌位')
            else:
                comparison['differences'].append(f'F2偏高 ({f2:.0f}Hz > {standard.f2_range[1]}Hz)')
                comparison['recommendations'].append('建议：舌根放松，避免过度靠后')
        
        hnr = analysis.get('hnr', 0) or 0
        if hnr < standard.hnr_min:
            comparison['differences'].append(f'谐噪比偏低 ({hnr:.1f}dB < {standard.hnr_min}dB)')
            comparison['recommendations'].append('建议：加强气息支持，减少声带漏气')
        
        jitter = analysis.get('jitter_local', 0) or 0
        if jitter > 1.04:
            comparison['differences'].append(f'频率微扰偏高 ({jitter:.2f}% > 1.04%)')
            comparison['recommendations'].append('建议：加强声带闭合练习')
        
        shimmer = analysis.get('shimmer_local', 0) or 0
        if shimmer > 3.81:
            comparison['differences'].append(f'振幅微扰偏高 ({shimmer:.2f}% > 3.81%)')
            comparison['recommendations'].append('建议：保持气息平稳')
        
        return comparison


class AIRecommendationService:
    """Generate AI-powered personalized recommendations."""
    
    def generate_recommendations(self, analysis: dict, history: list = None) -> Dict:
        """
        Generate personalized improvement recommendations.
        
        Args:
            analysis: Current voice analysis
            history: Optional list of historical analyses
            
        Returns:
            Dict with recommendations and analysis
        """
        timbre_service = TimbreAnalysisService()
        
        recommendations = {
            'overall_score': 0,
            'strengths': [],
            'weaknesses': [],
            'suggestions': [],
            'training_focus': []
        }
        
        hnr = analysis.get('hnr', 0) or 0
        jitter = analysis.get('jitter_local', 0) or 0
        shimmer = analysis.get('shimmer_local', 0) or 0
        
        score = 100
        
        if hnr >= 15:
            recommendations['strengths'].append('气息控制良好，嗓音清晰')
        elif hnr >= 10:
            recommendations['weaknesses'].append('气息支持有待加强')
            recommendations['suggestions'].append('练习腹式呼吸，增强气息支持')
            score -= 10
        else:
            recommendations['weaknesses'].append('气息不足，有气息声')
            recommendations['suggestions'].append('需要系统气息训练')
            score -= 20
        
        if jitter <= 1.04:
            recommendations['strengths'].append('频率稳定性好')
        else:
            recommendations['weaknesses'].append('频率微扰较大')
            recommendations['suggestions'].append('练习音阶保持，增强声带控制')
            score -= 15
        
        if shimmer <= 3.81:
            recommendations['strengths'].append('振幅稳定性好')
        else:
            recommendations['weaknesses'].append('振幅波动较大')
            recommendations['suggestions'].append('保持气息平稳，避免声音抖动')
            score -= 15
        
        f1 = analysis.get('f1_mean', 0) or 0
        f2 = analysis.get('f2_mean', 0) or 0
        
        if f1 > 0:
            if f1 < 200:
                recommendations['training_focus'].append('开口度训练：练习张大口型')
            elif f1 > 350:
                recommendations['training_focus'].append('口腔放松：避免过度打开')
        
        if f2 > 0:
            if f2 < 1500:
                recommendations['training_focus'].append('舌位训练：舌尖向前靠拢')
        
        if history and len(history) >= 3:
            trend = self._analyze_history_trend(history)
            recommendations['trend_analysis'] = trend
        
        recommendations['overall_score'] = max(0, score)
        
        return recommendations
    
    def _analyze_history_trend(self, history: list) -> Dict:
        """Analyze progress trend from historical data."""
        if len(history) < 3:
            return {'trend': 'insufficient_data'}
        
        hnr_values = [h.get('hnr', 0) or 0 for h in history if h.get('hnr')]
        jitter_values = [h.get('jitter_local', 0) or 0 for h in history if h.get('jitter_local')]
        
        trend = {'hnr_trend': 'stable', 'stability_trend': 'stable'}
        
        if len(hnr_values) >= 3:
            recent = hnr_values[-1]
            earlier = hnr_values[0]
            if recent > earlier * 1.1:
                trend['hnr_trend'] = 'improving'
            elif recent < earlier * 0.9:
                trend['hnr_trend'] = 'declining'
        
        if len(jitter_values) >= 3:
            recent = jitter_values[-1]
            earlier = jitter_values[0]
            if recent < earlier * 0.9:
                trend['stability_trend'] = 'improving'
            elif recent > earlier * 1.1:
                trend['stability_trend'] = 'declining'
        
        return trend
