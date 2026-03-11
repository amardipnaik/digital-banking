# Module 01 Authentication - Frontend UI Design

## Module Summary
React UI for authentication and verification flows integrated with backend auth APIs.

## Routes
- `/` -> redirects to `/login`
- `/login` -> login form + inline forgot/reset password flow
- `/register` -> customer registration form
- `/me` -> authenticated profile + verification actions
- `/admin/user-status` -> admin-only user status controls

## Login Screen (`/login`)
- Fields: `loginId` (email/mobile), `password`
- Behavior:
  - Sends background `deviceId` automatically (not shown in UI).
  - On success, redirects to `/me`.
  - Shows inline API error message.
- Forgot Password:
  - `Forgot Password?` toggle reveals:
    - request reset token form
    - set new password form

## Register Screen (`/register`)
- Fields: fullName, email, mobileNumber, dateOfBirth, password, confirmPassword
- Success behavior:
  - does not show user ID
  - redirects user to `/login`

## Profile Screen (`/me`)
- Displays user identity and status with role/status badges.
- Shows verification state for email/mobile.
- If channel is not verified:
  - show `Request OTP`
  - show OTP input + `Verify` only after request is made
  - allow resend OTP

## Admin Screen (`/admin/user-status`)
- For admin role only.
- Updates target user status to `ACTIVE` or `DISABLED`.

## UX/Validation Notes
- Inputs have labels and inline validation requirements.
- Loading states disable action buttons.
- Errors use common API error message parser.
- Mobile responsive grid collapses to one column.
