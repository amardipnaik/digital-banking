# Module 01 Authentication - Frontend UI Design

## Module Summary
React UI pages for sign-in, sign-up, and password reset flows.

## Pages / Routes
- `/login`
- `/register`
- `/forgot-password`

## UI Components
- Auth form container
- Input components for email/password/profile fields
- Validation and error message blocks

## Form Fields and Validation
- Login: email, password.
- Register: fullName, email, phoneNumber, dateOfBirth, address, password, confirmPassword.
- Forgot password: email.

## UI States
- Loading: disable submit and show spinner.
- Empty: initial form state.
- Error: inline field and server message.
- Success: redirect or confirmation toast/message.

## Accessibility Notes
- Keyboard navigation for all controls.
- Proper labels and descriptive error text for fields.
