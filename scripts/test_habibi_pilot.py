"""Detect the duplicate decoded-audio failure seen in the reported video."""
import importlib.util
import unittest

from review_habibi_pilot import similarity


@unittest.skipUnless(all(importlib.util.find_spec(name) for name in ('numpy', 'scipy')),
                     'Install numpy/scipy for waveform regression')
class HabibiPilotTest(unittest.TestCase):
    def test_same_audio_with_gain_and_small_offset_is_still_duplicate(self):
        import numpy as np
        rng = np.random.default_rng(0)
        wave = rng.normal(size=8000)
        shifted = np.concatenate((np.zeros(100), wave * .3))
        self.assertGreater(similarity(wave, shifted), .995)

    def test_distinct_recordings_do_not_match(self):
        import numpy as np
        rng = np.random.default_rng(0)
        self.assertLess(similarity(rng.normal(size=8000), rng.normal(size=8000)), .1)


if __name__ == '__main__':
    unittest.main()
