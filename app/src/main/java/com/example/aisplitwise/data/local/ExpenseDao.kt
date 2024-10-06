package com.example.aisplitwise.data.local

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.compose.runtime.Stable
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.parcelize.Parcelize
import java.util.Date


const val EXPENSE_TABLE="expense_table"

@Keep
@Parcelize
@Stable
@Entity(tableName = EXPENSE_TABLE,
    indices = [Index(value = ["groupId"])])
data class Expense(
    @PrimaryKey
    val id: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val paidBy: Member = Member(),
    val splitAmong: List<Member> = emptyList(),
    val createdAt: Timestamp = Timestamp(Date()),
    val updatedAt: Timestamp = Timestamp(Date()),
    val groupId: String = "",
    val latitude: Double = 0.0,   // Latitude as a Double
    val longitude: Double = 0.0   // Longitude as a Double
): Parcelable

fun Expense.toMap(): Map<String, Any> {
    return mapOf(
        "id" to id,
        "description" to description,
        "amount" to amount,
        "paidBy" to paidBy.toMap(),
        "splitAmong" to splitAmong.map { it.toMap() },
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
        "groupId" to groupId,
        "latitude" to latitude,
        "longitude" to longitude
    )
}


@Dao
interface ExpenseDao {
    @Query("SELECT * FROM $EXPENSE_TABLE")
    fun getAll(): List<Expense>

    @Delete
    fun delete(user: Member)

    @Query("SELECT * FROM $EXPENSE_TABLE")
    fun getAllFlow(): Flow<List<Expense>>

    @Query("SELECT * FROM $EXPENSE_TABLE WHERE groupId = :groupId")
    fun getExpenseForGroup(groupId: String): Flow<List<Expense>>

    @Insert
    suspend fun insert( expense: Expense)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(expense:List<Expense>)

}