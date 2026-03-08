"""Comparison service for voice analysis results."""

from dataclasses import dataclass
from typing import List, Dict, Optional
import numpy as np


@dataclass
class ComparisonResult:
    """Result of comparing two voice analysis results."""
    metric_name: str
    value1: float
    value2: float
    difference: float
    percent_change: float
    status: str  # 'improved', 'declined', 'stable'


@dataclass
class OverallComparison:
    """Overall comparison between two recordings."""
    record_id1: int
    record_id2: int
    record_date1: str
    record_date2: str
    comparisons: List[ComparisonResult]
    overall_improvement: float
    improved_metrics: int
    declined_metrics: int
    stable_metrics: int


def compare_two_recordings(analysis1: dict, analysis2: dict) -> OverallComparison:
    """
    Compare two voice analysis results.
    
    Args:
        analysis1: First analysis result (older)
        analysis2: Second analysis result (newer)
        
    Returns:
        OverallComparison with detailed metrics
    """
    metrics_to_compare = [
        ('meanF0', '平均基频', True),
        ('f1Mean', 'F1共振峰', True),
        ('f2Mean', 'F2共振峰', True),
        ('meanDB', '平均响度', True),
        ('jitterLocal', 'Jitter', False),
        ('shimmerLocal', 'Shimmer', False),
        ('hnr', '谐噪比', True),
    ]
    
    comparisons = []
    improved = 0
    declined = 0
    stable = 0
    
    for metric_key, metric_name, higher_is_better in metrics_to_compare:
        val1 = analysis1.get(metric_key, 0) or 0
        val2 = analysis2.get(metric_key, 0) or 0
        
        if val1 == 0 and val2 == 0:
            continue
            
        diff = val2 - val1
        percent = (diff / val1 * 100) if val1 != 0 else 0
        
        if higher_is_better:
            if diff > 0.1:
                status = 'improved'
                improved += 1
            elif diff < -0.1:
                status = 'declined'
                declined += 1
            else:
                status = 'stable'
                stable += 1
        else:
            if diff < -0.1:
                status = 'improved'
                improved += 1
            elif diff > 0.1:
                status = 'declined'
                declined += 1
            else:
                status = 'stable'
                stable += 1
        
        comparisons.append(ComparisonResult(
            metric_name=metric_name,
            value1=val1,
            value2=val2,
            difference=diff,
            percent_change=percent,
            status=status
        ))
    
    overall = (improved - declined) / len(comparisons) * 100 if comparisons else 0
    
    return OverallComparison(
        record_id1=analysis1.get('recordId', 0),
        record_id2=analysis2.get('recordId', 0),
        record_date1=analysis1.get('recordDate', ''),
        record_date2=analysis2.get('recordDate', ''),
        comparisons=comparisons,
        overall_improvement=overall,
        improved_metrics=improved,
        declined_metrics=declined,
        stable_metrics=stable
    )


def calculate_trend(analyses: List[dict], metric: str) -> Dict:
    """
    Calculate trend for a specific metric across multiple recordings.
    
    Args:
        analyses: List of analysis results ordered by date
        metric: Metric key to analyze
        
    Returns:
        Trend analysis result
    """
    values = []
    dates = []
    
    for a in analyses:
        val = a.get(metric)
        if val is not None and val > 0:
            values.append(val)
            dates.append(a.get('recordDate', ''))
    
    if len(values) < 2:
        return {
            'trend': 'insufficient_data',
            'slope': 0,
            'values': values,
            'dates': dates
        }
    
    x = np.arange(len(values))
    slope, intercept = np.polyfit(x, values, 1)
    
    first_third = np.mean(values[:len(values)//3]) if len(values) >= 3 else values[0]
    last_third = np.mean(values[-len(values)//3:]) if len(values) >= 3 else values[-1]
    change = (last_third - first_third) / first_third * 100 if first_third != 0 else 0
    
    if change > 10:
        trend = 'improving'
    elif change < -10:
        trend = 'declining'
    else:
        trend = 'stable'
    
    return {
        'trend': trend,
        'slope': float(slope),
        'percent_change': float(change),
        'values': values,
        'dates': dates,
        'mean': float(np.mean(values)),
        'std': float(np.std(values)),
        'min': float(np.min(values)),
        'max': float(np.max(values))
    }


def detect_stagnation(analyses: List[dict], metric: str, threshold: float = 5.0) -> Dict:
    """
    Detect if progress has stagnated for a metric.
    
    Args:
        analyses: List of analysis results
        metric: Metric to check
        threshold: Minimum change percentage to consider as progress
        
    Returns:
        Stagnation detection result
    """
    if len(analyses) < 3:
        return {
            'stagnant': False,
            'reason': 'insufficient_data'
        }
    
    values = []
    for a in analyses:
        val = a.get(metric)
        if val is not None and val > 0:
            values.append(val)
    
    if len(values) < 3:
        return {
            'stagnant': False,
            'reason': 'insufficient_valid_data'
        }
    
    recent = values[-3:]
    recent_change = (recent[-1] - recent[0]) / recent[0] * 100 if recent[0] != 0 else 0
    
    if abs(recent_change) < threshold:
        return {
            'stagnant': True,
            'metric': metric,
            'recent_values': recent,
            'change_percent': recent_change,
            'suggestion': f'{metric}指标近3次无明显变化，建议调整训练方法'
        }
    
    return {
        'stagnant': False,
        'metric': metric,
        'recent_values': recent,
        'change_percent': recent_change
    }


class ComparisonService:
    """Service for voice analysis comparison and trend analysis."""
    
    def compare_records(self, analysis1: dict, analysis2: dict) -> OverallComparison:
        """Compare two voice analysis records."""
        return compare_two_recordings(analysis1, analysis2)
    
    def analyze_trend(self, analyses: List[dict], metric: str) -> Dict:
        """Analyze trend for a specific metric."""
        return calculate_trend(analyses, metric)
    
    def check_progress(self, analyses: List[dict]) -> Dict:
        """
        Check overall progress across all key metrics.
        
        Returns:
            Dict with progress status for each metric
        """
        key_metrics = ['meanF0', 'f1Mean', 'f2Mean', 'meanDB', 'jitterLocal', 'shimmerLocal', 'hnr']
        
        results = {}
        for metric in key_metrics:
            results[metric] = {
                'trend': calculate_trend(analyses, metric),
                'stagnation': detect_stagnation(analyses, metric)
            }
        
        improving_metrics = sum(1 for r in results.values() if r['trend']['trend'] == 'improving')
        declining_metrics = sum(1 for r in results.values() if r['trend']['trend'] == 'declining')
        stagnant_metrics = sum(1 for r in results.values() if r['stagnation'].get('stagnant', False))
        
        return {
            'metrics': results,
            'summary': {
                'improving': improving_metrics,
                'declining': declining_metrics,
                'stagnant': stagnant_metrics,
                'total': len(key_metrics)
            }
        }
