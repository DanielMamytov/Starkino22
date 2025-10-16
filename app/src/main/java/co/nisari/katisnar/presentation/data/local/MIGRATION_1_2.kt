package co.nisari.katisnar.presentation.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `star_routes` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `date` TEXT NOT NULL,
                `time` TEXT NOT NULL,
                `description` TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `route_points` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `routeId` INTEGER NOT NULL,
                `lat` REAL NOT NULL,
                `lng` REAL NOT NULL,
                `location` TEXT NOT NULL DEFAULT '',
                FOREIGN KEY(`routeId`) REFERENCES `star_routes`(`id`) ON DELETE CASCADE
            )
        """.trimIndent())
    }
}
