package com.example.aisplitwise.data

import android.content.Context
import androidx.room.Room
import com.example.aisplitwise.data.local.ExpenseDao
import com.example.aisplitwise.data.local.GroupDao
import com.example.aisplitwise.data.local.MemberDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProviderModule {

    @Singleton
    @Provides
    fun provideDataBase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "Spliwise.db"
        ).build()
    }

    @Singleton
    @Provides
    fun provideFireStore(): FirebaseFirestore {
        return  Firebase.firestore
    }

    @Singleton
    @Provides
    fun provideFireBaseAuth(): FirebaseAuth {
        return   Firebase.auth
    }

    @Singleton
    @Provides
    fun provideFireBaseStorage(): FirebaseStorage {
        return   Firebase.storage
    }


    @Provides
    fun provideMemberDao(database: AppDatabase): MemberDao = database.memberDao()

    @Provides
    fun provideGroupDao(database: AppDatabase): GroupDao = database.groupDao()

    @Provides
    fun provideExpenseDao(database: AppDatabase): ExpenseDao = database.expenseDao()
}
