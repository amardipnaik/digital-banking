# Module 02 Customer Management - Frontend UI Design

## Module Summary
Admin console UI for customer lifecycle management and KYC operations.

## Pages / Routes
- `/admin/customers`
- `/admin/customers/add`
- `/admin/customers/edit/:id`
- `/admin/customers/:id`

## UI Components
- Customer list table with actions
- Search/filter controls
- Add/Edit customer form
- Customer detail/KYC action panel

## Form Fields and Validation
- Add: fullName, email, phoneNumber, dateOfBirth, address, governmentId, accountType.
- Edit: fullName, phoneNumber, address, accountStatus, kycStatus.

## UI States
- Loading: list and detail fetch states.
- Empty: no customers / no search results.
- Error: API error handling with retry.
- Success: toast notifications for create/update/status actions.

## Accessibility Notes
- Table actions keyboard reachable.
- Clear labels for status dropdowns and action buttons.
