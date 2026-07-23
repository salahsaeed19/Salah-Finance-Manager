# ADR-005 — Build Arabic RTL and English LTR from the Beginning

**Status:** Accepted  
**Date:** 23 July 2026  

## Context

Arabic is the default language, while English is also required.

Adding RTL support after the English interface is completed would create layout, navigation, mixed-text, and testing problems.

## Decision

Every feature must be implemented and tested in both Arabic RTL and English LTR in the same vertical slice.

## Consequences

### Positive

- No late RTL redesign.
- Equal product quality.
- Better mixed Arabic and English handling.
- Localization errors appear early.

### Negative

- Every screen requires two-language review.
- Screenshots and tests require more effort.

## Guardrails

- No hardcoded visible strings.
- All navigation icons are direction-aware.
- Account numbers remain LTR.
- Person names remain unchanged.
- Every completed feature includes Arabic and English screenshots.
