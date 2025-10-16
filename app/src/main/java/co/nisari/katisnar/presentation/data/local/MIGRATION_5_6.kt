package co.nisari.katisnar.presentation.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE `route_points`
            ADD COLUMN `location` TEXT NOT NULL DEFAULT ''
            """.trimIndent()
        )
    }
}
