package com.example.aisplitwise.feature.feature_ledger

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.aisplitwise.navigation.LedgerRoute
import com.example.aisplitwise.data.local.Expense
import com.example.aisplitwise.data.local.Group
import com.example.aisplitwise.data.local.Member
import com.example.aisplitwise.data.repository.GroupRepository
import com.example.aisplitwise.data.repository.MemberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject



@Immutable
data class LedgerUIState(
    val group:Group?=null,
    val member:Member?=null,
    val expense: List<Expense> =emptyList()

)

@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository,
    private val memberRepository: MemberRepository
): ViewModel()  {
    val expenseFlow: StateFlow<Expense?> = savedStateHandle.getStateFlow("expenseKey", null)
    private val _uiState = MutableStateFlow(LedgerUIState())
    val uiState: StateFlow<LedgerUIState> = _uiState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LedgerUIState()
    )
    init {
        getGroup(savedStateHandle.toRoute<LedgerRoute>().groupId)
        getMembersFromDb()
    }

    private fun getGroup(groupId: String) {
        viewModelScope.launch (Dispatchers.IO) {
            groupRepository.getGroupFromID(groupId).collect{group->
                _uiState.update { it.copy(group = group) }
            }

        }
        viewModelScope.launch(Dispatchers.IO) {
           groupRepository.getExpenseFromGroupId(groupId).collect{expense->
                _uiState.update { it.copy(expense = expense) }

            }
        }
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

    fun setExpense(expense: Expense) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value.member?.let { member ->
                groupRepository.addExpense(
                    uiState.value.group!!,
                    expense.copy(paidBy = member)
                ).collect {}
            }
        }
    }
}