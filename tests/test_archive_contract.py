"""Regression checks for the archived Android project before compilation."""
from pathlib import Path
import re
import unittest
import zipfile

ARCHIVE = Path(__file__).resolve().parents[1] / "MyWords.zip"
DB = "MyWords/app/src/main/java/com/mywords/data/db/AppDatabase.kt"

class ArchiveContractTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        with zipfile.ZipFile(ARCHIVE) as z:
            cls.paths = set(z.namelist())
            cls.database = z.read(DB).decode("utf-8")

    def test_gradle_project_present(self):
        self.assertIn("MyWords/settings.gradle.kts", self.paths)
        self.assertIn("MyWords/app/build.gradle.kts", self.paths)

    def test_no_destructive_migration(self):
        self.assertNotIn("fallbackToDestructiveMigration(", self.database)

    def test_required_migrations(self):
        self.assertRegex(self.database, r"Migration\\(1,\\s*2\\)")
        self.assertRegex(self.database, r"Migration\\(2,\\s*3\\)")

    def test_preserve_legacy_tables(self):
        # Deliberately fails on an unsafe source ZIP; migration should be reviewed.
        for name in ("family_members", "voice_recordings", "pain_reports"):
            self.assertNotRegex(
                self.database,
                rf"(?i)DROP\\s+TABLE\\s+(?:IF\\s+EXISTS\\s+)?{name}\\b",
                f"Legacy table {name} must not be dropped without verified preservation",
            )

if __name__ == "__main__":
    unittest.main()
