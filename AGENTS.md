This is a new Android Studio Project that uses Kotlin and Jetpack Compose.

ARCHITECTURE PROTOCOL: We use a strict Feature-Sliced Clean Architecture. The Single Source of Truth for data structures are the pure Kotlin Data Classes located in core/domain/models/. NEVER leak framework imports (Android, Room, Ktor, AI SDKs) into the domain/ layer. All external communications (DB, APIs, Sensors) must implement abstract interfaces defined in domain/interfaces/. External data (DTOs, DB Entities) must exist only in the data/ layer and MUST use Mappers to convert to/from Domain models. Pure business logic lives exclusively in UseCases within features/{feature_name}/domain/. UI state is managed via ViewModels in features/{feature_name}/presentation/. To add a new capability, create a new isolated folder inside features/ following this exact Domain/Data/Presentation triad. Keep it SOLID, DRY, and completely decoupled using Dependency Injection.

Read ./SPEC.md for general information about the project.
Always be proactive and make production ready code and atomic-conventional commits along all your runs.
The architecture must have easy future compatibility for Kotlin Multiplatform.
Always make sure to write code with a high Test Coverage and include unit tests for everything.
Procure the use of the newest versions of the packages and never use deprecated packages.
For the UI always make an ultra minimalist modern-professional Apple Like UI mainly in gray-scale for this app.