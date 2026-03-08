"""Voice analysis service using Parselmouth (Praat)."""

from dataclasses import dataclass, field
from typing import List, Optional

from parselmouth import Sound
import numpy as np

from config import audio_config


@dataclass
class VoiceResult:
    """Complete voice analysis results."""
    # Basic pitch parameters
    mean_f0: float = 0.0          # Average fundamental frequency (Hz)
    min_f0: float = 0.0           # Minimum fundamental frequency
    max_f0: float = 0.0           # Maximum fundamental frequency
    f0_range: float = 0.0         # Pitch range
    
    # Formants
    f1_mean: float = 0.0          # F1 mean
    f2_mean: float = 0.0          # F2 mean
    f3_mean: float = 0.0          # F3 mean
    f4_mean: float = 0.0          # F4 mean
    
    # Intensity
    mean_dB: float = 0.0          # Average loudness
    max_dB: float = 0.0           # Maximum loudness
    
    # Frequency perturbation (Jitter)
    jitter_local: float = 0.0     # Jitter (local) %
    jitter_rap: float = 0.0       # Jitter (rap) %
    jitter_ppq5: float = 0.0      # Jitter (ppq5) %
    jitter_ddp: float = 0.0       # Jitter (ddp) %
    
    # Amplitude perturbation (Shimmer)
    shimmer_local: float = 0.0    # Shimmer (local) %
    shimmer_local_dB: float = 0.0 # Shimmer (local, dB)
    shimmer_apq3: float = 0.0     # Shimmer (apq3) %
    shimmer_apq5: float = 0.0     # Shimmer (apq5) %
    
    # Harmonics-to-Noise Ratio
    hnr: float = 0.0             # HNR (dB)
    
    # Raw data for visualization
    pitch_contour: List[float] = field(default_factory=list)
    intensity_contour: List[float] = field(default_factory=list)


class VoiceService:
    """Voice analysis service using Parselmouth."""
    
    def __init__(self, voice_type: str = 'female'):
        """
        Initialize voice analysis service.
        
        Args:
            voice_type: Voice type - 'child', 'female', or 'male'
                       Affects formant ceiling frequency
        """
        self.voice_type = voice_type
        self.ceiling = audio_config.FORMANT_CEILING.get(voice_type, 5000)
    
    def analyze(self, audio_path: str) -> VoiceResult:
        """
        Perform complete voice analysis on audio file.
        
        Args:
            audio_path: Path to audio file
            
        Returns:
            VoiceResult with all analysis parameters
        """
        sound = Sound(audio_path)
        
        # Run all analysis modules
        pitch_result = self._analyze_pitch(sound)
        formant_result = self._analyze_formants(sound)
        intensity_result = self._analyze_intensity(sound)
        perturbation_result = self._analyze_perturbation(sound)
        hnr = self._analyze_hnr(sound)
        
        return VoiceResult(
            # Pitch
            mean_f0=pitch_result['mean'],
            min_f0=pitch_result['min'],
            max_f0=pitch_result['max'],
            f0_range=pitch_result['range'],
            # Formants
            f1_mean=formant_result['f1'],
            f2_mean=formant_result['f2'],
            f3_mean=formant_result['f3'],
            f4_mean=formant_result['f4'],
            # Intensity
            mean_dB=intensity_result['mean'],
            max_dB=intensity_result['max'],
            # Jitter
            jitter_local=perturbation_result['jitter_local'],
            jitter_rap=perturbation_result['jitter_rap'],
            jitter_ppq5=perturbation_result['jitter_ppq5'],
            jitter_ddp=perturbation_result['jitter_ddp'],
            # Shimmer
            shimmer_local=perturbation_result['shimmer_local'],
            shimmer_local_dB=perturbation_result['shimmer_dB'],
            shimmer_apq3=perturbation_result['shimmer_apq3'],
            shimmer_apq5=perturbation_result['shimmer_apq5'],
            # HNR
            hnr=hnr,
            # Contours for visualization
            pitch_contour=pitch_result['contour'],
            intensity_contour=intensity_result['contour']
        )
    
    def _analyze_pitch(self, sound: Sound) -> dict:
        """Analyze pitch (F0) fundamental frequency."""
        pitch = sound.to_pitch(
            time_step=audio_config.TIME_STEP,
            pitch_floor=audio_config.PITCH_FLOOR,
            pitch_ceiling=audio_config.PITCH_CEILING
        )
        
        f0_values = pitch.selected_array['frequency']
        f0_values = f0_values[f0_values > 0]
        
        if len(f0_values) == 0:
            return {'mean': 0, 'min': 0, 'max': 0, 'range': 0, 'contour': []}
        
        return {
            'mean': float(np.mean(f0_values)),
            'min': float(np.min(f0_values)),
            'max': float(np.max(f0_values)),
            'range': float(np.max(f0_values) - np.min(f0_values)),
            'contour': [float(x) for x in f0_values]
        }
    
    def _analyze_formants(self, sound: Sound) -> dict:
        """Analyze formants (F1-F4) using Burg method."""
        formants = sound.to_formant_burg(
            time_step=audio_config.TIME_STEP,
            maximum_formant=self.ceiling
        )
        
        t = formants.xs()
        results = {}
        
        for i in range(1, 5):
            f_values = [
                formants.get_value_at_time(i, time)
                for time in t
                if formants.get_value_at_time(i, time) > 0
            ]
            results[f'f{i}'] = float(np.mean(f_values)) if f_values else 0.0
        
        return results
    
    def _analyze_intensity(self, sound: Sound) -> dict:
        """Analyze intensity (loudness in dB)."""
        intensity = sound.to_intensity(time_step=audio_config.TIME_STEP)
        
        values = intensity.values.T[0]
        values = values[np.isfinite(values)]
        
        if len(values) == 0:
            return {'mean': 0, 'max': 0, 'contour': []}
        
        return {
            'mean': float(np.mean(values)),
            'max': float(np.max(values)),
            'contour': [float(x) for x in values]
        }
    
    def _analyze_perturbation(self, sound: Sound) -> dict:
        """Analyze perturbation (Jitter and Shimmer)."""
        result = {}
        
        try:
            # Jitter calculations using Praat methods
            result['jitter_local'] = float(sound.get_jitter(
                audio_config.JITTER_WINDOW,
                audio_config.JITTER_LENGTH,
                0.02,
                audio_config.JITTER_FILTER
            ))
            result['jitter_rap'] = float(sound.get_jitter_rap(
                audio_config.JITTER_WINDOW,
                audio_config.JITTER_LENGTH,
                0.02,
                audio_config.JITTER_FILTER
            ))
            result['jitter_ppq5'] = float(sound.get_jitter_ppq5(
                audio_config.JITTER_WINDOW,
                audio_config.JITTER_LENGTH,
                0.02,
                audio_config.JITTER_FILTER
            ))
            result['jitter_ddp'] = float(sound.get_jitter_ddp(
                audio_config.JITTER_WINDOW,
                audio_config.JITTER_LENGTH,
                0.02,
                audio_config.JITTER_FILTER
            ))
        except Exception:
            result['jitter_local'] = 0.0
            result['jitter_rap'] = 0.0
            result['jitter_ppq5'] = 0.0
            result['jitter_ddp'] = 0.0
        
        try:
            # Shimmer calculations
            result['shimmer_local'] = float(sound.get_shimmer(
                audio_config.JITTER_WINDOW,
                audio_config.JITTER_LENGTH,
                0.02,
                audio_config.JITTER_FILTER
            ))
            result['shimmer_dB'] = float(sound.get_shimmer_dB(
                audio_config.JITTER_WINDOW,
                audio_config.JITTER_LENGTH,
                0.02,
                audio_config.JITTER_FILTER
            ))
            result['shimmer_apq3'] = float(sound.get_shimmer_apq3(
                audio_config.JITTER_WINDOW,
                audio_config.JITTER_LENGTH,
                0.02,
                audio_config.JITTER_FILTER
            ))
            result['shimmer_apq5'] = float(sound.get_shimmer_apq5(
                audio_config.JITTER_WINDOW,
                audio_config.JITTER_LENGTH,
                0.02,
                audio_config.JITTER_FILTER
            ))
        except Exception:
            result['shimmer_local'] = 0.0
            result['shimmer_dB'] = 0.0
            result['shimmer_apq3'] = 0.0
            result['shimmer_apq5'] = 0.0
        
        return result
    
    def _analyze_hnr(self, sound: Sound) -> float:
        """Analyze Harmonics-to-Noise Ratio."""
        try:
            harmonicity = sound.to_harmonicity_cc(
                time_step=audio_config.TIME_STEP,
                minimum_pitch=audio_config.PITCH_FLOOR,
                silence_threshold=0.1,
                periods_per_window=1.0
            )
            hnr = harmonicity.values.T[0]
            hnr = hnr[np.isfinite(hnr)]
            return float(np.mean(hnr)) if len(hnr) > 0 else 0.0
        except Exception:
            return 0.0
