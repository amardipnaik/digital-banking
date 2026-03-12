# Module 02 Customer Management - Frontend UI Design

## Module Summary
Admin console UI for customer profile and KYC management, aligned with existing authentication tone and security flow.

## Route Plan
- `/admin/customers` - list, filters, and row actions
- `/admin/customers/:userId` - detail view, profile edit, KYC update, soft-delete

## API Ownership and Reuse
- New customer API client: `customerApi.js` for Module 02 endpoints only.
- Keep auth endpoints in existing `authApi.js`.
- Reuse `PATCH /api/admin/auth/users/{userId}/status` for ACTIVE/DISABLED actions.
- Reuse existing auth/session infrastructure: `src/lib/http.js`, `src/context/*`, `src/routes/ProtectedRoute.jsx`.

## UI Components
- Customer list table with server-side pagination and sorting.
- Filter bar: `search`, `kycStatus`, `userStatus`.
- Quick actions in list: view, disable/enable, soft-delete, restore.
- Customer detail card combining:
  - Profile section (editable fields from `customer_profiles`)
  - KYC action section (`PENDING`/`APPROVED`/`REJECTED`)
  - Account status action section (calls existing auth status endpoint)
- Verification assist section (trigger resend using existing auth verification API).
- Admin activity timeline panel.
- Soft-delete confirmation modal.

## Form Fields and Validation
- Editable profile fields: full name, DOB, address lines, city, state, postal code, country.
- Optional KYC fields (if approved in schema): government ID, government ID type, reviewer remarks.
- Validation mirrors backend constraints and shows API error messages from standard envelope.

## UX and State Handling
- Loading states for list/detail and per-action button busy state.
- Empty states for no data and no filter results.
- Inline error state + retry action.
- Success feedback pattern consistent with existing auth pages.
- Destructive actions (delete/restore/status/kyc) require confirmation dialog with action reason when applicable.

## Visual Tone and Consistency
- Keep typography, spacing, form controls, and action hierarchy aligned with current auth pages.
- Keep CTA wording and message style consistent with existing product tone.

## Accessibility Notes
- Keyboard-accessible table actions and dialogs.
- Proper label associations, focus management after modal close, and clear status text.
