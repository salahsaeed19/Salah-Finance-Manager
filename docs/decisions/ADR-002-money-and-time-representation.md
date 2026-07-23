# ADR-002 — Money and Time Representation

**Status:** Accepted  
**Date:** 23 July 2026  

## Money Decision

Store monetary amounts as `Long` minor units.

Examples:

- 1.00 ILS = 100
- 1.50 ILS = 150
- 201.50 ILS = 20150

Never use `Float` or `Double` for financial amounts.

Use `BigDecimal` only for exchange-rate calculations and store the normalized rate as text.

## Time Decision

- Accounting dates: epoch day.
- Timestamps: epoch milliseconds.
- Preserve original imported date text.
- Store user time zone separately.
- Never silently repair suspicious imported dates.
