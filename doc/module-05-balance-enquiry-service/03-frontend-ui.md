# Module 05 Balance Enquiry Service - Frontend UI Design

## Module Summary
Read-only balance and quick statement UI for customer and admin enquiry use-cases.

## Route Plan
- `/balances` - customer balance enquiry dashboard for owned accounts
- `/admin/balances` - admin balance enquiry dashboard for selected account/customer

## API Ownership and Reuse
- New `balanceApi.js` for balance enquiry endpoints.
- Reuse `accountApi.js` for account lookup context and dropdown population.
- Reuse existing auth/session infrastructure from Modules 01-04.
- Do not duplicate transaction posting APIs from Module 04.

## UI Components
- Account selector and summary card (account number, status, currency).
- Balance panel with `availableBalance` and `ledgerBalance`.
- Mini statement table (recent transactions only).
- Refresh action and last-updated indicator.
- Admin filter bar (search by account id/user id/reference).

## Form Fields and Validation
- Required account selection before enquiry call.
- For admin filters: numeric validation for account/user id fields.
- Client-side guard to prevent invalid/empty query payload.
- Read-only module: no mutation fields or destructive actions.

## UX and State Handling
- Loading skeleton for balance cards and mini statement table.
- Empty state when account has no transactions yet.
- Error state for unauthorized access or not-found account.
- Success state with timestamped snapshot.
- Manual refresh and optional auto-refresh strategy with safe interval.

## Visual Tone and Consistency
- Keep styling, spacing, and status pills aligned with existing app pages.
- Keep concise copy and clear currency formatting conventions.

## Accessibility Notes
- Keyboard-accessible selectors, filters, and refresh controls.
- Proper table semantics for statement rows and headers.
- Screen-reader friendly amount labels including debit/credit context.
