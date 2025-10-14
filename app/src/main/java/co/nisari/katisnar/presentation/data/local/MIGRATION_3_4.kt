package co.nisari.katisnar.presentation.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            ALTER TABLE `checklists`
            ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0
        """.trimIndent())
        db.execSQL(
            "UPDATE `checklists` SET `createdAt` = CASE " +
                "WHEN `createdAt` = 0 THEN CAST(strftime('%s','now') AS INTEGER) * 1000 ELSE `createdAt` END"
        )
    }
}
