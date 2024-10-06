package com.example.aisplitwise.data.local

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

const val MEMBER_TABLE="member_table"
@Keep
@Parcelize
@Serializable
@Entity(tableName = MEMBER_TABLE)
data class Member(
    @PrimaryKey
    val uid: String="",                     // Unique identifier for the user
    val displayName: String?="",            // User's display name
    val email: String?="",                  // User's email
    val phoneNumber: String?="",            // User's phone number
    val photoUrl: String?="",               // URL to the user's profile photo
    val createdGroupIds: List<String> = emptyList(),  // List of group IDs the user has created
    val joinedGroupIds: List<String> = emptyList()    // List of group IDs the user has joined
):Parcelable

fun Member.toMap(): Map<String, Any?> {
    return mapOf(
        "uid" to uid,
        "displayName" to displayName,
        "email" to email,
        "phoneNumber" to phoneNumber,
        "photoUrl" to photoUrl,
        "createdGroupIds" to createdGroupIds,
        "joinedGroupIds" to joinedGroupIds
    )
}


@Dao
interface MemberDao {
    @Query("SELECT * FROM $MEMBER_TABLE")
    fun getAll(): List<Member>

    @Delete
    fun delete(user: Member)

    @Query("SELECT * FROM $MEMBER_TABLE")
    fun getAllFlow(): Flow<List<Member>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member)

    @Query("DELETE FROM member_table") // Replace 'member_table' with your actual table name
    suspend fun deleteAllMembers()
}

