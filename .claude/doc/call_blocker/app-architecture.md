# Call Blocker - App Architecture

**Feature**: Call Blocker App
**Agent**: android-architecture-expert
**Date**: 2026-01-17
**Session**: context_session_call_blocker

---

## Executive Summary

Arquitectura Clean Architecture con MVVM para la capa de presentacion. Uso de Hilt para DI, Room para persistencia, y Kotlin Coroutines/Flow para operaciones asincronas.

**Key Decisions:**
- Clean Architecture (Domain, Data, Presentation)
- MVVM con StateFlow para UI state
- Hilt para dependency injection
- Room para base de datos local
- Coroutines + Flow para async

---

## 1. Project Structure

```
app/
|-- src/main/java/com/callblocker/
|   |-- CallBlockerApplication.kt
|   |
|   |-- core/
|   |   |-- service/
|   |   |   +-- CallBlockerScreeningService.kt
|   |   |-- receiver/
|   |   |   +-- BootReceiver.kt
|   |   |-- util/
|   |   |   |-- PermissionHandler.kt
|   |   |   +-- CallScreeningRoleManager.kt
|   |   +-- di/
|   |       |-- AppModule.kt
|   |       |-- DatabaseModule.kt
|   |       +-- RepositoryModule.kt
|   |
|   |-- domain/
|   |   |-- model/
|   |   |   |-- BlockedNumber.kt
|   |   |   |-- BlockedCall.kt
|   |   |   |-- Settings.kt
|   |   |   +-- BlockReason.kt
|   |   |-- repository/
|   |   |   |-- BlockedNumberRepository.kt
|   |   |   |-- BlockedCallRepository.kt
|   |   |   +-- SettingsRepository.kt
|   |   +-- usecase/
|   |       |-- blockedcalls/
|   |       |   |-- GetBlockedCallsUseCase.kt
|   |       |   +-- ClearBlockedCallsUseCase.kt
|   |       |-- blockednumbers/
|   |       |   |-- GetBlockedNumbersUseCase.kt
|   |       |   |-- AddBlockedNumberUseCase.kt
|   |       |   |-- RemoveBlockedNumberUseCase.kt
|   |       |   +-- IsNumberBlockedUseCase.kt
|   |       +-- settings/
|   |           |-- GetSettingsUseCase.kt
|   |           +-- UpdateSettingsUseCase.kt
|   |
|   |-- data/
|   |   |-- local/
|   |   |   |-- AppDatabase.kt
|   |   |   |-- dao/
|   |   |   |   |-- BlockedNumberDao.kt
|   |   |   |   |-- BlockedCallDao.kt
|   |   |   |   +-- SettingsDao.kt
|   |   |   +-- entity/
|   |   |       |-- BlockedNumberEntity.kt
|   |   |       |-- BlockedCallEntity.kt
|   |   |       +-- SettingsEntity.kt
|   |   +-- repository/
|   |       |-- BlockedNumberRepositoryImpl.kt
|   |       |-- BlockedCallRepositoryImpl.kt
|   |       +-- SettingsRepositoryImpl.kt
|   |
|   +-- presentation/
|       |-- MainActivity.kt
|       |-- navigation/
|       |   |-- Screen.kt
|       |   +-- CallBlockerNavHost.kt
|       |-- theme/
|       |   |-- Theme.kt
|       |   |-- Color.kt
|       |   +-- Type.kt
|       |-- components/
|       |   |-- BlockedCallCard.kt
|       |   |-- BlockedNumberCard.kt
|       |   |-- SettingsItem.kt
|       |   |-- EmptyState.kt
|       |   +-- ErrorState.kt
|       +-- screens/
|           |-- blockedcalls/
|           |   |-- BlockedCallsScreen.kt
|           |   |-- BlockedCallsViewModel.kt
|           |   +-- BlockedCallsUiState.kt
|           |-- blocklist/
|           |   |-- BlockListScreen.kt
|           |   |-- BlockListViewModel.kt
|           |   |-- BlockListUiState.kt
|           |   +-- AddNumberBottomSheet.kt
|           +-- settings/
|               |-- SettingsScreen.kt
|               |-- SettingsViewModel.kt
|               +-- SettingsUiState.kt
```

---

## 2. Domain Layer

### 2.1 Models

```kotlin
// domain/model/BlockedNumber.kt
data class BlockedNumber(
    val id: Long = 0,
    val phoneNumber: String,
    val displayName: String? = null,
    val isPrefix: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val notes: String? = null
)

// domain/model/BlockedCall.kt
data class BlockedCall(
    val id: Long = 0,
    val phoneNumber: String,
    val displayName: String? = null,
    val reason: BlockReason,
    val timestamp: Long
)

// domain/model/BlockReason.kt
enum class BlockReason {
    BLACKLIST,
    UNKNOWN,
    PRIVATE,
    PREFIX
}

// domain/model/Settings.kt
data class Settings(
    val isBlockingEnabled: Boolean = true,
    val blockMode: BlockMode = BlockMode.REJECT,
    val blockUnknownNumbers: Boolean = false,
    val blockPrivateNumbers: Boolean = false,
    val showBlockedNotifications: Boolean = true
)

enum class BlockMode {
    REJECT,
    SILENCE
}
```

### 2.2 Repository Interfaces

```kotlin
// domain/repository/BlockedNumberRepository.kt
interface BlockedNumberRepository {
    fun getAll(): Flow<List<BlockedNumber>>
    suspend fun getById(id: Long): BlockedNumber?
    suspend fun add(number: BlockedNumber): Long
    suspend fun remove(id: Long)
    suspend fun isBlocked(phoneNumber: String): Boolean
    fun search(query: String): Flow<List<BlockedNumber>>
}

// domain/repository/BlockedCallRepository.kt
interface BlockedCallRepository {
    fun getAll(): Flow<List<BlockedCall>>
    fun getGroupedByDate(): Flow<Map<String, List<BlockedCall>>>
    suspend fun log(call: BlockedCall)
    suspend fun clear()
    suspend fun getCount(): Int
}

// domain/repository/SettingsRepository.kt
interface SettingsRepository {
    fun getSettingsFlow(): Flow<Settings>
    suspend fun getSettings(): Settings
    suspend fun updateSettings(settings: Settings)
}
```

### 2.3 Use Cases

```kotlin
// domain/usecase/blockednumbers/GetBlockedNumbersUseCase.kt
class GetBlockedNumbersUseCase @Inject constructor(
    private val repository: BlockedNumberRepository
) {
    operator fun invoke(): Flow<List<BlockedNumber>> = repository.getAll()
}

// domain/usecase/blockednumbers/AddBlockedNumberUseCase.kt
class AddBlockedNumberUseCase @Inject constructor(
    private val repository: BlockedNumberRepository
) {
    suspend operator fun invoke(
        phoneNumber: String,
        displayName: String? = null,
        isPrefix: Boolean = false,
        notes: String? = null
    ): Long {
        val blockedNumber = BlockedNumber(
            phoneNumber = phoneNumber.normalizePhoneNumber(),
            displayName = displayName,
            isPrefix = isPrefix,
            notes = notes
        )
        return repository.add(blockedNumber)
    }
}

// domain/usecase/blockednumbers/IsNumberBlockedUseCase.kt
class IsNumberBlockedUseCase @Inject constructor(
    private val repository: BlockedNumberRepository
) {
    suspend operator fun invoke(phoneNumber: String): Boolean {
        return repository.isBlocked(phoneNumber.normalizePhoneNumber())
    }
}
```

---

## 3. Data Layer

### 3.1 Room Database

```kotlin
// data/local/AppDatabase.kt
@Database(
    entities = [
        BlockedNumberEntity::class,
        BlockedCallEntity::class,
        SettingsEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedNumberDao(): BlockedNumberDao
    abstract fun blockedCallDao(): BlockedCallDao
    abstract fun settingsDao(): SettingsDao
}
```

### 3.2 DAOs

```kotlin
// data/local/dao/BlockedNumberDao.kt
@Dao
interface BlockedNumberDao {
    @Query("SELECT * FROM blocked_numbers ORDER BY created_at DESC")
    fun getAll(): Flow<List<BlockedNumberEntity>>

    @Query("SELECT * FROM blocked_numbers WHERE id = :id")
    suspend fun getById(id: Long): BlockedNumberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BlockedNumberEntity): Long

    @Query("DELETE FROM blocked_numbers WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM blocked_numbers
            WHERE phone_number = :phoneNumber
            OR (:phoneNumber LIKE phone_number || '%' AND is_prefix = 1)
        )
    """)
    suspend fun isBlocked(phoneNumber: String): Boolean

    @Query("""
        SELECT * FROM blocked_numbers
        WHERE phone_number LIKE '%' || :query || '%'
        OR display_name LIKE '%' || :query || '%'
    """)
    fun search(query: String): Flow<List<BlockedNumberEntity>>
}

// data/local/dao/BlockedCallDao.kt
@Dao
interface BlockedCallDao {
    @Query("SELECT * FROM blocked_calls ORDER BY timestamp DESC")
    fun getAll(): Flow<List<BlockedCallEntity>>

    @Insert
    suspend fun insert(entity: BlockedCallEntity)

    @Query("DELETE FROM blocked_calls")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM blocked_calls")
    suspend fun getCount(): Int
}

// data/local/dao/SettingsDao.kt
@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun getSettingsOnce(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SettingsEntity)
}
```

### 3.3 Repository Implementations

```kotlin
// data/repository/BlockedNumberRepositoryImpl.kt
class BlockedNumberRepositoryImpl @Inject constructor(
    private val dao: BlockedNumberDao
) : BlockedNumberRepository {

    override fun getAll(): Flow<List<BlockedNumber>> {
        return dao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getById(id: Long): BlockedNumber? {
        return dao.getById(id)?.toDomain()
    }

    override suspend fun add(number: BlockedNumber): Long {
        return dao.insert(number.toEntity())
    }

    override suspend fun remove(id: Long) {
        dao.deleteById(id)
    }

    override suspend fun isBlocked(phoneNumber: String): Boolean {
        return dao.isBlocked(phoneNumber)
    }

    override fun search(query: String): Flow<List<BlockedNumber>> {
        return dao.search(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
```

---

## 4. Presentation Layer

### 4.1 ViewModels

```kotlin
// presentation/screens/blocklist/BlockListViewModel.kt
@HiltViewModel
class BlockListViewModel @Inject constructor(
    private val getBlockedNumbers: GetBlockedNumbersUseCase,
    private val addBlockedNumber: AddBlockedNumberUseCase,
    private val removeBlockedNumber: RemoveBlockedNumberUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BlockListUiState>(BlockListUiState.Loading)
    val uiState: StateFlow<BlockListUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadBlockedNumbers()
    }

    private fun loadBlockedNumbers() {
        viewModelScope.launch {
            getBlockedNumbers()
                .catch { e ->
                    _uiState.value = BlockListUiState.Error(e.message ?: "Unknown error")
                }
                .collect { numbers ->
                    _uiState.value = if (numbers.isEmpty()) {
                        BlockListUiState.Empty
                    } else {
                        BlockListUiState.Success(numbers)
                    }
                }
        }
    }

    fun addNumber(
        phoneNumber: String,
        displayName: String?,
        isPrefix: Boolean,
        notes: String?
    ) {
        viewModelScope.launch {
            try {
                addBlockedNumber(phoneNumber, displayName, isPrefix, notes)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun removeNumber(id: Long) {
        viewModelScope.launch {
            try {
                removeBlockedNumber(id)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}

// presentation/screens/blocklist/BlockListUiState.kt
sealed interface BlockListUiState {
    data object Loading : BlockListUiState
    data object Empty : BlockListUiState
    data class Success(val numbers: List<BlockedNumber>) : BlockListUiState
    data class Error(val message: String) : BlockListUiState
}
```

### 4.2 Screens

```kotlin
// presentation/screens/blocklist/BlockListScreen.kt
@Composable
fun BlockListScreen(
    viewModel: BlockListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Filled.Add, "Add number")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is BlockListUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is BlockListUiState.Empty -> {
                    EmptyState(
                        icon = Icons.Outlined.Block,
                        title = "No blocked numbers",
                        message = "Tap + to add numbers to block",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is BlockListUiState.Success -> {
                    BlockedNumberList(
                        numbers = state.numbers,
                        onRemove = viewModel::removeNumber
                    )
                }
                is BlockListUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { /* Retry */ },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    if (showAddSheet) {
        AddNumberBottomSheet(
            onDismiss = { showAddSheet = false },
            onAdd = { phoneNumber, displayName, isPrefix, notes ->
                viewModel.addNumber(phoneNumber, displayName, isPrefix, notes)
                showAddSheet = false
            }
        )
    }
}
```

---

## 5. Dependency Injection

### 5.1 App Module

```kotlin
// core/di/AppModule.kt
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context
}
```

### 5.2 Database Module

```kotlin
// core/di/DatabaseModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "call_blocker_db"
        ).build()
    }

    @Provides
    fun provideBlockedNumberDao(db: AppDatabase): BlockedNumberDao {
        return db.blockedNumberDao()
    }

    @Provides
    fun provideBlockedCallDao(db: AppDatabase): BlockedCallDao {
        return db.blockedCallDao()
    }

    @Provides
    fun provideSettingsDao(db: AppDatabase): SettingsDao {
        return db.settingsDao()
    }
}
```

### 5.3 Repository Module

```kotlin
// core/di/RepositoryModule.kt
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBlockedNumberRepository(
        impl: BlockedNumberRepositoryImpl
    ): BlockedNumberRepository

    @Binds
    @Singleton
    abstract fun bindBlockedCallRepository(
        impl: BlockedCallRepositoryImpl
    ): BlockedCallRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository
}
```

---

## 6. Application Class

```kotlin
// CallBlockerApplication.kt
@HiltAndroidApp
class CallBlockerApplication : Application()
```

---

## Implementation Checklist

- [ ] Set up project with Hilt
- [ ] Create domain models
- [ ] Create repository interfaces
- [ ] Create use cases
- [ ] Set up Room database
- [ ] Create DAOs
- [ ] Implement repositories
- [ ] Create ViewModels
- [ ] Create Compose screens
- [ ] Set up navigation
- [ ] Configure DI modules
- [ ] Write tests

---

*Generated by android-architecture-expert - Sistema de Agentes 996*
