package com.supersci.calculator.data

import android.content.Context
import androidx.room.Room

class DatabaseProvider private constructor(context: Context) {

    val db: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "super_sci_calculator.db"
    ).fallbackToDestructiveMigration().build()

    companion object {
        @Volatile
        private var INSTANCE: DatabaseProvider? = null

        fun getInstance(context: Context): DatabaseProvider {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DatabaseProvider(context).also { INSTANCE = it }
            }
        }
    }
}
