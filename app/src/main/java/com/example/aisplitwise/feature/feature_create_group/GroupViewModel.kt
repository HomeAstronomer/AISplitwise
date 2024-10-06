package com.example.aisplitwise.feature.feature_create_group

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aisplitwise.data.local.Group
import com.example.aisplitwise.data.local.Member
import com.example.aisplitwise.data.repository.DataState
import com.example.aisplitwise.data.repository.GroupRepository
import com.example.aisplitwise.data.repository.MemberRepository
import com.example.aisplitwise.feature.dashboard.DashboardUiState
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

@Immutable
data class CreateGroupUIState(
    val imgList: List<String> =emptyList(),
    val showLoader:Boolean=false,
    val member:Member?=null,
    val showToast:Boolean=false,
    val toastMessage:String=""

)

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val firebaseStorage: FirebaseStorage,
    private val groupRepository: GroupRepository,
    private val memberRepository: MemberRepository
) : ViewModel() {

    init {
        getallImageURI()
        getMembersFromDb()
    }

    private fun getMembersFromDb() {
        viewModelScope.launch(Dispatchers.IO) {
            memberRepository.getMemberDb().collect { memberList ->
                memberList.getOrNull(0)?.let { firstMember->
                    _uiState.update { it.copy(member = firstMember) }

                }

            }
        }
    }


    private val _uiState = MutableStateFlow(CreateGroupUIState())
    val uiState: StateFlow<CreateGroupUIState> = _uiState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CreateGroupUIState()
    )
    private fun getallImageURI() {
        viewModelScope.launch (Dispatchers.IO) {
            getAllImageUrisFromFirebase("groupImages")
        }
    }

    suspend fun getAllImageUrisFromFirebase(folderPath: String): List<String> {
        // Reference to Firebase Storage
        val storageReference: StorageReference = firebaseStorage.reference.child(folderPath)

        // List to store image URIs
        val imageUris = ArrayList<String>()

        try {
            // List all items (files) in the folder
            val result = storageReference.listAll().await()

            // Loop through the items and get their download URIs
            for (fileRef in result.items) {
                val uri = fileRef.downloadUrl.await()  // Get the download URL asynchronously
                imageUris.add(uri.toString())  // Add URI to the list
            }

        } catch (e: Exception) {
            e.printStackTrace()
            // Handle any errors (e.g., folder not found, network issues)
        }
        _uiState.update { it.copy(imgList = imageUris) }
        return imageUris
    }

    fun createGroup(groupName: String, groupImage: String, onSuccess: () -> Unit) {
        _uiState.update { it.copy(showLoader = true) }
        viewModelScope.launch(Dispatchers.IO) {
            uiState.value.member?.let {
                val group = Group(
                    id = getNewGroupId(),
                    name = groupName,
                    members = listOf(it),
                    createdAt = Timestamp(Date()),
                    updatedAt = Timestamp(Date()),
                    groupImg = groupImage
                )
                groupRepository.createGroup(group, it.uid).collect { dataState ->
                    _uiState.update { it.copy(showLoader = false) }
                    when (dataState) {
                        is DataState.Success -> {
                            _uiState.update {uistate->
                                uistate.copy(showToast = true, toastMessage = "Group Created Successfully")
                            }
                            withContext(Dispatchers.Main) {
                                onSuccess.invoke()
                            }
                        }

                        is DataState.Error -> {
                            _uiState.update {uistate->
                                uistate.copy(
                                    showToast = true,
                                    toastMessage = dataState.errorMessage
                                )
                            }
                        }
                    }

                }
            }
        }

    }
    fun getNewGroupId(): String {
        return groupRepository.getNewGroupId()
    }

}