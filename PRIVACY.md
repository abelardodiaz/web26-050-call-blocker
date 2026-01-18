# Privacy Policy

**Last updated**: January 2026

## Overview

Call Blocker is designed with privacy as a core principle. This app:

- Works entirely offline
- Stores all data locally on your device
- Does not collect, transmit, or share any personal information
- Contains no analytics, tracking, or advertising

## Data Collection

**We do not collect any data.**

Call Blocker does not:
- Send any data to external servers
- Use analytics or tracking services
- Include advertising SDKs
- Require an account or registration
- Access the internet for any purpose

## Data Storage

All data is stored locally on your device:

| Data Type | Storage Location | Purpose |
|-----------|------------------|---------|
| Blocked numbers | App database | Numbers you've chosen to block |
| Blocked call log | App database | History of blocked calls |
| Settings | App database | Your preferences |
| Backups | Downloads folder | Manual backups you create |

### Data Retention

- Data remains on your device until you delete it
- Uninstalling the app removes all data
- Backups in Downloads folder persist until manually deleted

## Permissions Explained

### READ_PHONE_STATE

**Why needed**: To identify incoming phone numbers for blocking decisions.

**What we access**: Only the incoming phone number when a call arrives.

**What we DON'T access**: Your contacts, call history (except blocked calls), or any other phone state information.

### READ_CALL_LOG

**Why needed**: To record blocked calls in the app's history.

**What we access**: Only call log entries to correlate with blocked calls.

**What we DON'T access**: Your complete call history.

### ANSWER_PHONE_CALLS

**Why needed**: To reject (block) incoming calls that match your block list.

**What we do**: Silently reject calls from blocked numbers.

### POST_NOTIFICATIONS

**Why needed**: To notify you when a call is blocked (optional feature).

**What we do**: Show a local notification. No data is sent anywhere.

### ROLE_CALL_SCREENING

**Why needed**: Android requires this role for apps that screen incoming calls.

**What we do**: Act as the system's call screening service.

## Backups

When you create a backup:

- The file is saved locally to your Downloads folder
- You control where the file goes (share, copy, delete)
- Optional password protection uses AES-256-GCM encryption
- We never see, access, or store your backups

### Encrypted Backups

If you choose to encrypt your backup:

- Password never leaves your device
- We don't store or recover passwords
- Lost passwords cannot be recovered

## Third-Party Services

Call Blocker uses **no third-party services**:

- No Google Analytics
- No Firebase (except required Android components)
- No crash reporting services
- No advertising networks
- No social media SDKs

## Children's Privacy

Call Blocker does not knowingly collect any information from anyone, including children under 13.

## Open Source

Call Blocker is open source. You can verify our privacy claims by reviewing the source code:

- All code is publicly available
- No hidden data collection
- Community-auditable

## Changes to This Policy

We will post any changes to this privacy policy in the app repository. Significant changes will be noted in the changelog.

## Contact

For privacy questions or concerns, please open an issue on our GitLab repository.

## Your Rights

Since we don't collect any data:

- There's no data to request
- There's no data to delete (beyond uninstalling the app)
- There's no data we share with third parties

**Your data stays on your device, under your control.**
