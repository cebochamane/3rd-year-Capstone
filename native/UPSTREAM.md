# DDS provenance

DDS by Bo Haglund and Soren Hein, version 2.9.0, Apache License 2.0.
Source: https://github.com/dds-bridge/dds/tree/v2.9.0
Archive: https://codeload.github.com/dds-bridge/dds/tar.gz/refs/tags/v2.9.0
Archive SHA-256: 9ef36d8c36bf697ba3b499fcb9dca51a4b423278ac72e947235ac86f0b5fc38a

The vendor directory contains unmodified upstream src/, include/, LICENSE,
README.md and ChangeLog. The build script and dds_cli.cpp are new integration
files. Preserve the upstream copyright notices and LICENSE when sharing.
The Java application invokes a separate native executable through standard
input/output. There are no Maven dependency changes and no runtime downloads.

API documentation:
https://github.com/dds-bridge/dds/blob/v2.9.0/doc/dll-description.md

SolveBoard uses target=-1, solutions=1, mode=1. Its best score is for the side
whose turn it is. The Java adapter converts this to declarer's partnership.
The current partial trick is included; cards already played are excluded from
remaining hand masks. Analysis uses perfect information and optimal play on
both sides. Normal concessions are voluntary and do not require proving that
the conceding side cannot win a trick.
