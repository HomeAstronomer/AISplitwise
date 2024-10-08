package com.example.aisplitwise.data.repository

import com.example.aisplitwise.data.local.Expense
import com.example.aisplitwise.data.local.Group
import com.example.aisplitwise.data.local.GroupDao
import com.example.aisplitwise.data.local.Member
import com.example.aisplitwise.data.local.MemberDao
import com.example.aisplitwise.data.local.toMap
import com.example.aisplitwise.utils.MEMBER_COLLECTION
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class GroupRepository @Inject constructor(
    private  val groupDao:GroupDao,
    private  val memberDao:MemberDao,
    private val fireStoreDb: FirebaseFirestore,
) {
    fun getGroupsDb(): Flow<List<Group>> {
        return groupDao.getAllFlow()
    }

    suspend fun getGroupFromID(groupId: String):Flow<Group>{
        return groupDao.getGroup(groupId)
    }

    fun getGroups(member: Member): Flow<DataState<List<Group>>> = flow {

        val joinedGroups = member.joinedGroupIds
        val createdGroups = member.createdGroupIds
        val allGroups = joinedGroups + createdGroups

        if (allGroups.isNotEmpty()) {
            try {
                val collection = fireStoreDb.collection("groups")
                    .whereIn("id", allGroups)
                    .get()
                    .await()

                val groupsWithExpenses =
                    collection.documents.mapNotNull { document ->
                        val group = document.toObject(Group::class.java)
                        group?.let {
                                // Fetch the expenses sub-collection for this group
                                val expensesSnapshot = fireStoreDb.collection("groups")
                                    .document(document.id)
                                    .collection("expenses")
                                    .get()
                                    .await()

                                val expenses = expensesSnapshot.toObjects(Expense::class.java)

                                // Attach expenses to the group object or modify it accordingly
                                group.apply { this.expenses = expenses }
                            }

                    }
                groupDao.insertAll(groupsWithExpenses)
                emit(DataState.Success(groupsWithExpenses))

            } catch (e: Exception) {
                emit(DataState.Error(e.message ?: "Error fetching groups"))
            }
        } else {
            emit(DataState.Error("No group IDs found."))
        }
    }

    fun getNewGroupId(): String {
        return fireStoreDb.collection("groups").document().id
    }

    fun createGroup(group: Group, uid: String): Flow<DataState<Unit>> = flow {
        try {
            val groupMap = group.copy(expenses = emptyList()).toMap()
            fireStoreDb.collection("groups")
                .document(group.id)
                .set(groupMap)
                .await()

            addGroupIdToMemberCreatedGroups(uid, group.id)

            groupDao.insert(group)

            emit(DataState.Success(Unit))

        } catch (e: Exception) {
            emit(DataState.Error(e.message ?: "Something Went Wrong"))
        }
    }


    // while joining group following  this occur
    //addmember to group-> add group id to member joined groups  -> get the new Member -> add new member to db  -> call the get groups to refresh Groups
    fun joinGroup(member: Member,groupId: String): Flow<DataState<Unit>> =flow {
        try {
        fireStoreDb.collection("groups")
            .document(groupId)
            .update("members", FieldValue.arrayUnion(member.toMap()))
            val updatedMember=addGroupIdToMemberJoinedGroups(member.uid,groupId)
            updatedMember?.let {
                getGroups(it)
            }
            emit(DataState.Success(Unit))

        } catch (e: Exception) {
            emit(DataState.Error(e.message ?: "Something Went Wrong"))
        }

    }
    private suspend fun addGroupIdToMemberJoinedGroups(uid: String, groupId: String):Member? {
        // Update the Member's createdGroupIds in Firestore
        fireStoreDb.collection(MEMBER_COLLECTION)
            .document(uid)
            .update("joinedGroupIds", FieldValue.arrayUnion(groupId))
            .await()

        // Fetch the updated Member from Firestore and add it to the local DAO
        val memberDocument = fireStoreDb.collection(MEMBER_COLLECTION)
            .document(uid)
            .get()
            .await()

        val member = memberDocument.toObject(Member::class.java)
        member?.let {
            memberDao.insertMember(it)  // Assuming `insertOrUpdate` is a function that either inserts or updates a member in your DAO
        }
        return member
    }

    private suspend fun addGroupIdToMemberCreatedGroups(uid: String, groupId: String) {
        // Update the Member's createdGroupIds in Firestore
        fireStoreDb.collection(MEMBER_COLLECTION)
            .document(uid)
            .update("createdGroupIds", FieldValue.arrayUnion(groupId))
            .await()

        // Fetch the updated Member from Firestore and add it to the local DAO
        val memberDocument = fireStoreDb.collection(MEMBER_COLLECTION)
            .document(uid)
            .get()
            .await()

        val member = memberDocument.toObject(Member::class.java)
        member?.let {
            memberDao.insertMember(it)  // Assuming `insertOrUpdate` is a function that either inserts or updates a member in your DAO
        }
    }

    fun addExpense(group: Group, expense: Expense): Flow<DataState<Unit>> = flow {

        try {
            // Generate expense ID and update the expense object
            val expenseId = fireStoreDb.collection("groups")
                .document(group.id)
                .collection("expenses")
                .document().id

            val updatedExpense = expense.copy(id = expenseId)
            val expenseMap = updatedExpense.toMap()

            // Add the expense to Firestore
            fireStoreDb.collection("groups")
                .document(group.id)
                .collection("expenses")
                .document(expenseId)
                .set(expenseMap)
                .await() // Use await to work with coroutines
            val newExpenseList=group.expenses+updatedExpense
            groupDao.updateExpensesAndTimestamp(group.id,newExpenseList, Timestamp(Date()))
            emit(DataState.Success(Unit)) // Emit success state with no data

        } catch (e: Exception) {
            emit(DataState.Error(e.message ?: "Something Went Wrong")) // Emit error state
        }
    }

    fun getCurrentTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        return dateFormat.format(Date())
    }


}