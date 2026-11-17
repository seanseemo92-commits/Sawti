"""Contract regressions for real exported corpus and tampered inputs."""
import copy
import json
import tempfile
import unittest
from pathlib import Path

from habibi_contract import DIALECT_IDS, EXPECTED_BY_DIALECT, load_catalog


class HabibiContractTest(unittest.TestCase):
    def test_exact_official_dialect_map(self):
        self.assertEqual({
            'LIBYAN': 'LBY', 'TUNISIAN': 'TUN', 'ALGERIAN': 'ALG',
            'MOROCCAN': 'MAR', 'EGYPTIAN': 'EGY', 'SYRIAN': 'LEV',
            'IRAQI': 'IRQ', 'SAUDI': 'SAU', 'STANDARD': 'MSA',
        }, DIALECT_IDS)

    def test_actual_kotlin_export_and_rejected_corruption(self):
        path = Path('app/build/speech_catalog.json')
        if not path.is_file():
            self.skipTest('Run Kotlin ExportSpeechCatalogTest to validate production corpus')
        source = json.loads(path.read_text())
        groups = load_catalog(path)
        self.assertEqual(EXPECTED_BY_DIALECT, {k: len(v) for k, v in groups.items()})
        self.assertEqual(391, len(set().union(*(set(g) for g in groups.values()))))
        for replacement in ('نُطق', 'كلمـة', 'رقم 2', 'رقم ٣', 'رقم ۴', 'رقم ４', '', ' كلمة', 'كلمتين  هنا'):
            with self.subTest(replacement=replacement):
                bad = copy.deepcopy(source)
                bad['entries'][0]['speechText'] = replacement
                self.assert_rejected(bad)
        for field, value in (('semanticKey', '../escape'), ('dialect', 'UNKNOWN')):
            bad = copy.deepcopy(source)
            bad['entries'][0][field] = value
            self.assert_rejected(bad)
        bad = copy.deepcopy(source)
        bad['entries'].append(bad['entries'][0])
        self.assert_rejected(bad)
        # Same total and union cardinality, but a card removed from one dialect
        # and inserted into another: global counts alone must not pass.
        bad = copy.deepcopy(source)
        row = bad['entries'].pop(0)
        next_dialect = next(d for d in DIALECT_IDS if d != row['dialect'])
        row = {**row, 'dialect': next_dialect, 'semanticKey': 'test.unique'}
        bad['entries'].append(row)
        self.assert_rejected(bad)

    def assert_rejected(self, data):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / 'catalog.json'
            path.write_text(json.dumps(data))
            with self.assertRaises(ValueError):
                load_catalog(path)


if __name__ == '__main__':
    unittest.main()
