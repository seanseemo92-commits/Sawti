"""Verify disjoint corpus partitioning and exact, integrity-checked reassembly."""
import copy
import hashlib
import json
import tempfile
import unittest
from pathlib import Path

from generate_habibi_tts import shard_items
from habibi_contract import DIALECT_IDS, load_catalog
from habibi_ci import checkpoint_ref, combine_shards


class HabibiShardingTest(unittest.TestCase):
    def test_every_real_corpus_key_belongs_to_exactly_one_worker(self):
        path = Path('app/build/speech_catalog.json')
        if not path.is_file():
            self.skipTest('Run ExportSpeechCatalogTest first')
        total = 0
        for dialect, items in load_catalog(path).items():
            shards = [shard_items(items, i, 6) for i in range(6)]
            flattened = [key for shard in shards for key in shard]
            self.assertEqual(set(items), set(flattened))
            self.assertEqual(len(items), len(flattened))
            self.assertLessEqual(max(map(len, shards)), 46)
            total += len(flattened)
        self.assertEqual(2399, total)
        for index, count in ((0, 0), (-1, 6), (6, 6)):
            with self.assertRaises(ValueError):
                shard_items({}, index, count)

    def test_references_are_limited_to_this_run_and_valid_dialects(self):
        self.assertEqual('habibi-checkpoints/123/libyan-0', checkpoint_ref('123', 'LIBYAN', 0))
        for args in (('../escape', 'LIBYAN', 0), ('123', 'UNKNOWN', 0), ('123', 'LIBYAN', -1)):
            with self.assertRaises(ValueError):
                checkpoint_ref(*args)

    def fixture(self):
        groups = {d: {'word.a': 'أمي', 'word.b': 'أبي'} for d in DIALECT_IDS}
        payload = b'unit-test-checksum-fixture' * 100
        manifests = []
        for dialect, words in groups.items():
            for index in range(2):
                rows = {key: {'file': key + '.mp3', 'speechText': value,
                              'sha256': hashlib.sha256(payload).hexdigest()}
                        for key, value in shard_items(words, index, 2).items()}
                manifests.append(dict(dialect=dialect, index=index, ref=f'{dialect}-{index}',
                                      data={'engine': 'Habibi-TTS', 'referenceSha256': 'same', 'entries': rows}))
        return groups, manifests, payload

    def test_complete_shards_assemble_once_with_exact_hashes_and_text(self):
        groups, manifests, payload = self.fixture()
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            self.assertEqual(18, combine_shards(groups, manifests, lambda *args: payload, root, 2))
            self.assertEqual(18, len(list(root.glob('*/*.mp3'))))
            for dialect in DIALECT_IDS:
                data = json.loads((root / dialect.lower() / 'manifest.json').read_text())
                self.assertEqual(set(groups[dialect]), set(data['entries']))

    def test_missing_duplicate_stale_and_corrupt_shards_cannot_make_an_apk_pack(self):
        groups, manifests, payload = self.fixture()
        damaged = []
        damaged.append(manifests[:-1])
        damaged.append(manifests + [manifests[0]])
        empty = copy.deepcopy(manifests)
        empty[0]['data']['entries'] = {}
        damaged.append(empty)
        stale = copy.deepcopy(manifests)
        stale[1]['data']['referenceSha256'] = 'different'
        damaged.append(stale)
        wrong_text = copy.deepcopy(manifests)
        next(iter(wrong_text[0]['data']['entries'].values()))['speechText'] = 'خطأ'
        damaged.append(wrong_text)
        unsafe = copy.deepcopy(manifests)
        next(iter(unsafe[0]['data']['entries'].values()))['file'] = '../escape.mp3'
        damaged.append(unsafe)
        for case in damaged:
            with self.subTest(case=damaged.index(case)), tempfile.TemporaryDirectory() as folder:
                with self.assertRaises(ValueError):
                    combine_shards(groups, case, lambda *args: payload, Path(folder), 2)
        with tempfile.TemporaryDirectory() as folder, self.assertRaises(ValueError):
            combine_shards(groups, manifests, lambda *args: payload + b'corrupt', Path(folder), 2)


if __name__ == '__main__':
    unittest.main()
