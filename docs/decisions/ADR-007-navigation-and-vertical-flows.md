# ADR-007 — Use Five Primary Destinations and Vertical User Flows

**Status:** Accepted  
**Date:** 23 July 2026  

## Decision

Use five bottom navigation destinations:

1. Home
2. Transactions
3. People
4. Plans
5. Settings

Use a central floating action button for common creation actions.

Each feature must be delivered as a full vertical user flow.

## Rationale

The selected structure balances:

- Frequent financial review.
- People management.
- Monthly planning.
- Fast transaction creation.
- Clear navigation.

## Guardrails

- Bottom destinations preserve state.
- No feature is marked complete with backend-only work.
- Unsaved financial forms require exit confirmation.
- Navigation labels remain visible.
