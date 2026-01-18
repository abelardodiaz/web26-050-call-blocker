# Security Policy

## Reporting a Vulnerability

If you discover a security vulnerability in Call Blocker, please report it responsibly:

1. **Do NOT open a public issue** for security vulnerabilities
2. Send details to the project maintainers via GitLab private message
3. Include:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if any)

We will respond within 48 hours and work with you to address the issue.

## Security Practices

### Data Storage

- **Local only**: All data is stored locally on your device
- **No cloud sync**: No data is transmitted to external servers
- **Room database**: SQLite with standard Android security
- **Encrypted backups**: Optional AES-256-GCM encryption for backup files

### Encryption Details

When you create an encrypted backup:

- **Algorithm**: AES-256-GCM (authenticated encryption)
- **Key derivation**: PBKDF2 with 100,000 iterations
- **Salt**: Random 16-byte salt per backup
- **IV**: Random 12-byte IV per backup
- **Format**: Custom `.cbbk` format with magic header

### Permissions

Call Blocker requests only the permissions necessary for its functionality:

| Permission | Purpose | Data Access |
|------------|---------|-------------|
| `READ_PHONE_STATE` | Identify incoming calls | Phone number only |
| `READ_CALL_LOG` | Log blocked calls | Call log entries |
| `ANSWER_PHONE_CALLS` | Reject blocked calls | None |
| `POST_NOTIFICATIONS` | Show blocking notifications | None |
| `ROLE_CALL_SCREENING` | Act as system call screener | Incoming call data |

### What We DON'T Do

- No analytics or tracking
- No data collection
- No network requests
- No advertising
- No third-party SDKs that collect data
- No cloud storage or sync

### Code Security

- No obfuscation: Open source, fully auditable
- Minimal dependencies
- Regular dependency updates
- No native code (pure Kotlin)

## Supported Versions

| Version | Supported |
|---------|-----------|
| 0.3.x   | Yes       |
| 0.2.x   | Yes       |
| < 0.2   | No        |

## Security Updates

Security fixes are prioritized and released as patch versions (e.g., 0.3.1).

## Audit

The codebase is open source and available for security audits. We welcome security researchers to review our code.

## Third-Party Dependencies

All dependencies are from reputable sources:

- **AndroidX/Jetpack**: Google's official Android libraries
- **Hilt**: Google's dependency injection
- **Room**: Google's SQLite abstraction
- **Material 3**: Google's design system

No third-party analytics, advertising, or tracking libraries are included.
