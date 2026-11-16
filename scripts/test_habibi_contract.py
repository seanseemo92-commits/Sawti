#!/usr/bin/env python3
"""Model-independent checks; does not require Python/PyTorch model installation."""
import unittest

from habibi_contract import DIALECT_IDS, license_policy


class HabibiContractTest(unittest.TestCase):
    def test_nine_dialects(self):
        self.assertEqual(9, len(DIALECT_IDS))
        self.assertEqual("LBY", DIALECT_IDS["LIBYAN"])
        self.assertEqual("EGY", DIALECT_IDS["EGYPTIAN"])
        self.assertEqual("LEV", DIALECT_IDS["SYRIAN"])

    def test_model_licenses(self):
        self.assertEqual(("EGY", "Specialized", "Apache-2.0"),
                         license_policy("EGYPTIAN"))
        self.assertEqual(("LBY", "Unified", "CC-BY-NC-SA-4.0"),
                         license_policy("LIBYAN"))
        with self.assertRaises(ValueError):
            license_policy("LIBYAN", "specialized")


if __name__ == "__main__":
    unittest.main()
