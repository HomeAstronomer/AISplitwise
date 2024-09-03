package com.example.aisplitwise

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.aisplitwise.data.local.Converters
import com.example.aisplitwise.data.local.Group
import com.example.aisplitwise.data.local.GroupDao
import com.example.aisplitwise.data.local.Member
import com.example.aisplitwise.data.local.MemberDao

@Database(entities = [Group::class,
                     Member::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao

    abstract fun groupDao():GroupDao
}