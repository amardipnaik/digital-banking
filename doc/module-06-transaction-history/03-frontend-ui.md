# Module 06 Transaction History - Frontend UI Design

## Module Summary
History UI for browsing and inspecting ledger transactions with filters and detail drill-down.

## Route Plan
- `/transactions/history` - customer history list with filters and pagination
- `/transactions/history/:transactionRef` - customer transaction detail view
- `/admin/transactions/history` - admin history explorer
- `/admin/transactions/history/:transactionRef` - admin detail view

## API Ownership and Reuse
- New `transactionHistoryApi.js` for history list/detail endpoints.
- Reuse `transactionApi.js` for operation context links where needed.
- Reuse auth/account context from existing modules; no duplicate ownership logic in UI.

## UI Components
- Filter bar: account selector, date range, type, side, status, reference search.
- Paginated table with sortable columns (`createdAt`, `amount`, `type`).
- Transaction detail panel with transfer/reversal linkage section.
- Export trigger placeholder (if enabled later) with same filters.
- Empty-state and retry components.

## Form Fields and Validation
- Validate date-range input (`from <= to`) before request.
- Validate pagination size and keep within allowed bounds.
- Optional transactionRef search field with trimming and length validation.
- Read-only fields in detail view; no mutation controls.

## UX and State Handling
- Loading skeleton for table and detail pane.
- Empty state for no matching records under selected filters.
- Inline recoverable error state for invalid query and transient failures.
- Persist last-used filters in component state for smoother navigation.
- Detail-page back navigation preserves list filters/page where possible.

## Visual Tone and Consistency
- Keep history table and badges aligned with existing account/transaction pages.
- Keep terminology consistent with Module 04 response fields.

## Accessibility Notes
- Keyboard-accessible filter controls, table navigation, and pagination buttons.
- Semantic table markup with header associations.
- Accessible status badges and readable amount formatting.
