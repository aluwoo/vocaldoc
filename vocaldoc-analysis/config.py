"""Audio configuration for voice analysis service."""

from dataclasses import dataclass, field
from typing import Dict, List


@dataclass
class AudioConfig:
    """Configuration for audio processing parameters."""

    SAMPLING_RATE: int = 44100
    PITCH_FLOOR: float = 75
    PITCH_CEILING: float = 800
    TIME_STEP: float = 0.01
    MAX_DURATION: int = 120
    FORMANT_CEILING: Dict[str, int] = field(default_factory=lambda: {'child': 8000, 'female': 5000, 'male': 5500})
    JITTER_WINDOW: float = 0.0001
    JITTER_LENGTH: float = 0.02
    JITTER_FILTER: float = 1.3
    SUPPORTED_FORMATS: List[str] = field(default_factory=lambda: ['wav', 'flac', 'mp3', 'm4a'])


audio_config = AudioConfig()
