package de.rittitservice.frequenzia.`data`

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class FavoritesDao_Impl(
  __db: RoomDatabase,
) : FavoritesDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfFavoriteStation: EntityInsertAdapter<FavoriteStation>
  init {
    this.__db = __db
    this.__insertAdapterOfFavoriteStation = object : EntityInsertAdapter<FavoriteStation>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `favorites` (`stationuuid`,`name`,`url_resolved`,`favicon`,`countrycode`,`tags`) VALUES (?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: FavoriteStation) {
        statement.bindText(1, entity.stationuuid)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.url_resolved)
        val _tmpFavicon: String? = entity.favicon
        if (_tmpFavicon == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpFavicon)
        }
        val _tmpCountrycode: String? = entity.countrycode
        if (_tmpCountrycode == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpCountrycode)
        }
        val _tmpTags: String? = entity.tags
        if (_tmpTags == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpTags)
        }
      }
    }
  }

  public override suspend fun insert(station: FavoriteStation): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfFavoriteStation.insert(_connection, station)
  }

  public override fun getAll(): Flow<List<FavoriteStation>> {
    val _sql: String = "SELECT * FROM favorites ORDER BY name ASC"
    return createFlow(__db, false, arrayOf("favorites")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfStationuuid: Int = getColumnIndexOrThrow(_stmt, "stationuuid")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfUrlResolved: Int = getColumnIndexOrThrow(_stmt, "url_resolved")
        val _columnIndexOfFavicon: Int = getColumnIndexOrThrow(_stmt, "favicon")
        val _columnIndexOfCountrycode: Int = getColumnIndexOrThrow(_stmt, "countrycode")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _result: MutableList<FavoriteStation> = mutableListOf()
        while (_stmt.step()) {
          val _item: FavoriteStation
          val _tmpStationuuid: String
          _tmpStationuuid = _stmt.getText(_columnIndexOfStationuuid)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpUrl_resolved: String
          _tmpUrl_resolved = _stmt.getText(_columnIndexOfUrlResolved)
          val _tmpFavicon: String?
          if (_stmt.isNull(_columnIndexOfFavicon)) {
            _tmpFavicon = null
          } else {
            _tmpFavicon = _stmt.getText(_columnIndexOfFavicon)
          }
          val _tmpCountrycode: String?
          if (_stmt.isNull(_columnIndexOfCountrycode)) {
            _tmpCountrycode = null
          } else {
            _tmpCountrycode = _stmt.getText(_columnIndexOfCountrycode)
          }
          val _tmpTags: String?
          if (_stmt.isNull(_columnIndexOfTags)) {
            _tmpTags = null
          } else {
            _tmpTags = _stmt.getText(_columnIndexOfTags)
          }
          _item = FavoriteStation(_tmpStationuuid,_tmpName,_tmpUrl_resolved,_tmpFavicon,_tmpCountrycode,_tmpTags)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun isFavorite(uuid: String): Boolean {
    val _sql: String = "SELECT EXISTS(SELECT 1 FROM favorites WHERE stationuuid = ?)"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, uuid)
        val _result: Boolean
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp != 0
        } else {
          _result = false
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteByUuid(uuid: String) {
    val _sql: String = "DELETE FROM favorites WHERE stationuuid = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, uuid)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
