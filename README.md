# Traffic Violation Reporting App

A comprehensive Android application that enables citizens to report traffic violations through photo/video evidence, GPS location, and violation categorization. The app creates a collaborative platform between citizens and traffic police departments to reduce violations, increase awareness, and improve road safety through community participation.

## 🚀 Features

### Core Features
- **Violation Reporting**: Capture photos/videos with GPS location and violation categorization
- **Duplicate Detection**: Intelligent algorithm to detect and group similar reports
- **Jurisdiction Management**: City-wise segregation with pincode-based routing
- **Reward System**: Point-based rewards for verified reports
- **Offline Support**: Store reports locally when network is unavailable
- **Real-time Location**: GPS tracking with 3-meter accuracy

### Violation Categories
- Wrong-side driving
- No parking zone violations
- Signal jumping
- Speed violations
- Helmet/seatbelt violations
- Mobile phone usage while driving
- Lane cutting
- Drunk driving (suspected)
- Others

### User Management
- Phone number-based authentication with OTP
- City registration and jurisdiction validation
- Multi-city access for authorized users
- Anonymous reporting option
- User profile and reporting history

## 🏗️ Architecture

The app follows the **MVVM (Model-View-ViewModel)** architecture pattern with the following components:

### Data Layer
- **Room Database**: Local storage for reports, users, and sessions
- **Repository Pattern**: Centralized data access with offline-first approach
- **Type Converters**: Custom converters for LocalDateTime and List<String>

### Domain Layer
- **Services**: Duplicate detection, jurisdiction validation, reward calculation
- **Use Cases**: Business logic for violation reporting and user management

### Presentation Layer
- **Jetpack Compose**: Modern UI toolkit for native Android development
- **Navigation**: Type-safe navigation with Compose Navigation
- **ViewModel**: State management and business logic
- **Hilt**: Dependency injection for clean architecture

### Key Components

#### Database Schema
```kotlin
// ViolationReport Entity
@Entity(tableName = "violation_reports")
data class ViolationReport(
    val id: Long,
    val reporterId: String,
    val violationType: ViolationType,
    val latitude: Double,
    val longitude: Double,
    val timestamp: LocalDateTime,
    val status: ReportStatus,
    // ... other fields
)

// User Entity
@Entity(tableName = "users")
data class User(
    val id: String,
    val phoneNumber: String,
    val registeredCity: String,
    val totalPoints: Int,
    // ... other fields
)
```

#### Duplicate Detection Algorithm
- **Location-based**: Groups reports within 50-meter radius
- **Time-based**: Considers reports within 30-minute window
- **Vehicle matching**: License plate recognition and comparison
- **Confidence scoring**: 0-100% similarity with configurable thresholds

#### Jurisdiction Management
- **Pincode validation**: GPS coordinates converted to pincode
- **City boundaries**: Users restricted to registered city limits
- **Auto-routing**: Reports automatically assigned to respective police departments
- **Multi-city access**: Guest mode for travelers

## 🛠️ Technical Stack

### Core Technologies
- **Kotlin**: Primary programming language
- **Jetpack Compose**: Modern UI toolkit
- **Room**: Local database with SQLite
- **Hilt**: Dependency injection
- **Coroutines**: Asynchronous programming
- **Navigation Compose**: Type-safe navigation

### Libraries & Dependencies
- **CameraX**: Camera functionality
- **Google Maps**: Location services and mapping
- **Retrofit**: Network communication
- **Firebase**: Backend services (Auth, Firestore, Storage)
- **ML Kit**: Text recognition for license plates
- **Work Manager**: Background processing
- **DataStore**: Preferences storage

### Permissions Required
- Camera access for photo/video capture
- Location access for GPS coordinates
- Internet access for data synchronization
- Storage access for media files

## 📱 Screens

1. **Splash Screen**: App logo and loading
2. **Login Screen**: Phone number and OTP verification
3. **Registration Screen**: User registration with city selection
4. **Dashboard**: Main interface with quick actions
5. **Report Violation**: Violation reporting interface
6. **Camera Screen**: Photo/video capture
7. **Reports History**: View submitted reports
8. **Rewards Screen**: Points and redemption

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 26 (API level 26) or higher
- Google Maps API key
- Firebase project setup

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/traffic-violation-app.git
   cd traffic-violation-app
   ```

2. **Configure API Keys**
   - Add your Google Maps API key in `AndroidManifest.xml`
   - Configure Firebase project and add `google-services.json`

3. **Build and Run**
   ```bash
   ./gradlew build
   ./gradlew installDebug
   ```

### Configuration

#### Google Maps API Key
Replace `YOUR_MAPS_API_KEY` in `AndroidManifest.xml`:
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="your_actual_api_key_here" />
```

#### Firebase Setup
1. Create a Firebase project
2. Enable Authentication, Firestore, and Storage
3. Download `google-services.json` and place it in the `app/` directory

## 📊 Database Schema

### Violation Reports Table
- Primary key: `id` (auto-increment)
- Reporter information: `reporterId`, `reporterPhone`, `reporterCity`
- Violation details: `violationType`, `severity`, `description`, `timestamp`
- Location: `latitude`, `longitude`, `address`, `pincode`, `city`
- Vehicle info: `vehicleNumber`, `vehicleType`, `vehicleColor`
- Media: `photoUri`, `videoUri`, `mediaMetadata`
- Status: `status`, `isDuplicate`, `duplicateGroupId`, `confidenceScore`
- Review: `reviewerId`, `reviewTimestamp`, `reviewNotes`, `challanIssued`
- Rewards: `pointsAwarded`, `isFirstReporter`

### Users Table
- Primary key: `id` (user ID)
- Basic info: `phoneNumber`, `name`, `email`
- Location: `registeredCity`, `registeredPincode`, `registeredDistrict`
- Verification: `isPhoneVerified`, `isIdentityVerified`
- Rewards: `totalPoints`, `pointsEarned`, `pointsRedeemed`
- Statistics: `reportsSubmitted`, `reportsApproved`, `accuracyRate`
- Settings: `isAnonymousMode`, `notificationEnabled`
- Multi-city: `authorizedCities`, `isGuestUser`

## 🔧 Development

### Project Structure
```
app/src/main/java/com/example/reportviolation/
├── data/
│   ├── local/
│   │   ├── dao/
│   │   ├── converter/
│   │   └── AppDatabase.kt
│   ├── model/
│   ├── repository/
│   └── remote/
├── domain/
│   ├── service/
│   └── usecase/
├── ui/
│   ├── screens/
│   ├── navigation/
│   └── theme/
├── di/
└── ReportViolationApp.kt
```

### Key Classes

#### ViolationReportRepository
- Handles all data operations for violation reports
- Implements duplicate detection logic
- Manages offline storage and synchronization

#### DuplicateDetectionService
- Location-based similarity calculation
- Time-based grouping
- Vehicle matching algorithms
- Confidence scoring system

#### JurisdictionService
- City boundary validation
- Pincode-based routing
- Multi-city access management

### Testing
```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## 📈 Performance Requirements

- **App Launch Time**: Under 3 seconds
- **Photo/Video Upload**: Under 10 seconds on 4G
- **Location Accuracy**: 3-meter radius, 95% reliability
- **Offline Storage**: 50+ reports locally
- **Battery Optimization**: Background processing limitations

## 🔒 Security & Privacy

- **Data Encryption**: End-to-end encryption for sensitive data
- **Privacy Compliance**: GDPR and local data protection laws
- **User Anonymity**: Option to submit anonymous reports
- **Evidence Protection**: Watermarking, tamper detection

## 🚧 Roadmap

### Phase 1 - MVP (Current)
- ✅ Basic photo reporting
- ✅ GPS location capture
- ✅ Simple violation categories
- ✅ City-wise jurisdiction validation
- ✅ Basic duplicate detection
- ✅ Police review dashboard

### Phase 2 - Enhanced Features
- 🔄 Video recording capability
- 🔄 Advanced duplicate detection with ML
- 🔄 Reward system implementation
- 🔄 User profiles and history

### Phase 3 - Scale & Optimize
- 📋 Multi-city deployment
- 📋 Advanced analytics
- 📋 API integrations
- 📋 Performance optimization

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For support and questions:
- Email: support@trafficviolationapp.com
- Documentation: [Wiki](https://github.com/yourusername/traffic-violation-app/wiki)
- Issues: [GitHub Issues](https://github.com/yourusername/traffic-violation-app/issues)

## 🙏 Acknowledgments

- Google Maps Platform for location services
- Firebase for backend infrastructure
- Android Jetpack for modern Android development
- Material Design for UI/UX guidelines

---

**Note**: This is a development version. For production deployment, additional security measures, testing, and compliance checks are required. 