# Signup Flow Implementation

This document describes the signup flow implementation for the ReportViolation Android app.

## Overview

The signup flow consists of two main screens:
1. **SignupScreen** - Collects user details (name, email, phone number)
2. **OtpVerificationScreen** - Verifies OTP and completes registration

## Features

### SignupScreen
- **Input Fields:**
  - Full Name (letters and spaces only, 2-50 characters)
  - Email Address (valid email format)
  - Phone Number (10 digits only)
- **Validation:** Real-time validation with error messages
- **Navigation:** Proceeds to OTP verification when form is valid

### OtpVerificationScreen
- **OTP Input:** 6-digit numeric OTP
- **Phone Display:** Shows the phone number where OTP was sent
- **Resend OTP:** Available after 30-second cooldown
- **Change Details:** Option to go back and modify signup information
- **Navigation:** Proceeds to Dashboard on successful verification

## Implementation Details

### Files Created/Modified

#### New Files:
- `SignupScreen.kt` - Main signup UI
- `SignupViewModel.kt` - Signup state management and validation
- `OtpVerificationScreen.kt` - OTP verification UI
- `OtpVerificationViewModel.kt` - OTP verification state management
- `UserRegistrationService.kt` - User registration business logic
- `AppModule.kt` - Dependency injection setup
- `SignupData.kt` - Data class for signup information

#### Modified Files:
- `AppNavigation.kt` - Added navigation routes for signup flow
- `LoginScreen.kt` - Updated to navigate to new signup screen
- `ReportViolationApp.kt` - Initialize dependency injection

### Key Features

1. **Form Validation:**
   - Real-time validation with immediate feedback
   - Comprehensive error messages
   - Input filtering (e.g., phone numbers only accept digits)

2. **State Management:**
   - Uses Kotlin Flow for reactive state management
   - Proper loading states and error handling
   - Navigation state management

3. **Data Persistence:**
   - User data stored in Room database
   - Login state managed via SharedPreferences
   - Default language set to English on registration

4. **Navigation:**
   - Clean navigation flow with proper back stack management
   - Data passing between screens via navigation arguments
   - Proper screen transitions

5. **Dependency Injection:**
   - Simple DI setup for services
   - Proper separation of concerns
   - Testable architecture

## Usage

### Starting the Signup Flow
```kotlin
// From LoginScreen
navController.navigate(Screen.Signup.route)
```

### Navigation Flow
1. LoginScreen → SignupScreen
2. SignupScreen → OtpVerificationScreen (with user data)
3. OtpVerificationScreen → Dashboard (on successful verification)

### Data Flow
1. User enters details in SignupScreen
2. Data is validated and passed to OtpVerificationScreen
3. OTP verification triggers user registration
4. User is logged in and language is set to English
5. Navigation to Dashboard

## Testing

Unit tests are included for:
- Form validation logic
- State management
- Error handling

Run tests with:
```bash
./gradlew test
```

## Future Enhancements

1. **Real OTP Integration:** Replace simulated OTP with actual SMS service
2. **Enhanced Validation:** Add server-side validation
3. **Biometric Authentication:** Add fingerprint/face unlock options
4. **Social Login:** Integrate Google, Facebook login
5. **Email Verification:** Add email verification step
6. **Profile Completion:** Add profile setup after registration

## Dependencies

The implementation uses:
- Jetpack Compose for UI
- Kotlin Flow for state management
- Room for data persistence
- Navigation Compose for navigation
- Material 3 for theming
- Coroutines for async operations

## Notes

- The current implementation simulates OTP sending/verification
- User registration sets the default language to English as requested
- The app follows Material 3 design guidelines
- All screens are responsive and handle different screen sizes
- Error handling is comprehensive with user-friendly messages

