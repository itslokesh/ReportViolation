package com.example.reportviolation.data.local

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.reportviolation.data.local.dao.UserDao
import com.example.reportviolation.data.local.dao.ViolationReportDao
import com.example.reportviolation.data.local.converter.DateTimeConverter
import com.example.reportviolation.data.local.converter.ListConverter
import com.example.reportviolation.data.model.User
import com.example.reportviolation.data.model.UserSession
import com.example.reportviolation.data.model.ViolationReport

@Database(
    entities = [
        ViolationReport::class,
        User::class,
        UserSession::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(DateTimeConverter::class, ListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun violationReportDao(): ViolationReportDao
    abstract fun userDao(): UserDao
    
    companion object {
        const val DATABASE_NAME = "traffic_violation_db"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        // Migration from version 1 to 2 (for future use)
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns or tables as needed
                // Example: database.execSQL("ALTER TABLE violation_reports ADD COLUMN new_field TEXT")
            }
        }
    }
} 