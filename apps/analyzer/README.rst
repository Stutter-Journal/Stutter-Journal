================
Eloquia Analyzer
================

Python utilities for analysis and reporting workflows used by the Stutter Journal platform.

Prerequisites
=============

- Python 3.13+
- `uv` package manager

Setup
=====

Create a virtual environment and sync dependencies::

    cd apps/analyzer
    uv venv
    uv sync

Run tests::

    uv run pytest

Lint and format::

    uvx ruff check
    uvx ruff format
