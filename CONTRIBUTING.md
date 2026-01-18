# Contributing to Call Blocker

Thank you for your interest in contributing to Call Blocker! This document provides guidelines and instructions for contributing.

## Code of Conduct

- Be respectful and inclusive
- Focus on constructive feedback
- Help others learn and grow

## Getting Started

### Prerequisites

- JDK 17
- Android SDK 34
- Android Studio (recommended) or command line tools
- Device or emulator running Android 9+ (API 28+)

### Development Setup

1. **Fork and clone the repository**
   ```bash
   git clone https://gitlab.com/your-username/call-blocker.git
   cd call-blocker
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory
   - Wait for Gradle sync to complete

3. **Build the project**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run tests**
   ```bash
   ./gradlew test
   ```

## Project Architecture

This project follows **Clean Architecture** with **MVVM**:

```
app/src/main/java/com/callblocker/
|-- core/           # DI, Services, Receivers
|-- data/           # Room entities, DAOs, Repositories
|-- domain/         # Models, Repository interfaces, Use Cases
+-- presentation/   # Compose UI, ViewModels, Navigation
```

### Key Technologies

- **Kotlin** - Primary language
- **Jetpack Compose** - UI framework
- **Material 3** - Design system
- **Hilt** - Dependency injection
- **Room** - Local database
- **Coroutines/Flow** - Async operations

## Code Style

### Kotlin

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Prefer immutability (`val` over `var`)
- Use data classes for models
- Keep functions small and focused

### Compose

- Use `@Preview` for composables
- Extract reusable components
- Follow Material 3 theming
- Use `stringResource()` for all user-facing strings

### Example

```kotlin
// Good
@Composable
fun BlockedNumberCard(
    number: BlockedNumber,
    onDelete: (BlockedNumber) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Text(text = number.phoneNumber)
        IconButton(onClick = { onDelete(number) }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.content_desc_delete_number)
            )
        }
    }
}
```

## Making Changes

### Branch Naming

- `feature/description` - New features
- `fix/description` - Bug fixes
- `docs/description` - Documentation
- `refactor/description` - Code refactoring

### Commit Messages

Follow conventional commits:

```
type(scope): description

- feat: New feature
- fix: Bug fix
- docs: Documentation
- refactor: Code refactoring
- test: Tests
- chore: Maintenance
```

Example:
```
feat(backup): Add encrypted backup support

- Implement AES-256-GCM encryption
- Add password dialog
- Support .cbbk file format
```

## Pull Request Process

1. **Create a feature branch**
   ```bash
   git checkout -b feature/my-feature
   ```

2. **Make your changes**
   - Write clean, documented code
   - Add tests if applicable
   - Update documentation if needed

3. **Test your changes**
   ```bash
   ./gradlew test
   ./gradlew assembleDebug
   ```

4. **Commit and push**
   ```bash
   git add .
   git commit -m "feat(scope): description"
   git push origin feature/my-feature
   ```

5. **Open a Merge Request**
   - Provide a clear description
   - Reference any related issues
   - Add screenshots for UI changes

### MR Checklist

- [ ] Code compiles without errors
- [ ] Tests pass
- [ ] No new warnings introduced
- [ ] Documentation updated if needed
- [ ] Commit messages follow conventions

## Testing

### Unit Tests

```bash
./gradlew test
```

### Instrumented Tests

```bash
./gradlew connectedAndroidTest
```

### Manual Testing

- Test on multiple Android versions (9, 12, 14+)
- Test dual SIM scenarios if possible
- Verify backup/restore functionality

## Reporting Issues

### Bug Reports

Include:
- Android version and device model
- Steps to reproduce
- Expected vs actual behavior
- Logs if available (`adb logcat`)

### Feature Requests

Include:
- Clear description of the feature
- Use case and benefits
- Possible implementation approach

## Questions?

- Open an issue for questions
- Check existing issues before creating new ones

## License

By contributing, you agree that your contributions will be licensed under the GNU General Public License v3.0.
