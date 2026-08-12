package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TriggerEntity::class, TemplateEntity::class, FilledResultEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun triggerDao(): TriggerDao
    abstract fun templateDao(): TemplateDao
    abstract fun filledResultDao(): FilledResultDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "template_flow_database"
                )
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            suspend fun populateInitialData(db: AppDatabase) {
                if (db.triggerDao().getCount() == 0) {
                    db.triggerDao().insertTriggers(
                        listOf(
                            TriggerEntity(tag = "word", displayName = "کلمه", colorHex = "#8B5CF6", isDefault = true),
                            TriggerEntity(tag = "chord", displayName = "آکورد", colorHex = "#EC4899", isDefault = true),
                            TriggerEntity(tag = "name", displayName = "نام", colorHex = "#3B82F6", isDefault = true),
                            TriggerEntity(tag = "price", displayName = "قیمت", colorHex = "#10B981", isDefault = true),
                            TriggerEntity(tag = "date", displayName = "تاریخ", colorHex = "#F59E0B", isDefault = true)
                        )
                    )
                }

                if (db.templateDao().getCount() == 0) {
                    db.templateDao().insertTemplate(
                        TemplateEntity(
                            title = "نمونه آکورد و متن ترانه",
                            content = "بلا بلا بلا [word] بلا بلا [word] بلابلابلا [chord] بلا بلا\n[chord] ترانه یادگار [word] با ریتم زیبا [word] بنواز.",
                            category = "موسیقی"
                        )
                    )
                    db.templateDao().insertTemplate(
                        TemplateEntity(
                            title = "پیام تبریک و اطلاع‌رسانی سفارش",
                            content = "سلام [name] عزیز! سفارش شما به ارزش [price] در تاریخ [date] آماده ارسال شد. کلمه [word] را جهت پیگیری به خاطر داشته باشید.",
                            category = "پیامک"
                        )
                    )
                }
            }
        }
    }
}
