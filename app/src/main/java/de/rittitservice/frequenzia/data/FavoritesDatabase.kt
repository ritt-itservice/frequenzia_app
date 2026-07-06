package de.rittitservice.frequenzia.data

import android.content.Context
import androidx.room.*
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

@Database(entities = [FavoriteStation::class], version = 1, exportSchema = false)
abstract class FavoritesDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao

    companion object {
        @Volatile private var INSTANCE: FavoritesDatabase? = null

        fun getInstance(context: Context): FavoritesDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FavoritesDatabase::class.java,
                    "frequenzia-favorites.db"
                ).build().also { INSTANCE = it }
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
