"""Exercise real ffmpeg encoding, silence trimming and pre-PCM normalization."""
import importlib.util
import tempfile
import unittest
from pathlib import Path

from generate_habibi_tts import mp3_output
from verify_habibi_packs import inspect_mp3


@unittest.skipUnless(all(importlib.util.find_spec(name) for name in ('numpy', 'pydub', 'soundfile')),
                     'Install requirements-habibi.txt for real codec tests')
class HabibiAudioTest(unittest.TestCase):
    def test_outer_silence_trim_and_unclipped_mono_24khz_mp3(self):
        import numpy as np
        from pydub import AudioSegment
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / 'clip.mp3'
            tone = 3 * np.sin(2 * np.pi * 300 * np.arange(12000) / 24000)
            wave = np.concatenate((np.zeros(9600), tone, np.zeros(9600)))
            mp3_output(wave, 24000, path)
            inspect_mp3(path)
            audio = AudioSegment.from_mp3(path)
            self.assertLess(len(audio), 800)
            self.assertGreater(len(audio), 500)
            self.assertLess(audio.max_dBFS, 0)

    def test_invalid_or_silent_output_never_replaces_a_valid_clip(self):
        import numpy as np
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / 'clip.mp3'
            path.write_bytes(b'previous-clip')
            for wave in (np.zeros(24000), np.array([float('nan')]), np.zeros((2, 24000))):
                with self.assertRaises(ValueError):
                    mp3_output(wave, 24000, path)
                self.assertEqual(b'previous-clip', path.read_bytes())


if __name__ == '__main__':
    unittest.main()
