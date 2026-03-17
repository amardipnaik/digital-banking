# Module 03 Account Management - Frontend UI Design

## Module Summary
Customer and admin UI for account lifecycle actions, aligned with existing authentication and customer-management design language.

## Route Plan
- `/accounts` - customer account list and create-account CTA
- `/accounts/new` - customer account creation form
- `/accounts/:accountId` - customer account detail (read-only lifecycle state)
- `/admin/accounts` - admin account list with filters and lifecycle actions
- `/admin/accounts/:accountId` - admin detail with timeline and status controls

## API Ownership and Reuse
- New Module 03 API client: `accountApi.js` for account endpoints only.
- Reuse auth/session stack from existing frontend modules (`http.js`, auth context, protected route).
- Reuse existing `me` and role data from auth module for access gating and UI personalization.
- Do not duplicate customer-profile APIs from Module 02.

## UI Components
- Account summary cards (account number mask, type, status, opened date).
- Account list table/grid with pagination and status/type filters.
- Create account form card.
- Admin action panel for `Approve/Freeze/Unfreeze/Close` transitions.
- Account lifecycle timeline component.
- Confirmation modal for admin lifecycle actions with reason field.

## Form Fields and Validation
- Create account fields: account type, preferred currency (if supported), declaration checkbox.
- Lifecycle action fields (admin): next status and mandatory reason for freeze/close.
- Client-side validation mirrors backend rules and shows standard API envelope errors.
- UI prevents actions when account is already `CLOSED`.

## UX and State Handling
- Loading states for list/detail and per-action busy indicators.
- Empty states for no accounts and no filter results.
- Inline error state with retry action.
- Success toasts/messages consistent with existing auth/customer pages.
- Hide or disable actions that violate allowed transition matrix.

## Visual Tone and Consistency
- Keep typography, spacing, forms, and button hierarchy aligned with current app pages.
- Preserve concise action text and confirmation patterns from previous modules.

## Accessibility Notes
- Keyboard-accessible table rows, menus, and confirmation dialogs.
- Proper label associations, focus return after modal close, and clear status badges.
