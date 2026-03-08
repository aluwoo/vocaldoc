"""Visualization service for voice analysis results."""

from typing import Dict, List, Tuple, Optional
import base64
from io import BytesIO

import numpy as np
from parselmouth import Sound


def generate_pitch_contour(pitch_values: List[float], 
                           mean_f0: float,
                           min_f0: float, 
                           max_f0: float) -> str:
    """
    Generate pitch contour plot as base64 encoded PNG.
    
    Args:
        pitch_values: List of F0 values
        mean_f0: Mean fundamental frequency
        min_f0: Minimum F0
        max_f0: Maximum F0
        
    Returns:
        Base64 encoded PNG image
    """
    try:
        import matplotlib
        matplotlib.use('Agg')
        import matplotlib.pyplot as plt
        
        if not pitch_values or len(pitch_values) == 0:
            return ""
        
        times = np.linspace(0, len(pitch_values) * 0.01, len(pitch_values))
        valid_pitch = [p if p > 0 else np.nan for p in pitch_values]
        
        plt.figure(figsize=(10, 4))
        plt.plot(times, valid_pitch, 'b-', linewidth=1)
        plt.axhline(y=mean_f0, color='r', linestyle='--', label=f'Mean: {mean_f0:.1f} Hz')
        plt.fill_between(times, min_f0, max_f0, alpha=0.2, color='green', label='Range')
        
        plt.xlabel('Time (s)')
        plt.ylabel('Frequency (Hz)')
        plt.title('Pitch Contour (F0)')
        plt.legend()
        plt.grid(True, alpha=0.3)
        plt.tight_layout()
        
        buffer = BytesIO()
        plt.savefig(buffer, format='png', dpi=100)
        plt.close()
        
        buffer.seek(0)
        return base64.b64encode(buffer.read()).decode('utf-8')
    except Exception as e:
        return ""


def generate_intensity_contour(intensity_values: List[float],
                                mean_dB: float,
                                max_dB: float) -> str:
    """
    Generate intensity contour plot as base64 encoded PNG.
    
    Args:
        intensity_values: List of intensity values in dB
        mean_dB: Mean intensity
        max_dB: Maximum intensity
        
    Returns:
        Base64 encoded PNG image
    """
    try:
        import matplotlib
        matplotlib.use('Agg')
        import matplotlib.pyplot as plt
        
        if not intensity_values or len(intensity_values) == 0:
            return ""
        
        times = np.linspace(0, len(intensity_values) * 0.01, len(intensity_values))
        
        plt.figure(figsize=(10, 4))
        plt.plot(times, intensity_values, 'g-', linewidth=1)
        plt.axhline(y=mean_dB, color='r', linestyle='--', label=f'Mean: {mean_dB:.1f} dB')
        plt.axhline(y=max_dB, color='orange', linestyle=':', label=f'Max: {max_dB:.1f} dB')
        
        plt.xlabel('Time (s)')
        plt.ylabel('Intensity (dB)')
        plt.title('Intensity Contour')
        plt.legend()
        plt.grid(True, alpha=0.3)
        plt.tight_layout()
        
        buffer = BytesIO()
        plt.savefig(buffer, format='png', dpi=100)
        plt.close()
        
        buffer.seek(0)
        return base64.b64encode(buffer.read()).decode('utf-8')
    except Exception as e:
        return ""


def generate_spectrogram(audio_path: str) -> str:
    """
    Generate spectrogram as base64 encoded PNG.
    
    Args:
        audio_path: Path to audio file
        
    Returns:
        Base64 encoded PNG image
    """
    try:
        import matplotlib
        matplotlib.use('Agg')
        import matplotlib.pyplot as plt
        
        sound = Sound(audio_path)
        spectrogram = sound.to_spectrogram()
        
        plt.figure(figsize=(10, 6))
        spectrogram.plot()
        plt.title('Spectrogram')
        
        buffer = BytesIO()
        plt.savefig(buffer, format='png', dpi=100)
        plt.close()
        
        buffer.seek(0)
        return base64.b64encode(buffer.read()).decode('utf-8')
    except Exception as e:
        return ""


def generate_formant_chart(f1: float, f2: float, f3: float, f4: float) -> str:
    """
    Generate formant frequency bar chart.
    
    Args:
        f1-f4: Formant frequencies
        
    Returns:
        Base64 encoded PNG image
    """
    try:
        import matplotlib
        matplotlib.use('Agg')
        import matplotlib.pyplot as plt
        
        formants = ['F1', 'F2', 'F3', 'F4']
        values = [f1, f2, f3, f4]
        
        plt.figure(figsize=(8, 5))
        bars = plt.bar(formants, values, color=['#3498db', '#2ecc71', '#e74c3c', '#9b59b6'])
        
        for bar, val in zip(bars, values):
            plt.text(bar.get_x() + bar.get_width()/2, bar.get_height() + 50,
                    f'{val:.0f} Hz', ha='center', va='bottom')
        
        plt.xlabel('Formant')
        plt.ylabel('Frequency (Hz)')
        plt.title('Formant Frequencies')
        plt.grid(True, alpha=0.3, axis='y')
        plt.tight_layout()
        
        buffer = BytesIO()
        plt.savefig(buffer, format='png', dpi=100)
        plt.close()
        
        buffer.seek(0)
        return base64.b64encode(buffer.read()).decode('utf-8')
    except Exception as e:
        return ""


def generate_perturbation_chart(jitter_local: float, shimmer_local: float, hnr: float) -> str:
    """
    Generate perturbation metrics comparison chart.
    
    Args:
        jitter_local: Jitter local percentage
        shimmer_local: Shimmer local percentage  
        hnr: Harmonics-to-Noise Ratio in dB
        
    Returns:
        Base64 encoded PNG image
    """
    try:
        import matplotlib
        matplotlib.use('Agg')
        import matplotlib.pyplot as plt
        
        metrics = ['Jitter\n(local %)', 'Shimmer\n(local %)', 'HNR\n(dB)']
        values = [jitter_local, shimmer_local, hnr]
        
        colors = ['#e74c3c', '#f39c12', '#27ae60']
        
        plt.figure(figsize=(8, 5))
        bars = plt.bar(metrics, values, color=colors)
        
        for bar, val in zip(bars, values):
            plt.text(bar.get_x() + bar.get_width()/2, bar.get_height() + 0.1,
                    f'{val:.2f}', ha='center', va='bottom')
        
        plt.ylabel('Value')
        plt.title('Voice Quality Metrics')
        plt.grid(True, alpha=0.3, axis='y')
        plt.tight_layout()
        
        buffer = BytesIO()
        plt.savefig(buffer, format='png', dpi=100)
        plt.close()
        
        buffer.seek(0)
        return base64.b64encode(buffer.read()).decode('utf-8')
    except Exception as e:
        return ""


class VisualizationService:
    """Service for generating voice analysis visualizations."""
    
    @staticmethod
    def generate_all_charts(audio_path: str, 
                            pitch_values: List[float],
                            intensity_values: List[float],
                            mean_f0: float, min_f0: float, max_f0: float,
                            mean_dB: float, max_dB: float,
                            f1: float, f2: float, f3: float, f4: float,
                            jitter_local: float, shimmer_local: float, 
                            hnr: float) -> Dict[str, str]:
        """
        Generate all visualization charts.
        
        Returns:
            Dict with chart names as keys and base64 images as values
        """
        charts = {}
        
        charts['pitch_contour'] = generate_pitch_contour(
            pitch_values, mean_f0, min_f0, max_f0
        )
        charts['intensity_contour'] = generate_intensity_contour(
            intensity_values, mean_dB, max_dB
        )
        charts['formants'] = generate_formant_chart(f1, f2, f3, f4)
        charts['perturbation'] = generate_perturbation_chart(
            jitter_local, shimmer_local, hnr
        )
        
        return charts
