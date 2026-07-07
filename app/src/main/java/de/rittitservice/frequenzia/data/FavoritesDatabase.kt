package de.rittitservice.frequenzia.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "favorites")
data class FavoriteStation(
    @PrimaryKey val stationuuid: String,
    val name: String,
    val url_resolved: String,
    val favicon: String?,
    val countrycode: String?,
    val tags: String?
)

@Entity(tableName = "recently_played")
data class RecentlyPlayedStation(
    @PrimaryKey val stationuuid: String,
    val name: String,
    val url_resolved: String,
    val favicon: String?,
    val countrycode: String?,
    val tags: String?,
    val playedAt: Long
)

@Dao
interface RecentlyPlayedDao {
    @Query("SELECT * FROM recently_played ORDER BY playedAt DESC LIMIT 50")
    fun getRecent(): Flow<List<RecentlyPlayedStation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(station: RecentlyPlayedStation)
}

@Dao
interface FavoritesDao {
    @Query("SELECT * FROM favorites ORDER BY name ASC")
    fun getAll(): Flow<List<FavoriteStation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(station: FavoriteStation)

    @Query("DELETE FROM favorites WHERE stationuuid = :uuid")
    suspend fun deleteByUuid(uuid: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE stationuuid = :uuid)")
    suspend fun isFavorite(uuid: String): Boolean
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `recently_played` (
                `stationuuid` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `url_resolved` TEXT NOT NULL,
                `favicon` TEXT,
                `countrycode` TEXT,
                `tags` TEXT,
                `playedAt` INTEGER NOT NULL,
                PRIMARY KEY(`stationuuid`)
            )
            """.trimIndent()
        )
    }
}

@Database(
    entities = [FavoriteStation::class, RecentlyPlayedStation::class],
    version = 2,
    exportSchema = true
)
abstract class FavoritesDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
    abstract fun recentlyPlayedDao(): RecentlyPlayedDao

    companion object {
        @Volatile private var INSTANCE: FavoritesDatabase? = null

        fun getInstance(context: Context): FavoritesDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FavoritesDatabase::class.java,
                    "frequenzia-favorites.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build().also { INSTANCE = it }
            }
        }
    }
}

fun RadioStation.toFavorite() = FavoriteStation(
    stationuuid = stationuuid,
    name = name,
    url_resolved = url_resolved,
    favicon = favicon,
    countrycode = countrycode,
    tags = tags
)

fun RadioStation.toRecentlyPlayed(playedAt: Long = System.currentTimeMillis()) = RecentlyPlayedStation(
    stationuuid = stationuuid,
    name = name,
    url_resolved = url_resolved,
    favicon = favicon,
    countrycode = countrycode,
    tags = tags,
    playedAt = playedAt
)
