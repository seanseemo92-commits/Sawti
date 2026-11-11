import io
import unittest
from zipfile import ZipFile
from review_apk_resources import VEHICLES, verify_transport_photos


class ApkResourceVerificationTest(unittest.TestCase):
    def fixture(self, optimized):
        data = io.BytesIO()
        lines = []
        with ZipFile(data, 'w') as archive:
            for index, vehicle in enumerate(VEHICLES):
                path = f'res/{index}.webp' if optimized else f'res/drawable-v4/word_transport_{vehicle}.webp'
                archive.writestr(path, b'RIFF\x04\x00\x00\x00WEBP')
                lines += [f'    resource 0x7f0800{index:02x} com.sawti:drawable/word_transport_{vehicle}',
                          f'      () (file) {path} type=WEBP']
        data.seek(0)
        return data, '\n'.join(lines)

    def test_qualified_drawable_directories_resolve(self):
        data, dump = self.fixture(False)
        with ZipFile(data) as archive:
            self.assertEqual(12, len(verify_transport_photos(archive, dump)))

    def test_optimized_file_paths_resolve_through_logical_names(self):
        data, dump = self.fixture(True)
        with ZipFile(data) as archive:
            self.assertEqual(12, len(verify_transport_photos(archive, dump)))

    def test_present_file_without_logical_identity_is_rejected(self):
        data, dump = self.fixture(True)
        with ZipFile(data) as archive, self.assertRaisesRegex(AssertionError, 'Missing logical'):
            verify_transport_photos(archive, dump.replace('drawable/word_transport_car', 'drawable/unrelated'))

    def test_missing_apk_file_is_rejected(self):
        data, dump = self.fixture(True)
        with ZipFile(data) as archive, self.assertRaisesRegex(AssertionError, 'Missing packaged'):
            verify_transport_photos(archive, dump.replace('res/0.webp', 'res/missing.webp'))


if __name__ == '__main__':
    unittest.main()
