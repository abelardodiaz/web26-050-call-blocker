# Notas de Desarrollo - Call Blocker

## 2026-01-17: Implementacion de Bloqueo por Prefijo

### Resumen del Cambio

Se agrego la funcionalidad para bloquear llamadas por prefijo. Por ejemplo, si se bloquea "442", todas las llamadas que empiecen con "442" seran bloqueadas automaticamente.

### Archivos Modificados

| Capa | Archivo | Cambio |
|------|---------|--------|
| Domain | `domain/model/BlockedNumber.kt` | Agregado `isPrefix: Boolean = false` |
| Data | `data/local/entity/BlockedNumberEntity.kt` | Agregado columna `is_prefix` con `@ColumnInfo` |
| Data | `data/local/migration/Migrations.kt` | **Nuevo** - MIGRATION_1_2 |
| Data | `data/local/AppDatabase.kt` | Version 1 → 2 |
| Data | `data/local/dao/BlockedNumberDao.kt` | Query corregido para matching exacto + prefijo |
| DI | `core/di/AppModule.kt` | Agregado `.addMigrations()` |
| UI | `presentation/components/AddNumberDialog.kt` | Agregado Switch "Block as prefix" |
| UI | `presentation/components/BlockedNumberCard.kt` | Agregado badge "PREFIX" |
| ViewModel | `presentation/screens/blocklist/BlockListViewModel.kt` | Parametro `isPrefix` |
| Screen | `presentation/screens/blocklist/BlockListScreen.kt` | Pasa `isPrefix` al dialog |

### Query de Bloqueo (Critico)

**Antes** (problematico - matching bidireccional confuso):
```sql
SELECT EXISTS(
  SELECT 1 FROM blocked_numbers
  WHERE phoneNumber LIKE '%' || :phoneNumber
     OR :phoneNumber LIKE '%' || phoneNumber
)
```

**Despues** (correcto - exacto + prefijo):
```sql
SELECT EXISTS(
  SELECT 1 FROM blocked_numbers
  WHERE (is_prefix = 0 AND phoneNumber = :phoneNumber)
     OR (is_prefix = 1 AND :phoneNumber LIKE phoneNumber || '%')
)
```

### Migracion de Base de Datos

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE blocked_numbers ADD COLUMN is_prefix INTEGER NOT NULL DEFAULT 0")
    }
}
```

- Los numeros existentes migran con `isPrefix = false` (compatibilidad hacia atras)
- No se pierden datos

### UI Agregada

1. **AddNumberDialog**: Switch toggle para marcar como prefijo
2. **BlockedNumberCard**: Badge "PREFIX" en color primaryContainer cuando `isPrefix == true`

---

## 2026-01-17: Configuracion del Entorno de Build

### Android SDK en Server005

Se configuro el entorno de compilacion Android en el servidor:

```bash
# Ubicaciones
ANDROID_HOME=/home/ubuntu/android-sdk
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
GRADLE=/home/ubuntu/gradle-8.7/bin/gradle
```

### Componentes SDK Instalados

- `platform-tools` (adb, fastboot)
- `platforms;android-34`
- `build-tools;34.0.0`
- `cmdline-tools;latest`

### Archivos de Proyecto Agregados

| Archivo | Proposito |
|---------|-----------|
| `gradlew` | Gradle wrapper script (Unix) |
| `gradlew.bat` | Gradle wrapper script (Windows) |
| `gradle.properties` | Configuracion de Gradle (AndroidX, memoria) |
| `app/src/main/res/mipmap-*/` | Iconos de launcher (placeholder) |
| `app/src/main/res/drawable/ic_launcher_*.xml` | Iconos adaptativos (vector) |

### Comando de Build

```bash
# Recomendado (evita problemas de memoria)
./gradlew assembleDebug --no-daemon

# Con daemon (mas rapido pero puede crashear en VPS)
./gradlew assembleDebug
```

### Output

- APK Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Tamano: ~54MB (debug con todas las librerias Compose)

---

## Notas Tecnicas

### Por que no usar Snap para Gradle

- snap tiene problemas con paths en algunos entornos
- apt gradle (4.4.1) es muy viejo para Android moderno
- Solucion: Descarga manual de Gradle 8.7

### Memoria para Compilacion

- Minimo recomendado: 4GB RAM
- Configurado en `gradle.properties`: `-Xmx2048m`
- Usar `--no-daemon` si el daemon crashea

### Iconos Placeholder

Los iconos actuales son placeholders minimos. Para produccion:

1. Usar Android Studio > Image Asset Studio
2. O crear iconos con herramientas como Figma
3. Exportar a todas las densidades (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)
