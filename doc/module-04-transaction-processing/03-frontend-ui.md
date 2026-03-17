# Module 04 Transaction Processing - Frontend UI Design

## Module Summary
Customer and admin transaction posting UI aligned with existing app tone and security flow.

## Route Plan
- `/transactions` - customer transaction workspace (deposit/withdraw/transfer)
- `/transactions/new` - optional dedicated create transaction screen
- `/admin/transactions` - admin operational posting and review panel

## API Ownership and Reuse
- New API client: `transactionApi.js` for Module 04 endpoints only.
- Reuse `accountApi.js` for account lookup context where needed.
- Reuse existing auth/session layers (`http.js`, auth context, protected routes).
- Do not duplicate account/customer APIs from Modules 02/03.

## UI Components
- Transaction type segmented control (`DEPOSIT`, `WITHDRAWAL`, `TRANSFER`).
- Source/target account selectors with role-based visibility.
- Amount and remarks form with inline validation.
- Submission confirmation panel showing projected effect.
- Result card with transaction references and status.
- Admin-only adjustment/reversal form section.

## Form Fields and Validation
- `accountId` required for all transaction types.
- `targetAccountId` required for transfer and must differ from source.
- `amount` required, positive, and precision-limited.
- `currencyCode` defaults from account context and must match posting rules.
- `idempotencyKey` generated client-side per submit action (hidden from user in standard flow).

## UX and State Handling
- Loading states for account dropdowns and submit action.
- Disabled submit while request is in-flight to reduce duplicate posts.
- Structured error display (insufficient funds, account blocked, duplicate key).
- Success state shows transaction references and next-step actions.
- Clear warning state for irreversible postings and admin adjustments.

## Visual Tone and Consistency
- Keep typography, spacing, and action hierarchy consistent with Modules 01-03 pages.
- Use existing pill/status patterns and confirmation style.

## Accessibility Notes
- Keyboard-navigable segmented controls, forms, and dialogs.
- Explicit labels and helper text for all monetary fields.
- ARIA live region for success/error transaction outcome messages.
