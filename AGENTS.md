# Agent Guidelines & Rules

## Core Rules

1. **No Useless or Redundant Information**:
    - Check the existing project files and active workspace context thoroughly *before* suggesting
      terminal commands or workflows.
    - Do not provide generic instructions or multi-platform alternatives (e.g., macOS vs Linux CLI
      commands) when the required output or file (like pre-encoded Base64 files) already exists in
      the repository.
    - Be direct, precise, and concise.

2. **Context Awareness**:
    - Always inspect project structure, existing configuration files, and active documents before
      proposing solutions.
    - Respect user preferences and direct feedback.

### File Maintenance & Guidelines Limit

- The `AGENTS.md` file must never exceed 200 lines, and this rule must always be
  placed at the very end of the file (and must not be placed below line 200). If
  a new rule needs to be added, concise existing content or remove less critical
  rules to remain strictly under the 200 line limit.
