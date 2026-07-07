package de.rittitservice.frequenzia.data

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FavoritesDatabaseMigrationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val driver = AndroidSQLiteDriver()
    private val dbFile = context.getDatabasePath(TEST_DB)

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        dbFile,
        driver,
        FavoritesDatabase::class,
        {
            Room.databaseBuilder(context, FavoritesDatabase::class.java, dbFile.path)
                .setDriver(driver)
                .build()
        },
        emptyList()
    )

    @Test
    fun migrate1To2_preservesFavoritesAndAddsRecentlyPlayedTable() {
        helper.createDatabase(1).apply {
            execSQL(
                "INSERT INTO favorites (stationuuid, name, url_resolved, favicon, countrycode, tags) " +
                    "VALUES ('abc-123', 'Test FM', 'https://stream.example/test', NULL, 'DE', 'pop')"
            )
            close()
        }

        val migratedDb = helper.runMigrationsAndValidate(2, listOf(MIGRATION_1_2))
        assertEquals(1L, countRows(migratedDb, "favorites"))
        assertEquals(0L, countRows(migratedDb, "recently_played"))
        migratedDb.close()
    }

    private fun countRows(connection: SQLiteConnection, table: String): Long {
        connection.prepare("SELECT COUNT(*) FROM $table").use { statement ->
            statement.step()
            return statement.getLong(0)
        }
    }

    companion object {
        private const val TEST_DB = "migration-test"
    }
}
