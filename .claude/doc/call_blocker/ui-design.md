# Call Blocker - UI Design

**Feature**: Call Blocker App
**Agent**: compose-ui-architect
**Date**: 2026-01-17
**Session**: context_session_call_blocker

---

## Executive Summary

Diseno de UI minimalista y funcional usando Jetpack Compose con Material 3. La app tiene 4 pantallas principales con navegacion por bottom navigation.

**Key Decisions:**
- Material 3 con Dynamic Color
- Bottom Navigation para navegacion principal
- FAB para agregar numeros
- Cards para mostrar llamadas bloqueadas

---

## 1. Navigation Structure

```
BottomNavigation
|
|-- BlockedCalls (Home)     // Lista de llamadas bloqueadas
|-- BlockList               // Numeros bloqueados
|-- Settings                // Configuracion
```

---

## 2. Screen Specifications

### 2.1 BlockedCallsScreen (Home)

**Layout:**
```
+----------------------------------+
|  TopAppBar                       |
|  Call Blocker          [Toggle]  |
+----------------------------------+
|                                  |
|  Today (2)                       |
|  +----------------------------+  |
|  | +1 555-123-4567           |  |
|  | Blocked - Blacklist       |  |
|  | 10:30 AM                  |  |
|  +----------------------------+  |
|  | Private Number            |  |
|  | Blocked - Private         |  |
|  | 9:15 AM                   |  |
|  +----------------------------+  |
|                                  |
|  Yesterday (5)                   |
|  +----------------------------+  |
|  | ...                       |  |
|  +----------------------------+  |
|                                  |
+----------------------------------+
|  [Calls]  [Block List]  [Settings]
+----------------------------------+
```

**States:**
| State | UI |
|-------|-----|
| Loading | CircularProgressIndicator centered |
| Empty | EmptyState with icon and message |
| Loaded | LazyColumn with grouped items |
| Error | ErrorState with retry button |

### 2.2 BlockListScreen

**Layout:**
```
+----------------------------------+
|  TopAppBar                       |
|  Block List              [Search]|
+----------------------------------+
|  [Search TextField]              |
+----------------------------------+
|                                  |
|  +----------------------------+  |
|  | +1 555-123-4567      [X]  |  |
|  | Spam caller               |  |
|  +----------------------------+  |
|  | 800-*              [X]    |  |
|  | All 800 numbers (prefix)  |  |
|  +----------------------------+  |
|                                  |
+----------------------------------+
|                          [+ FAB] |
+----------------------------------+
|  [Calls]  [Block List]  [Settings]
+----------------------------------+
```

### 2.3 AddNumberScreen (Bottom Sheet)

**Layout:**
```
+----------------------------------+
|  Add to Block List               |
+----------------------------------+
|                                  |
|  Phone Number                    |
|  [+1 555-123-4567           ]   |
|                                  |
|  [ ] Block as prefix             |
|      (blocks all numbers         |
|       starting with this)        |
|                                  |
|  Notes (optional)                |
|  [Spam caller              ]     |
|                                  |
|  [    Cancel    ] [    Add    ]  |
|                                  |
+----------------------------------+
```

### 2.4 SettingsScreen

**Layout:**
```
+----------------------------------+
|  TopAppBar                       |
|  Settings                        |
+----------------------------------+
|                                  |
|  BLOCKING                        |
|  +----------------------------+  |
|  | Enable Call Blocking  [X] |  |
|  +----------------------------+  |
|  | Block Mode            [>] |  |
|  | Reject calls              |  |
|  +----------------------------+  |
|                                  |
|  FILTERS                         |
|  +----------------------------+  |
|  | Block Unknown Numbers [X] |  |
|  +----------------------------+  |
|  | Block Private Numbers [X] |  |
|  +----------------------------+  |
|                                  |
|  NOTIFICATIONS                   |
|  +----------------------------+  |
|  | Show Blocked Alerts   [X] |  |
|  +----------------------------+  |
|                                  |
|  ABOUT                           |
|  +----------------------------+  |
|  | Version 1.0.0             |  |
|  +----------------------------+  |
|                                  |
+----------------------------------+
|  [Calls]  [Block List]  [Settings]
+----------------------------------+
```

---

## 3. Component Specifications

### 3.1 BlockedCallCard

```kotlin
@Composable
fun BlockedCallCard(
    call: BlockedCall,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)

// Visual:
// +--------------------------------+
// | [Icon]  +1 555-123-4567        |
// |         Blocked - Blacklist    |
// |         10:30 AM          [>]  |
// +--------------------------------+
```

### 3.2 BlockedNumberCard

```kotlin
@Composable
fun BlockedNumberCard(
    number: BlockedNumber,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
)

// Visual:
// +--------------------------------+
// | +1 555-123-4567           [X]  |
// | Spam caller                    |
// +--------------------------------+
```

### 3.3 SettingsItem

```kotlin
@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
)

@Composable
fun SettingsClickItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
)
```

### 3.4 MainToggle (Enable/Disable)

```kotlin
@Composable
fun MainToggle(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
)

// Visual: Large switch in TopAppBar
```

---

## 4. Color Scheme

```kotlin
// Using Material 3 Dynamic Color with fallback

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),          // Blue
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    secondary = Color(0xFF43A047),        // Green (enabled)
    error = Color(0xFFD32F2F),            // Red (blocked)
    background = Color(0xFFFAFAFA),
    surface = Color.White,
)

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF1565C0),
    secondary = Color(0xFF81C784),
    error = Color(0xFFEF5350),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
)
```

---

## 5. Icons

| Element | Icon |
|---------|------|
| Blocked Calls tab | `Icons.Outlined.CallEnd` |
| Block List tab | `Icons.Outlined.Block` |
| Settings tab | `Icons.Outlined.Settings` |
| Add number FAB | `Icons.Filled.Add` |
| Delete number | `Icons.Outlined.Close` |
| Private number | `Icons.Outlined.VisibilityOff` |
| Unknown number | `Icons.Outlined.PersonOff` |
| Blacklist | `Icons.Outlined.Block` |

---

## 6. Navigation Implementation

```kotlin
// navigation/CallBlockerNavigation.kt
sealed class Screen(val route: String) {
    object BlockedCalls : Screen("blocked_calls")
    object BlockList : Screen("block_list")
    object Settings : Screen("settings")
}

@Composable
fun CallBlockerNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.BlockedCalls.route,
        modifier = modifier
    ) {
        composable(Screen.BlockedCalls.route) {
            BlockedCallsScreen()
        }
        composable(Screen.BlockList.route) {
            BlockListScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
```

---

## 7. Bottom Navigation

```kotlin
@Composable
fun CallBlockerBottomBar(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.CallEnd, "Blocked Calls") },
            label = { Text("Calls") },
            selected = currentRoute == Screen.BlockedCalls.route,
            onClick = { onNavigate(Screen.BlockedCalls) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Block, "Block List") },
            label = { Text("Block List") },
            selected = currentRoute == Screen.BlockList.route,
            onClick = { onNavigate(Screen.BlockList) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Settings, "Settings") },
            label = { Text("Settings") },
            selected = currentRoute == Screen.Settings.route,
            onClick = { onNavigate(Screen.Settings) }
        )
    }
}
```

---

## 8. Empty States

### No Blocked Calls
```kotlin
EmptyState(
    icon = Icons.Outlined.CheckCircle,
    title = "No blocked calls",
    message = "Blocked calls will appear here"
)
```

### No Blocked Numbers
```kotlin
EmptyState(
    icon = Icons.Outlined.Block,
    title = "No blocked numbers",
    message = "Tap + to add numbers to block"
)
```

---

## 9. Animations

```kotlin
// List item animations
LazyColumn {
    items(
        items = blockedCalls,
        key = { it.id }
    ) { call ->
        BlockedCallCard(
            call = call,
            modifier = Modifier.animateItem()
        )
    }
}

// FAB animation
AnimatedVisibility(
    visible = !isScrolling,
    enter = scaleIn() + fadeIn(),
    exit = scaleOut() + fadeOut()
) {
    FloatingActionButton(onClick = onAddClick) {
        Icon(Icons.Filled.Add, "Add")
    }
}
```

---

## Implementation Checklist

- [ ] Create navigation structure
- [ ] Implement BlockedCallsScreen
- [ ] Implement BlockListScreen
- [ ] Implement SettingsScreen
- [ ] Create AddNumberBottomSheet
- [ ] Create BlockedCallCard component
- [ ] Create BlockedNumberCard component
- [ ] Create SettingsItem components
- [ ] Implement theme (light/dark)
- [ ] Add animations
- [ ] Test on different screen sizes

---

*Generated by compose-ui-architect - Sistema de Agentes 996*
