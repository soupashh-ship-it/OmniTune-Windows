package com.omnitune.windows.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.omnitune.windows.db.OmniDatabase
import java.io.File

object DatabaseFactory {
    fun createDatabase(): OmniDatabase {
        val dbFile = File(System.getProperty("user.home"), ".omnitune/data.db")
        dbFile.parentFile.mkdirs()
        
        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
        
        // Ensure schema is created if needed
        if (!dbFile.exists() || dbFile.length() == 0L) {
            OmniDatabase.Schema.create(driver)
        } else {
            // Optional: run migrations
            // OmniDatabase.Schema.migrate(driver, oldVersion, newVersion)
        }
        
        return OmniDatabase(driver)
    }
}
