# ADR-006 — Use a Central Design System and Reusable Compose Components

**Status:** Accepted  
**Date:** 23 July 2026  

## Context

The application contains many financial screens and repeated patterns.

Without a shared design system, colors, money formatting, cards, badges, and states may become inconsistent.

## Decision

Create a central design system module that owns:

- Colors.
- Typography.
- Spacing.
- Radius.
- Elevation.
- Icons.
- Money components.
- Person components.
- Transaction components.
- Empty, loading, error, and confirmation states.

## Consequences

- Consistent experience.
- Faster feature delivery.
- Easier RTL and theme support.
- More upfront design work.

## Guardrails

- Feature modules may compose shared components but must not redefine global tokens.
- Financial values use `MoneyText`.
- Financial input uses `MoneyInputField`.
- Status colors always include labels or icons.
