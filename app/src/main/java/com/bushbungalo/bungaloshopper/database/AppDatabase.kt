package com.bushbungalo.bungaloshopper.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bushbungalo.bungaloshopper.R
import com.bushbungalo.bungaloshopper.model.ShoppingListItemEntity

@Database(entities = [ShoppingListItemEntity::class], version = 1,
    exportSchema = false)
abstract class AppDatabase : RoomDatabase()
{
    abstract fun productDao(): ShoppingListDao

    companion object
    {
        @Volatile
        lateinit var INSTANCE: AppDatabase

        /**
         * Returns an instance of a [AppDatabase] object
         */
        fun getInstance(context: Context): AppDatabase
        {
            synchronized(this)
            {
                val instance = Room.databaseBuilder(
                    context, AppDatabase::class.java,
                    context.getString(R.string.db_name)).build()

                INSTANCE = instance

                return instance
            }// end of synchronized block
        }// end of function getInstance
    }
}// end of class AppDatabase