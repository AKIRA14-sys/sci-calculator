package com.supersci.calculator.data

import androidx.room.*

@Entity(tableName = "calc_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "calc_favorites")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val category: String, // "CALCULATION", "FORMULA", "EQUATION"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_profiles")
data class GameProfileEntity(
    @PrimaryKey val packageName: String,
    val gameName: String,
    val crosshairPreset: String = "CROSS", // CROSS, DOT, CIRCLE
    val crosshairSize: Float = 24f,
    val crosshairOpacity: Float = 1.0f,
    val crosshairThickness: Float = 3f,
    val crosshairColor: Int = 0xFFFF0000.toInt(),
    val centerDot: Boolean = true,
    val isFavorite: Boolean = false,
    val lastPlayedTime: Long = 0
)

@Entity(tableName = "vault_files")
data class VaultFileEntity(
    @PrimaryKey val id: String,
    val originalName: String,
    val encryptedPath: String,
    val mimeType: String,
    val fileSize: Long,
    val folder: String = "Root",
    val dateAdded: Long = System.currentTimeMillis()
)

@Dao
interface CalcHistoryDao {
    @Query("SELECT * FROM calc_history ORDER BY timestamp DESC")
    suspend fun getAllHistory(): List<HistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: HistoryEntity)

    @Delete
    suspend fun delete(history: HistoryEntity)

    @Query("DELETE FROM calc_history")
    suspend fun clearAll()
}

@Dao
interface FavoritesDao {
    @Query("SELECT * FROM calc_favorites ORDER BY timestamp DESC")
    suspend fun getAllFavorites(): List<FavoriteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Delete
    suspend fun delete(favorite: FavoriteEntity)
}

@Dao
interface GameProfileDao {
    @Query("SELECT * FROM game_profiles")
    suspend fun getAllProfiles(): List<GameProfileEntity>

    @Query("SELECT * FROM game_profiles WHERE packageName = :pkg")
    suspend fun getProfile(pkg: String): GameProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: GameProfileEntity)

    @Query("UPDATE game_profiles SET lastPlayedTime = :time WHERE packageName = :pkg")
    suspend fun updateLastPlayed(pkg: String, time: Long)

    @Query("UPDATE game_profiles SET isFavorite = :isFav WHERE packageName = :pkg")
    suspend fun updateFavorite(pkg: String, isFav: Boolean)
}

@Dao
interface VaultFileDao {
    @Query("SELECT * FROM vault_files WHERE folder = :folder ORDER BY dateAdded DESC")
    suspend fun getFilesInFolder(folder: String): List<VaultFileEntity>

    @Query("SELECT * FROM vault_files ORDER BY dateAdded DESC")
    suspend fun getAllFiles(): List<VaultFileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(file: VaultFileEntity)

    @Delete
    suspend fun delete(file: VaultFileEntity)
}

@Database(
    entities = [
        HistoryEntity::class,
        FavoriteEntity::class,
        GameProfileEntity::class,
        VaultFileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): CalcHistoryDao
    abstract fun favoritesDao(): FavoritesDao
    abstract fun gameProfileDao(): GameProfileDao
    abstract fun vaultFileDao(): VaultFileDao
}
