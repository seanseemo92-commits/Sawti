"""Record actual test/Lint results without inferring device or listening validation."""
import os
from pathlib import Path
import xml.etree.ElementTree as ET

reports = sorted(Path("app/build/test-results/testDebugUnitTest").glob("TEST-*.xml"))
counts = dict.fromkeys(("tests", "failures", "errors", "skipped"), 0)
for report in reports:
    suite = ET.parse(report).getroot()
    for key in counts:
        counts[key] += int(suite.get(key, 0))
    for case in suite.findall("testcase"):
        for failure in list(case.findall("failure")) + list(case.findall("error")):
            print(f"FAILED {case.get('classname')}.{case.get('name')}: {failure.get('message', '')}")
            print((failure.text or "")[:5000])
lint = Path("app/build/reports/lint-results-release.xml")
issues = ET.parse(lint).getroot().findall("issue") if lint.is_file() else []
lint_errors = sum(issue.get("severity") in ("Error", "Fatal") for issue in issues)
summary = (f"Unit test reports: {len(reports)}; tests: {counts['tests']}; "
           f"failures: {counts['failures']}; errors: {counts['errors']}; skipped: {counts['skipped']}.\n"
           f"Release Lint report present: {lint.is_file()}; errors/fatal: {lint_errors}.\n"
           "Device update/restore and auditory Quran/TTS verification: pending human verification.\n")
print(summary)
if os.environ.get("GITHUB_STEP_SUMMARY"):
    with open(os.environ["GITHUB_STEP_SUMMARY"], "a", encoding="utf-8") as output:
        output.write(summary)
if reports and (counts["failures"] or counts["errors"] or lint_errors):
    raise SystemExit(1)
