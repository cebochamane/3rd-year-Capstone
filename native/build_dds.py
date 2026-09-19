#!/usr/bin/env python3
"""Build bundled, unmodified DDS 2.9.0 and its small command-line adapter.

Requires Python 3, a C++11 compiler and ar (macOS/Linux). No downloads occur.
"""
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path
import os
import shutil
import subprocess

root = Path(__file__).resolve().parent
compiler = os.environ.get("CXX") or shutil.which("c++")
if not compiler:
    raise SystemExit("A C++ compiler is required; on macOS install Xcode Command Line Tools.")
build = root / "build"
output = root / "bin"
build.mkdir(exist_ok=True)
output.mkdir(exist_ok=True)
sources = sorted((root / "vendor/dds-2.9.0/src").glob("*.cpp"))

def compile_source(source):
    obj = build / (source.stem + ".o")
    subprocess.run([compiler, "-O3", "-std=c++11", "-pthread", "-c", str(source), "-o", str(obj)], check=True)
    return obj

with ThreadPoolExecutor(max_workers=4) as pool:
    objects = list(pool.map(compile_source, sources))
archive = build / "libdds.a"
# Rebuild the archive from precisely this release's objects.
if archive.exists():
    archive.unlink()
subprocess.run(["ar", "rcs", str(archive), *map(str, objects)], check=True)
executable = output / "bridgetut-dds"
subprocess.run([compiler, "-O3", "-std=c++11", "-pthread", str(root / "dds_cli.cpp"),
                str(archive), "-o", str(executable)], check=True)
print("Built", executable)
