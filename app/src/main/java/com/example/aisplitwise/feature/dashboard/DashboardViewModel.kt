package com.example.aisplitwise.feature.dashboard

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aisplitwise.data.local.Group
import com.example.aisplitwise.data.local.Member
import com.example.aisplitwise.data.repository.DataState
import com.example.aisplitwise.data.repository.GroupRepository
import com.example.aisplitwise.data.repository.MemberRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject


@Immutable
data class DashboardUiState(
    val groupList:List<Group> = emptyList(),
    val showToast:Boolean=false,
    val toastMessage:String="",
    val showLoader:Boolean=false,
    val member: Member? =null

)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val memberRepository: MemberRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    init {
        getGroupsFromDb()
        getMembersFromDb()
//        fireBaseInitWithDummyData()
    }

    private fun getMembersFromDb() {
        viewModelScope.launch(Dispatchers.IO) {
            memberRepository.getMemberDb().collect { memberList ->
                memberList.getOrNull(0)?.let { firstMember->
                    _uiState.update { it.copy(member = firstMember) }
                    getGroupsApiCall()

                }

            }
        }
    }

    fun getGroupsApiCall() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value.member?.let {
                groupRepository.getGroups(it).collect {}
            }
        }
    }

    private fun getGroupsFromDb() {
        viewModelScope.launch (Dispatchers.IO){
            groupRepository.getGroupsDb().collect{groupList->
                _uiState.update { it.copy(groupList = groupList) }
            }
        }
    }

    fun getNewGroupId(): String {
        return groupRepository.getNewGroupId()
    }

    fun createGroup() {
        _uiState.update { it.copy(showLoader = true) }
        viewModelScope.launch(Dispatchers.IO) {
            uiState.value.member?.let {
                val group = Group(
                    id = getNewGroupId(),
                    name = "The Boys",
                    members = listOf(it),
                    createdAt = Timestamp(Date()),
                    updatedAt = Timestamp(Date()),
                )
                groupRepository.createGroup(group, it.uid).collect { dataState ->
                    _uiState.update { it.copy(showLoader = false) }
                    when (dataState) {
                        is DataState.Success -> {
                            _uiState.update {uistate->
                                uistate.copy(showToast = true, toastMessage = "Group Created Successfully")
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
                        else->{}
                    }

                }
            }
        }

    }

}