"""Regression checks for short-word timing, references and resumable caching."""
import json
import math
import tempfile
import types
import unittest
import subprocess
import sys
from pathlib import Path
from unittest.mock import Mock, patch

import generate_habibi_tts as gen


class HabibiGeneratorTest(unittest.TestCase):
    def test_pilot_cli_refuses_to_replace_the_production_pack(self):
        result = subprocess.run([sys.executable, str(Path(gen.__file__)),
                                 '--dialect', 'LIBYAN', '--semantic-key', 'family.mother'],
                                capture_output=True, text=True)
        self.assertEqual(2, result.returncode)
        self.assertIn('outside production assets', result.stderr)

    def test_strict_cleaning_without_changing_arabic_letters(self):
        self.assertEqual('نبي الحمام', gen.clean_for_habibi('  نِبِّيْ\n الحَمَّامـ  '))
        self.assertEqual('أمي بقرة نقعمز', gen.clean_for_habibi('أمي بقرة نقعمز'))
        for mark in [*(chr(n) for n in range(0x064B, 0x0653)), '\u0670', '\u0640']:
            self.assertEqual('كلمة', gen.clean_for_habibi('كل' + mark + 'مة'))
        for text in ('', '   ', 'عدد 12', 'عدد ١٢', 'عدد ۱۲', 'عدد １２'):
            with self.subTest(text=text), self.assertRaises(ValueError):
                gen.clean_for_habibi(text)

    def test_rejected_short_word_is_resynthesized_without_accepting_bad_audio(self):
        infer = Mock(return_value=([.1], 24000, None))
        with patch.object(gen, 'set_seed'), patch.object(gen, 'mp3_output',
                side_effect=[gen.GeneratedAudioError('too short'), None]):
            info = gen.synthesize_clip(infer, Path('unused.mp3'), 'أنام', 6.4)
        self.assertEqual(2, info['synthesisAttempts'])
        self.assertEqual(6.4, infer.call_args_list[0].args[0])
        self.assertGreaterEqual(infer.call_args_list[1].args[0], 6.9)

    def test_synthesis_retries_are_bounded_and_do_not_hide_disk_errors(self):
        infer = Mock(return_value=([.1], 24000, None))
        with patch.object(gen, 'set_seed'), patch.object(gen, 'mp3_output',
                side_effect=gen.GeneratedAudioError('silent')), self.assertRaises(gen.GeneratedAudioError):
            gen.synthesize_clip(infer, Path('unused.mp3'), 'أجلس', 6.4)
        self.assertEqual(3, infer.call_count)
        infer.reset_mock()
        with patch.object(gen, 'set_seed'), patch.object(gen, 'mp3_output',
                side_effect=OSError('disk full')), self.assertRaises(OSError):
            gen.synthesize_clip(infer, Path('unused.mp3'), 'أجلس', 6.4)
        self.assertEqual(1, infer.call_count)

    def test_dialect_names_habibi_tags_and_locales(self):
        for name, tag in gen.DIALECT_IDS.items():
            self.assertEqual(name, gen.dialect_arg(name.lower()))
            self.assertEqual(name, gen.dialect_arg(tag))
        self.assertEqual('LIBYAN', gen.dialect_arg('ar-LY'))
        self.assertEqual('ALL', gen.dialect_arg('all'))
        with self.assertRaises(gen.argparse.ArgumentTypeError):
            gen.dialect_arg('UNKNOWN')

    def test_speed_only_slows_short_single_words(self):
        self.assertEqual(.75, gen.speed_for_text('أمي'))
        self.assertEqual(.75, gen.speed_for_text('الحمام'))
        self.assertEqual(.88, gen.speed_for_text('نبي مية'))
        self.assertEqual(.88, gen.speed_for_text('المستشفيات'))

    def test_duration_reserves_generation_frames_for_words_and_requests(self):
        prompt = 'هذا نص المرجع.'
        for sample_rate in (24000, 44100):
            for text in ('أب', 'أمي', 'أختي', 'الحمام', 'نبي مية', 'عدل لي المخدة'):
                with self.subTest(text=text, sample_rate=sample_rate):
                    frames = sample_rate * 6
                    speed = gen.speed_for_text(text)
                    duration = gen.synthesis_duration(frames, sample_rate, prompt, text, speed)
                    ref = math.ceil(frames * 24000 / sample_rate) // 256
                    generated = int(duration * 24000 / 256) - ref
                    original = int(ref / len((prompt + ' ').encode()) * len(text.encode()) / speed)
                    self.assertGreaterEqual(generated, original * 2)
                    minimum = gen.MIN_WORD_SECONDS if len(text.split()) == 1 else gen.MIN_PHRASE_SECONDS
                    self.assertGreaterEqual(generated * 256 / 24000, minimum)

    def test_rejected_request_gets_more_model_time_too(self):
        infer = Mock(return_value=([.1], 24000, None))
        with patch.object(gen, 'set_seed'), patch.object(gen, 'mp3_output',
                side_effect=[gen.GeneratedAudioError('incomplete request'), {'decodedDurationSeconds': 1.4}]):
            info = gen.synthesize_clip(infer, Path('unused.mp3'), 'نبي الحمام', 8.)
        self.assertEqual(2, info['synthesisAttempts'])
        self.assertGreater(infer.call_args_list[1].args[0], infer.call_args_list[0].args[0])
        self.assertEqual(1.4, info['decodedDurationSeconds'])

    def test_pilot_selection_rejects_missing_keys_before_any_inference(self):
        groups = {'LIBYAN': {'family.mother': 'أمي', 'family.father': 'بوي'}}
        self.assertEqual({'LIBYAN': {'family.mother': 'أمي'}},
                         gen.selected_items(groups, ['LIBYAN'], ['family.mother']))
        with self.assertRaises(ValueError):
            gen.selected_items(groups, ['LIBYAN'], ['family.mother', 'not.a.card'])

    def test_missing_reference_falls_back_to_official_matching_pair(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            assets = root / 'assets'
            assets.mkdir()
            (assets / 'MSA.mp3').write_bytes(b'official-reference')
            mapping = {d: {'ref_audio': None, 'ref_text': None} for d in gen.DIALECT_IDS}
            mapping['LIBYAN'] = {'ref_audio': 'missing.wav', 'ref_text': 'صوت مرجعي'}
            config = root / 'ref_config.json'
            config.write_text(json.dumps({'schema_version': 1, 'dialects': mapping}))
            with patch.object(gen, 'files', return_value=root), self.assertWarns(UserWarning):
                audio, text, source = gen.load_references(config, 'LIBYAN')
            self.assertEqual(str(assets / 'MSA.mp3'), audio)
            self.assertEqual(gen.REFERENCE_TEXT, text)
            self.assertEqual('official-MSA-fallback', source)
            mapping['LIBYAN']['ref_text'] = None
            config.write_text(json.dumps({'schema_version': 1, 'dialects': mapping}))
            with self.assertRaises(ValueError):
                gen.load_references(config, 'LIBYAN')

    def test_resumption_preserves_later_cached_entries_and_rejects_corruption(self):
        # Inference is injected to exercise persistence/error paths independently
        # from model quality. A separate real-model smoke run tests the API.
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            reference = root / 'ref.wav'
            reference.write_bytes(b'ref')
            args = types.SimpleNamespace(ref_config=root / 'config.json', output=root / 'out',
                                         skip_existing=True, max_new=0, deadline=math.inf, device='cpu',
                                         checkpoint_run_id='123', checkpoint_every=2, catalog=root/'catalog.json',
                                         shard_index=0, shard_count=1)
            grouped = {'LIBYAN': {'a': 'أمي', 'b': 'بقرة', 'c': 'نبي الحمام'}}
            infer = Mock(return_value=([.1], 24000, None))
            modules = {
                'f5_tts.infer.utils_infer': types.SimpleNamespace(
                    preprocess_ref_audio_text=lambda *pair: pair),
                'habibi_tts.infer.utils_infer': types.SimpleNamespace(infer_process=infer),
                'habibi_tts.model.utils': types.SimpleNamespace(dialect_id_map={'LBY': '⑫'}),
                'torchaudio': types.SimpleNamespace(load=lambda _: (types.SimpleNamespace(shape=(1, 144000)), 24000)),
            }
            def encode(wave, rate, path, **kwargs):
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_bytes(b'generated' * 200)
            with patch.dict('sys.modules', modules), \
                 patch.object(gen, 'load_references', return_value=(str(reference), 'نص مرجعي.', 'custom')), \
                 patch.object(gen, 'version', return_value='test'), \
                 patch.object(gen, 'set_seed'), patch.object(gen, 'mp3_output', side_effect=encode), \
                 patch.object(gen.subprocess, 'run') as checkpoint:
                gen.generate_one_dialect(args, 'LIBYAN', grouped, None, None, 'vocos')
                self.assertEqual(3, infer.call_count)
                checkpoint.assert_called_once()
                self.assertIn('123', checkpoint.call_args.args[0])
                checkpoint.reset_mock()
                self.assertEqual('أمي', infer.call_args_list[0].args[2])
                self.assertEqual(.75, infer.call_args_list[0].kwargs['speed'])
                self.assertEqual('⑫', infer.call_args_list[0].kwargs['dialect_id'])
                self.assertIsNotNone(infer.call_args_list[0].kwargs['fix_duration'])
                infer.reset_mock()
                gen.generate_one_dialect(args, 'LIBYAN', grouped, None, None, 'vocos')
                infer.assert_not_called()
                checkpoint.assert_not_called()
                # Damage an early clip, then crash during its regeneration.
                (args.output / 'libyan/a.mp3').write_bytes(b'corrupt')
                infer.side_effect = RuntimeError('interrupted')
                with self.assertRaises(RuntimeError):
                    gen.generate_one_dialect(args, 'LIBYAN', grouped, None, None, 'vocos')
                manifest = args.output / 'libyan/manifest.json'
                self.assertEqual({'b', 'c'}, set(json.loads(manifest.read_text())['entries']))
                infer.side_effect = None
                infer.reset_mock()
                args.max_new = 1
                gen.generate_one_dialect(args, 'LIBYAN', grouped, None, None, 'vocos')
                self.assertEqual(1, infer.call_count)
                self.assertEqual({'a', 'b', 'c'}, set(json.loads(manifest.read_text())['entries']))
                # A text change invalidates only the affected clip.
                infer.reset_mock()
                grouped['LIBYAN']['c'] = 'نبي نشرب مية'
                gen.generate_one_dialect(args, 'LIBYAN', grouped, None, None, 'vocos')
                self.assertEqual(1, infer.call_count)
                self.assertEqual('نبي نشرب مية', infer.call_args.args[2])
                # Old pipeline outputs cannot be silently accepted as repaired.
                document = json.loads(manifest.read_text())
                document['pipelineVersion'] = 3
                manifest.write_text(json.dumps(document))
                infer.reset_mock()
                args.max_new = 0
                gen.generate_one_dialect(args, 'LIBYAN', grouped, None, None, 'vocos')
                self.assertEqual(3, infer.call_count)


if __name__ == '__main__':
    unittest.main()
